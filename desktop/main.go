package main

import (
	"embed"
	"os"
	"syscall"

	"github.com/wailsapp/wails/v2"
	"github.com/wailsapp/wails/v2/pkg/options"
	"github.com/wailsapp/wails/v2/pkg/options/assetserver"
	"github.com/wailsapp/wails/v2/pkg/options/linux"
)

func init() {
	// WebKitGTK on Linux with Intel Haswell (Mesa crocus) or NVIDIA fails Wayland DMA-BUF compositing.
	// Running under XWayland via GDK_BACKEND=x11 fixes the black screen across all Wayland compositors (Hyprland, Sway, GNOME).
	// Re-exec with GDK_BACKEND=x11 before GTK initializes if not already set.
	if os.Getenv("WIREFM_GDK_SET") == "" {
		os.Setenv("WIREFM_GDK_SET", "1")
		os.Setenv("GDK_BACKEND", "x11")
		os.Setenv("WEBKIT_DISABLE_DMABUF_RENDERER", "1")
		os.Setenv("WEBKIT_DISABLE_COMPOSITING_MODE", "1")
		exe, err := os.Executable()
		if err == nil {
			syscall.Exec(exe, os.Args, os.Environ())
		}
	}
}

//go:embed all:frontend/dist
var assets embed.FS

//go:embed appicon.png
var icon []byte

func main() {
	app := NewApp()

	err := wails.Run(&options.App{
		Title:             "WireFM",
		Width:             1020,
		Height:            680,
		MinWidth:          800,
		MinHeight:         560,
		BackgroundColour:  &options.RGBA{R: 13, G: 13, B: 17, A: 255}, // #0D0D11
		AssetServer: &assetserver.Options{
			Assets: assets,
		},
		OnStartup:  app.startup,
		OnShutdown: app.shutdown,
		Bind: []interface{}{
			app,
		},
		Linux: &linux.Options{
			Icon:                icon,
			WindowIsTranslucent: false,
			WebviewGpuPolicy:    linux.WebviewGpuPolicyOnDemand,
		},
	})

	if err != nil {
		println("Error:", err.Error())
	}
}
