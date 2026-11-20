package main

import (
	"os"
	"strings"
	"testing"
)

func TestApp_GetQrCodeData(t *testing.T) {
	app := NewApp()
	data, err := app.GetQrCodeData()
	if err != nil {
		t.Fatalf("GetQrCodeData failed: %v", err)
	}

	if !strings.HasPrefix(data.QrBase64, "data:image/png;base64,") {
		t.Errorf("expected base64 PNG data URL, got: %s", data.QrBase64[:30])
	}

	if data.IP == "" {
		t.Errorf("expected non-empty IP")
	}

	if data.Port == "" {
		t.Errorf("expected non-empty Port")
	}

	if len(data.Pin) != 6 {
		t.Errorf("expected 6-digit PIN, got: %s", data.Pin)
	}
}

func TestApp_ListLocalDirectory(t *testing.T) {
	app := NewApp()

	// List home or current directory
	entries, err := app.ListLocalDirectory(".")
	if err != nil {
		t.Fatalf("ListLocalDirectory failed: %v", err)
	}

	if len(entries) == 0 {
		t.Errorf("expected non-empty entries for current directory")
	}

	foundAppGo := false
	for _, e := range entries {
		if e.Name == "app.go" {
			foundAppGo = true
			if e.IsDir {
				t.Errorf("app.go should not be a directory")
			}
			if e.Ext != "go" {
				t.Errorf("expected ext 'go', got: %s", e.Ext)
			}
		}
	}

	if !foundAppGo {
		t.Errorf("expected to find app.go in current directory")
	}
}

func TestApp_ResolveSpecialPath(t *testing.T) {
	app := NewApp()
	homeDir, err := os.UserHomeDir()
	if err != nil {
		t.Skip("User home dir unavailable")
	}

	resolved := app.resolvePath("home")
	if resolved != homeDir {
		t.Errorf("expected %s, got %s", homeDir, resolved)
	}
}
