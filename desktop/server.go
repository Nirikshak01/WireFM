package main

import (
	"crypto/md5"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"io"
	"mime"
	"net/http"
	"os"
	"path/filepath"
	"runtime"
	"strings"
	"sync"
	"time"

	"golang.org/x/net/webdav"
)

type ServerFileInfo struct {
	Name    string    `json:"name"`
	Path    string    `json:"path"`
	IsDir   bool      `json:"is_dir"`
	Size    int64     `json:"size"`
	ModTime time.Time `json:"mod_time"`
	Ext     string    `json:"ext"`
}

type PhoneWorkerCommand struct {
	ID        string `json:"id"`
	Action    string `json:"action"`
	Path      string `json:"path,omitempty"`
	DestPath  string `json:"dest_path,omitempty"`
	Token     string `json:"token,omitempty"`
	Source    string `json:"source,omitempty"`
	Target    string `json:"target,omitempty"`
	Name      string `json:"name,omitempty"`
	Timestamp int64  `json:"timestamp"`
}

type PhoneCommandResponse struct {
	ID      string `json:"id"`
	Success bool   `json:"success"`
	Error   string `json:"error,omitempty"`
	Data    any    `json:"data,omitempty"`
}

var (
	phoneCmdQueue   = make([]PhoneWorkerCommand, 0)
	phoneCmdMu      sync.Mutex
	phoneCmdResults = make(map[string]PhoneCommandResponse)
	phoneResMu      sync.Mutex
	phonePendingReq = make(map[string]chan PhoneCommandResponse)
	phoneReqMu      sync.Mutex
)

// startEmbeddedServer launches the WireFM HTTP & WebDAV core server in background
func (a *App) startEmbeddedServer() {
	home, _ := os.UserHomeDir()
	if home == "" {
		home = "/"
	}

	mux := http.NewServeMux()

	auth := func(next http.HandlerFunc) http.HandlerFunc {
		return func(w http.ResponseWriter, r *http.Request) {
			w.Header().Set("Access-Control-Allow-Origin", "*")
			w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
			w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Password")

			if r.Method == "OPTIONS" {
				w.WriteHeader(http.StatusOK)
				return
			}

			pw := r.Header.Get("X-Password")
			if pw == "" {
				pw = r.URL.Query().Get("password")
			}

			if pw != a.pin {
				w.Header().Set("Content-Type", "application/json")
				w.WriteHeader(http.StatusUnauthorized)
				json.NewEncoder(w).Encode(map[string]string{"error": "Invalid password"})
				return
			}
			next(w, r)
		}
	}

	// 1. Info & Auth
	mux.HandleFunc("/api/info", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		w.Header().Set("Access-Control-Allow-Origin", "*")
		json.NewEncoder(w).Encode(map[string]string{
			"version": "2.0.0",
			"app":     "WireFM Desktop",
			"os":      runtime.GOOS,
			"ip":      a.ip,
			"port":    a.port,
		})
	})

	mux.HandleFunc("/api/verify", auth(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		host, _ := os.Hostname()
		json.NewEncoder(w).Encode(map[string]interface{}{
			"success": true,
			"user":    os.Getenv("USER"),
			"host":    host,
			"root":    home,
		})
	}))

	// 2. File operations
	mux.HandleFunc("/api/files", auth(func(w http.ResponseWriter, r *http.Request) {
		reqPath := r.URL.Query().Get("path")
		cleanPath := a.resolvePath(reqPath)

		entries, err := os.ReadDir(cleanPath)
		if err != nil {
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusBadRequest)
			json.NewEncoder(w).Encode(map[string]string{"error": err.Error()})
			return
		}

		var files []ServerFileInfo
		for _, entry := range entries {
			info, err := entry.Info()
			if err != nil {
				continue
			}
			filePath := filepath.Join(cleanPath, entry.Name())
			files = append(files, ServerFileInfo{
				Name:    entry.Name(),
				Path:    filePath,
				IsDir:   entry.IsDir(),
				Size:    info.Size(),
				ModTime: info.ModTime(),
				Ext:     strings.ToLower(filepath.Ext(entry.Name())),
			})
		}
		if files == nil {
			files = []ServerFileInfo{}
		}

		w.Header().Set("Content-Type", "application/json")
		host, _ := os.Hostname()
		json.NewEncoder(w).Encode(map[string]interface{}{
			"path":  cleanPath,
			"files": files,
			"os":    runtime.GOOS,
			"user":  os.Getenv("USER"),
			"host":  host,
		})
	}))

	mux.HandleFunc("/api/download", auth(func(w http.ResponseWriter, r *http.Request) {
		path := r.URL.Query().Get("path")
		if path == "" {
			http.Error(w, "path required", http.StatusBadRequest)
			return
		}

		cleanPath := a.resolvePath(path)
		info, err := os.Stat(cleanPath)
		if err != nil {
			http.Error(w, "file not found", http.StatusNotFound)
			return
		}
		if info.IsDir() {
			http.Error(w, "cannot download directory", http.StatusBadRequest)
			return
		}

		inline := r.URL.Query().Get("inline") == "1" || r.URL.Query().Get("inline") == "true"
		if inline {
			w.Header().Set("Content-Disposition", "inline; filename="+filepath.Base(cleanPath))
			ext := strings.ToLower(filepath.Ext(cleanPath))
			ctype := mime.TypeByExtension(ext)
			if ctype != "" {
				w.Header().Set("Content-Type", ctype)
			}
		} else {
			w.Header().Set("Content-Disposition", "attachment; filename="+filepath.Base(cleanPath))
			w.Header().Set("Content-Type", "application/octet-stream")
		}
		http.ServeFile(w, r, cleanPath)
	}))

	mux.HandleFunc("/api/upload", auth(func(w http.ResponseWriter, r *http.Request) {
		destPath := r.URL.Query().Get("path")
		cleanDestDir := a.resolvePath(destPath)

		err := r.ParseMultipartForm(1024 << 20) // 1GB max
		if err != nil {
			w.WriteHeader(http.StatusBadRequest)
			json.NewEncoder(w).Encode(map[string]string{"error": err.Error()})
			return
		}

		var uploaded []string
		if r.MultipartForm != nil && r.MultipartForm.File != nil {
			for _, fileHeaders := range r.MultipartForm.File {
				for _, fileHeader := range fileHeaders {
					file, err := fileHeader.Open()
					if err != nil {
						continue
					}
					destFile := filepath.Join(cleanDestDir, filepath.Base(fileHeader.Filename))
					out, err := os.Create(destFile)
					if err != nil {
						file.Close()
						continue
					}
					io.Copy(out, file)
					out.Close()
					file.Close()
					uploaded = append(uploaded, destFile)
				}
			}
		}

		w.Header().Set("Content-Type", "application/json")
		if len(uploaded) == 0 {
			w.WriteHeader(http.StatusBadRequest)
			json.NewEncoder(w).Encode(map[string]string{"error": "No file uploaded"})
			return
		}
		json.NewEncoder(w).Encode(map[string]interface{}{
			"success": "Uploaded",
			"count":   len(uploaded),
			"files":   uploaded,
		})
	}))

	mux.HandleFunc("/api/delete", auth(func(w http.ResponseWriter, r *http.Request) {
		path := r.URL.Query().Get("path")
		if path == "" {
			w.WriteHeader(http.StatusBadRequest)
			json.NewEncoder(w).Encode(map[string]string{"error": "path required"})
			return
		}
		cleanPath := a.resolvePath(path)
		err := os.RemoveAll(cleanPath)
		if err != nil {
			w.WriteHeader(http.StatusInternalServerError)
			json.NewEncoder(w).Encode(map[string]string{"error": err.Error()})
			return
		}
		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(map[string]string{"success": "Deleted"})
	}))

	mux.HandleFunc("/api/mkdir", auth(func(w http.ResponseWriter, r *http.Request) {
		var req struct {
			Path string `json:"path"`
			Name string `json:"name"`
		}
		json.NewDecoder(r.Body).Decode(&req)
		targetDir := a.resolvePath(req.Path)
		newFolder := filepath.Join(targetDir, req.Name)

		err := os.MkdirAll(newFolder, 0755)
		if err != nil {
			w.WriteHeader(http.StatusInternalServerError)
			json.NewEncoder(w).Encode(map[string]string{"error": err.Error()})
			return
		}
		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(map[string]string{"success": "Created", "path": newFolder})
	}))

	// 3. Mobile Phone Integration (Heartbeat & Worker)
	mux.HandleFunc("/api/phone/heartbeat", auth(func(w http.ResponseWriter, r *http.Request) {
		var hb struct {
			PhoneName string      `json:"phone_name"`
			IP        string      `json:"ip"`
			Files     []FileEntry `json:"files,omitempty"`
		}
		json.NewDecoder(r.Body).Decode(&hb)
		if hb.PhoneName == "" {
			hb.PhoneName = "Android Phone"
		}
		clientIP := hb.IP
		if clientIP == "" {
			clientIP = strings.Split(r.RemoteAddr, ":")[0]
		}

		a.SetConnectedDevice(hb.PhoneName, clientIP)

		if len(hb.Files) > 0 {
			a.mu.Lock()
			a.connectedFiles = hb.Files
			a.mu.Unlock()
		}

		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(map[string]string{"status": "ok"})
	}))

	mux.HandleFunc("/api/phone/poll", auth(func(w http.ResponseWriter, r *http.Request) {
		phoneCmdMu.Lock()
		defer phoneCmdMu.Unlock()
		w.Header().Set("Content-Type", "application/json")
		if len(phoneCmdQueue) > 0 {
			cmd := phoneCmdQueue[0]
			phoneCmdQueue = phoneCmdQueue[1:]
			json.NewEncoder(w).Encode(cmd)
		} else {
			json.NewEncoder(w).Encode(map[string]string{"id": ""})
		}
	}))

	mux.HandleFunc("/api/phone/respond", auth(func(w http.ResponseWriter, r *http.Request) {
		var resp PhoneCommandResponse
		json.NewDecoder(r.Body).Decode(&resp)

		phoneResMu.Lock()
		phoneCmdResults[resp.ID] = resp
		phoneResMu.Unlock()

		phoneReqMu.Lock()
		if ch, ok := phonePendingReq[resp.ID]; ok {
			ch <- resp
			delete(phonePendingReq, resp.ID)
		}
		phoneReqMu.Unlock()

		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(map[string]string{"status": "received"})
	}))

	// 4. WebDAV server (for native OS file manager mounting)
	webdavHandler := &webdav.Handler{
		Prefix:     "/webdav",
		FileSystem: webdav.Dir(home),
		LockSystem: webdav.NewMemLS(),
	}
	mux.Handle("/webdav/", webdavHandler)

	// Start Background HTTP Server
	go func() {
		fmt.Printf("WireFM Server listening on http://%s:%s/ (PIN: %s)\n", a.ip, a.port, a.pin)
		if err := http.ListenAndServe(":"+a.port, mux); err != nil {
			fmt.Println("Server error:", err)
		}
	}()

	// Start Background WebDAV Server on 8081 (or port+1)
	webdavPort := "8081"
	if webdavPort == a.port {
		webdavPort = "8082"
	}
	webdavMux := http.NewServeMux()
	webdavMux.Handle("/", &webdav.Handler{
		FileSystem: webdav.Dir(home),
		LockSystem: webdav.NewMemLS(),
	})
	go func() {
		_ = http.ListenAndServe(":"+webdavPort, webdavMux)
	}()
}

func md5Hash(text string) string {
	hash := md5.Sum([]byte(text))
	return hex.EncodeToString(hash[:])
}
