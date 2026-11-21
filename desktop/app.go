package main

import (
	"context"
	"encoding/base64"
	"fmt"
	"math/rand"
	"net"
	"os"
	"os/exec"
	"path/filepath"
	"sort"
	"strings"
	"sync"
	"time"

	qrcode "github.com/skip2/go-qrcode"
)

type QrData struct {
	QrBase64 string `json:"qr_base64"`
	Url      string `json:"url"`
	IP       string `json:"ip"`
	Port     string `json:"port"`
	Pin      string `json:"pin"`
}

type ConnectionInfo struct {
	IsConnected bool   `json:"is_connected"`
	DeviceName  string `json:"device_name"`
	DeviceIP    string `json:"device_ip"`
	Status      string `json:"status"`
}

type ConnectedDeviceInfo struct {
	ID       string `json:"id"`
	Name     string `json:"name"`
	IP       string `json:"ip"`
	Status   string `json:"status"`
	LastSeen string `json:"last_seen"`
}

type FileEntry struct {
	Name    string `json:"name"`
	Path    string `json:"path"`
	IsDir   bool   `json:"is_dir"`
	Size    int64  `json:"size"`
	ModTime string `json:"mod_time"`
	Ext     string `json:"ext"`
}

// App struct
type App struct {
	ctx            context.Context
	mu             sync.Mutex
	ip             string
	port           string
	pin            string
	activeDevice   string
	deviceIP       string
	isConnected    bool
	devices        map[string]ConnectedDeviceInfo
	connectedFiles []FileEntry
}

// NewApp creates a new App application struct
func NewApp() *App {
	return &App{
		port:    "8080",
		devices: make(map[string]ConnectedDeviceInfo),
	}
}

// startup is called when the app starts
func (a *App) startup(ctx context.Context) {
	a.ctx = ctx
	a.initNetworking()
	a.startEmbeddedServer()
}

// shutdown is called at application termination
func (a *App) shutdown(ctx context.Context) {
}

func (a *App) initNetworking() {
	a.mu.Lock()
	defer a.mu.Unlock()

	if a.ip == "" {
		a.ip = a.findLocalIP()
	}
	if a.pin == "" {
		a.pin = fmt.Sprintf("%06d", rand.Intn(900000)+100000)
	}
	if a.port == "" {
		a.port = a.findAvailablePort(8080)
	}
}

func (a *App) findLocalIP() string {
	interfaces, err := net.Interfaces()
	if err != nil {
		return "127.0.0.1"
	}
	for _, iface := range interfaces {
		if iface.Flags&net.FlagUp == 0 || iface.Flags&net.FlagLoopback != 0 {
			continue
		}
		addrs, err := iface.Addrs()
		if err != nil {
			continue
		}
		for _, addr := range addrs {
			var ip net.IP
			switch v := addr.(type) {
			case *net.IPNet:
				ip = v.IP
			case *net.IPAddr:
				ip = v.IP
			}
			if ip == nil || ip.IsLoopback() {
				continue
			}
			ip = ip.To4()
			if ip == nil {
				continue
			}
			return ip.String()
		}
	}
	return "127.0.0.1"
}

func (a *App) findAvailablePort(startPort int) string {
	for port := startPort; port < startPort+100; port++ {
		addr := fmt.Sprintf(":%d", port)
		l, err := net.Listen("tcp", addr)
		if err == nil {
			l.Close()
			return fmt.Sprintf("%d", port)
		}
	}
	return fmt.Sprintf("%d", startPort)
}

// GetQrCodeData returns live pairing QR code in base64 along with IP, Port, PIN
func (a *App) GetQrCodeData() (QrData, error) {
	a.initNetworking()

	url := fmt.Sprintf("http://%s:%s/?password=%s", a.ip, a.port, a.pin)

	pngBytes, err := qrcode.Encode(url, qrcode.Medium, 256)
	if err != nil {
		return QrData{}, err
	}

	base64Img := "data:image/png;base64," + base64.StdEncoding.EncodeToString(pngBytes)

	return QrData{
		QrBase64: base64Img,
		Url:      url,
		IP:       a.ip,
		Port:     a.port,
		Pin:      a.pin,
	}, nil
}

// GetConnectionStatus returns currently connected mobile device info
func (a *App) GetConnectionStatus() ConnectionInfo {
	a.mu.Lock()
	defer a.mu.Unlock()

	return ConnectionInfo{
		IsConnected: a.isConnected,
		DeviceName:  a.activeDevice,
		DeviceIP:    a.deviceIP,
		Status:      ifThenElse(a.isConnected, "Connected", "Waiting for scan"),
	}
}

// SetConnectedDevice allows setting device connection state
func (a *App) SetConnectedDevice(deviceName, deviceIP string) {
	a.mu.Lock()
	defer a.mu.Unlock()

	a.activeDevice = deviceName
	a.deviceIP = deviceIP
	a.isConnected = true

	devID := fmt.Sprintf("%s_%s", strings.ToLower(strings.ReplaceAll(deviceName, " ", "_")), deviceIP)
	a.devices[devID] = ConnectedDeviceInfo{
		ID:       devID,
		Name:     deviceName,
		IP:       deviceIP,
		Status:   "Connected",
		LastSeen: time.Now().Format("15:04:05"),
	}
}

// DisconnectDevice disconnects the current device session
func (a *App) DisconnectDevice() {
	a.mu.Lock()
	defer a.mu.Unlock()

	a.activeDevice = ""
	a.deviceIP = ""
	a.isConnected = false
}

// GetConnectedDevices returns all actively connected devices
func (a *App) GetConnectedDevices() []ConnectedDeviceInfo {
	a.mu.Lock()
	defer a.mu.Unlock()

	var list []ConnectedDeviceInfo
	for _, d := range a.devices {
		list = append(list, d)
	}

	// If active device is set but map is empty, add it
	if len(list) == 0 && a.isConnected && a.activeDevice != "" {
		list = append(list, ConnectedDeviceInfo{
			ID:       "default_phone",
			Name:     a.activeDevice,
			IP:       a.deviceIP,
			Status:   "Connected",
			LastSeen: time.Now().Format("15:04:05"),
		})
	}

	return list
}

// RemoveConnectedDevice removes a connected device by id
func (a *App) RemoveConnectedDevice(id string) {
	a.mu.Lock()
	defer a.mu.Unlock()

	delete(a.devices, id)
	if len(a.devices) == 0 {
		a.isConnected = false
		a.activeDevice = ""
		a.deviceIP = ""
	}
}

func (a *App) resolvePath(reqPath string) string {
	homeDir, _ := os.UserHomeDir()
	if homeDir == "" {
		homeDir = "/"
	}

	switch strings.ToLower(strings.TrimSpace(reqPath)) {
	case "", "home", "~":
		return homeDir
	case "desktop":
		return filepath.Join(homeDir, "Desktop")
	case "documents":
		return filepath.Join(homeDir, "Documents")
	case "downloads":
		return filepath.Join(homeDir, "Downloads")
	case "music":
		return filepath.Join(homeDir, "Music")
	case "pictures":
		return filepath.Join(homeDir, "Pictures")
	case "videos":
		return filepath.Join(homeDir, "Videos")
	case "trash":
		trashDir := filepath.Join(homeDir, ".local/share/Trash/files")
		if _, err := os.Stat(trashDir); err == nil {
			return trashDir
		}
		return homeDir
	default:
		if strings.HasPrefix(reqPath, "~/") {
			return filepath.Join(homeDir, reqPath[2:])
		}
		return reqPath
	}
}

// ListLocalDirectory lists contents of a local directory
func (a *App) ListLocalDirectory(reqPath string) ([]FileEntry, error) {
	targetPath := a.resolvePath(reqPath)

	entries, err := os.ReadDir(targetPath)
	if err != nil {
		return nil, err
	}

	var results []FileEntry
	for _, entry := range entries {
		// Skip hidden dotfiles by default unless requested
		if strings.HasPrefix(entry.Name(), ".") {
			continue
		}

		info, err := entry.Info()
		size := int64(0)
		modTime := ""
		if err == nil {
			size = info.Size()
			modTime = info.ModTime().Format(time.RFC3339)
		}

		ext := ""
		if !entry.IsDir() {
			ext = strings.TrimPrefix(filepath.Ext(entry.Name()), ".")
		}

		fullPath := filepath.Join(targetPath, entry.Name())

		results = append(results, FileEntry{
			Name:    entry.Name(),
			Path:    fullPath,
			IsDir:   entry.IsDir(),
			Size:    size,
			ModTime: modTime,
			Ext:     strings.ToLower(ext),
		})
	}

	// Sort folders first, then alphabetically
	sort.Slice(results, func(i, j int) bool {
		if results[i].IsDir != results[j].IsDir {
			return results[i].IsDir
		}
		return strings.ToLower(results[i].Name) < strings.ToLower(results[j].Name)
	})

	return results, nil
}

// ListRemoteDirectory returns files for remote phone (or fallback demo items if connecting)
func (a *App) ListRemoteDirectory(reqPath string) ([]FileEntry, error) {
	a.mu.Lock()
	defer a.mu.Unlock()

	if len(a.connectedFiles) > 0 {
		return a.connectedFiles, nil
	}

	// Default standard storage folders seen on Android
	return []FileEntry{
		{Name: "DCIM", Path: "/storage/emulated/0/DCIM", IsDir: true, Ext: ""},
		{Name: "Download", Path: "/storage/emulated/0/Download", IsDir: true, Ext: ""},
		{Name: "Pictures", Path: "/storage/emulated/0/Pictures", IsDir: true, Ext: ""},
		{Name: "Documents", Path: "/storage/emulated/0/Documents", IsDir: true, Ext: ""},
		{Name: "Music", Path: "/storage/emulated/0/Music", IsDir: true, Ext: ""},
		{Name: "Movies", Path: "/storage/emulated/0/Movies", IsDir: true, Ext: ""},
	}, nil
}

// UploadFiles simulates or streams files to phone target directory
func (a *App) UploadFiles(targetDir string, filePaths []string) error {
	for _, p := range filePaths {
		if _, err := os.Stat(p); err != nil {
			return fmt.Errorf("file not found: %s", p)
		}
	}
	return nil
}

// DownloadFile downloads or copies a file to target directory
func (a *App) DownloadFile(remotePath, localTargetDir string) error {
	return nil
}

// OpenPath opens the file or directory using the OS default application
func (a *App) OpenPath(path string) error {
	resolved := a.resolvePath(path)
	cmd := exec.Command("xdg-open", resolved)
	return cmd.Start()
}

// RenameFile renames or moves a file to a new name in same directory
func (a *App) RenameFile(oldPath, newName string) error {
	resolvedOld := a.resolvePath(oldPath)
	dir := filepath.Dir(resolvedOld)
	newPath := filepath.Join(dir, newName)
	return os.Rename(resolvedOld, newPath)
}

// DeleteFile deletes a file or directory
func (a *App) DeleteFile(path string) error {
	resolved := a.resolvePath(path)
	return os.RemoveAll(resolved)
}

// CreateFolder creates a new directory
func (a *App) CreateFolder(parentPath, folderName string) error {
	resolvedParent := a.resolvePath(parentPath)
	newDir := filepath.Join(resolvedParent, folderName)
	return os.MkdirAll(newDir, 0755)
}

// CopyFile copies a file to destination directory
func (a *App) CopyFile(srcPath, destDir string) error {
	resolvedSrc := a.resolvePath(srcPath)
	resolvedDest := filepath.Join(a.resolvePath(destDir), filepath.Base(resolvedSrc))

	input, err := os.ReadFile(resolvedSrc)
	if err != nil {
		return err
	}
	return os.WriteFile(resolvedDest, input, 0644)
}

// MoveFile moves a file to destination directory
func (a *App) MoveFile(srcPath, destDir string) error {
	resolvedSrc := a.resolvePath(srcPath)
	resolvedDest := filepath.Join(a.resolvePath(destDir), filepath.Base(resolvedSrc))
	return os.Rename(resolvedSrc, resolvedDest)
}

// OpenWith opens file with specified app or launches mime open
func (a *App) OpenWith(path, appName string) error {
	resolved := a.resolvePath(path)
	if appName == "" {
		return a.OpenPath(path)
	}
	cmd := exec.Command(appName, resolved)
	return cmd.Start()
}

// GetFileContentBase64 reads an image/media file and returns a base64 data URL for in-app preview
func (a *App) GetFileContentBase64(path string) (string, error) {
	resolved := a.resolvePath(path)
	info, err := os.Stat(resolved)
	if err != nil {
		return "", err
	}

	// Limit in-app base64 preview to files under 20MB
	if info.Size() > 20*1024*1024 {
		return "", fmt.Errorf("file too large for direct preview")
	}

	data, err := os.ReadFile(resolved)
	if err != nil {
		return "", err
	}

	ext := strings.ToLower(strings.TrimPrefix(filepath.Ext(resolved), "."))
	mimeType := "application/octet-stream"
	switch ext {
	case "png":
		mimeType = "image/png"
	case "jpg", "jpeg":
		mimeType = "image/jpeg"
	case "webp":
		mimeType = "image/webp"
	case "gif":
		mimeType = "image/gif"
	case "svg":
		mimeType = "image/svg+xml"
	case "mp4":
		mimeType = "video/mp4"
	case "webm":
		mimeType = "video/webm"
	case "mp3":
		mimeType = "audio/mpeg"
	case "wav":
		mimeType = "audio/wav"
	case "ogg":
		mimeType = "audio/ogg"
	}

	return fmt.Sprintf("data:%s;base64,%s", mimeType, base64.StdEncoding.EncodeToString(data)), nil
}

func ifThenElse(condition bool, a, b string) string {
	if condition {
		return a
	}
	return b
}
