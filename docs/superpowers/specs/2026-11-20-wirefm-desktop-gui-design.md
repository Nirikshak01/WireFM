# WireFM Desktop GUI File Manager Design Specification

**Date:** 2026-11-20  
**Project:** WireFM Desktop App  
**Status:** In Review  
**Target Architecture:** Linux (x86_64 / arm64), macOS, Windows  

---

## 1. Intent & The Why

WireFM is currently a high-performance, zero-setup wireless file sharing tool between Linux and Android with a headless Go server and a terminal/browser QR code. While the Android app is now polished with a modern Material 3 Jetpack Compose UI, the PC side currently relies on terminal CLI or browser tab.

The goal is to build a **standalone, ultra-lightweight desktop GUI file manager** that feels like an authentic native desktop app with **iOS/macOS-level component polish** (pill badges, fluid spring transitions, dark glassmorphism, responsive sidebar, drag-and-drop transfers) while consuming minimal system resources.

---

## 2. Goals, Non-Goals & Anti-Goals

### Goals:
- 🪶 **Ultra-Lightweight**: Consumes < 40 MB RAM (unlike Electron apps which consume 200+ MB). Single executable binary (~15 MB).
- 🎨 **iOS/macOS Aesthetic**: Deep pitch/AMOLED dark theme (`#0D0D11`), smooth rounded containers (16-24px radius), subtle borders (`#1E2024`), vibrant status badges, and SF/Inter typography.
- 📱 **Sidebar with Places & Devices**:
  - **Places**: Quick links to local folders (`Home`, `Desktop`, `Documents`, `Downloads`, `Music`, `Pictures`, `Videos`, `Trash`).
  - **Devices**: Connected mobile devices (e.g. `Vivo y36` / `Vivo V2247`) with live Wi-Fi connection indicator.
- ⚡ **Zero-Latency QR Pairing**:
  - Live QR code rendered directly inside the desktop window on startup.
  - Immediate pairing feedback when Android scans the QR code.
- 📂 **Bidirectional File Management**:
  - Browse phone storage and local PC storage side-by-side or tabbed.
  - Native Drag-and-Drop: drop files from Linux desktop directly into the WireFM window to push to phone.
  - Fast file actions: Copy, Cut, Paste, Rename, Delete, and Open in system file manager / viewer.

### Non-Goals:
- Cloud syncing or internet relay (WireFM remains 100% offline and peer-to-peer over local Wi-Fi).
- Heavy Chromium bundling (Electron is strictly avoided).

### Anti-Goals:
- Bloated memory footprint: If the idle desktop app consumes > 60 MB RAM, that is considered a defect.
- Stiff, web-like UI: The interface must feel like a native desktop operating system component, not a generic website inside a frame.

---

## 3. Approaches Considered & Trade-offs

| Approach | Binary Size | RAM Usage | UI Polish / Component Speed | Decision |
| :--- | :---: | :---: | :---: | :--- |
| **Option 1: Wails (Go + Native WebKit + React/Tailwind)** | ~15 MB | ~35 MB | **Extreme** (full CSS spring physics, iOS-grade blurs, Lucide icons, instant reactivity) | **SELECTED (User Choice)**: Reuses existing Go server code directly. Zero glue layers. |
| **Option 2: Tauri v2 (Rust + Native Webview)** | ~12 MB | ~30 MB | Extreme | Requires rewriting Go server networking and WebDAV in Rust. |
| **Option 3: Pure Native Slint / GPUI (Rust)** | ~12 MB | ~20 MB | Moderate | No webview, but building custom iOS-like pills, blur effects, and rich file icons requires extensive low-level canvas code. |
| **Option 4: Electron (Chromium + Node.js)** | ~150 MB | ~220 MB | High | **Rejected**: Too heavy and violates user requirement. |

---

## 4. UI Architecture & Visual Layout (Based on User Mockup)

```
┌────────────────────────────────────────────────────────────────────────┐
│  [Window Controls: Close, Min, Max]                             WireFM │
├─────────────────────────┬──────────────────────────────────────────────┤
│ 🏷️ [ WireFM ]           │                                              │
│                         │                                              │
│ Places                  │                 ┌───────────────┐            │
│  🏠 Home (Active)       │                 │               │            │
│  🖥️ Desktop             │                 │    SCANNER    │            │
│  📄 Documents           │                 │    QR CODE    │            │
│  ⬇️ Downloads           │                 │               │            │
│  🎵 Music               │                 └───────────────┘            │
│  🖼️ Pictures            │                                              │
│  🎬 Videos              │       ℹ️ Connect with WireFM Scan with       │
│  🗑️ Trash               │                   your Mobile                │
│                         │                                              │
│ Devices                 │                 🏷️ [ WireFM ]                │
│  📱 Vivo y36 (Connected)│                                              │
│                         │                                              │
├─────────────────────────┴──────────────────────────────────────────────┤
│ Status: Wi-Fi 192.168.1.10:8080 • Port 8081 WebDAV • 1 Device Active   │
└────────────────────────────────────────────────────────────────────────┘
```

### 1. Left Sidebar:
- **Top Brand Lockup**: WireFM tile logo pill with rounded 12px corners.
- **Section: Places**:
  - `Home`, `Desktop`, `Documents`, `Downloads`, `Music`, `Pictures`, `Videos`, `Trash`.
  - Active item: highlighted dark grey pill `#25272C`, white text.
  - Inactive item: transparent background with subtle hover `#1A1B20`.
- **Section: Devices**:
  - Automatically detected connected phones (e.g. `Vivo y36`).
  - Active connection indicator dot (green `#10B981`) and device battery/signal if available.

### 2. Main Content Area:
- **Unconnected State (Pairing View)**:
  - Big centered rounded card (`#CCD8DA` or soft neutral container) with the high-resolution QR code.
  - Subtitle: `Connect with WireFM Scan with your Mobile`.
  - WireFM branding badge.
  - One-click copy PIN / IP for manual connection.
- **Connected State (File Browser View)**:
  - Header: Breadcrumbs (`root > storage > emulated > 0`), Search bar with shortcut (`Ctrl+F`), View Mode (3-column Grid / Compact List).
  - 3-Column Grid matching mobile Figma look: tabbed folders in `#CCD8DA`, file type badges with color accents, drag-and-drop drop-zone with blue border highlight (`#2F8BF0`).
  - Context menu on right-click: Open, Download, Copy, Cut, Delete, Properties.

---

## 5. Technical Implementation Details

### Protocol & Synchronization (Decisions Settled):
- **Communication Protocol**: Directly uses WireFM's existing Go reverse-polling REST protocol (`/api/phone/poll`, `/api/phone/respond`, `/api/phone/stream_upload`). No breaking changes to the Android app.
- **View Navigation Model**: Single-pane explorer with unified sidebar (macOS Finder style). Clicking "Places" browses local PC storage; clicking "Devices" browses remote phone storage.
- **Pairing View**: When no phone is connected, selecting "Devices" displays the centered QR Scanner card. As soon as the phone scans the QR code, the view automatically transitions to the phone's file explorer.
- **Port Management**: Default ports are 8080 (HTTP) and 8081 (WebDAV). If ports are occupied, the Go engine automatically probes and binds to the next available ports (e.g. 8082/8083) and generates the QR code with the dynamically bound port.
- **Drag-and-Drop**: Global window drop-zone powered by Wails `runtime.OnFileDrop`. Dragging files from the desktop onto the window uploads them directly to the active folder in view, with a full-window blue drop-overlay (`#2F8BF0`).
- **Session Concurrency**: 1 active mobile session at a time, matching WireFM's secure 1-to-1 sync model.

### Backend (Go / Wails):
- Integrate with existing `server/` codebase (`server/main.go`, `server/webdav/`).
- App struct exposed to Wails frontend:
  - `GetQrCodeData() (string, error)`: Generates and returns base64 QR code PNG and connection URL.
  - `GetConnectionStatus() ConnectionInfo`: Returns active device, IP, port, PIN.
  - `ListLocalDirectory(path string) ([]FileInfo, error)`: Lists local folders (Home, Desktop, etc.).
  - `ListRemoteDirectory(path string) ([]FileInfo, error)`: Lists connected phone files via internal reverse-polling engine.
  - `UploadFiles(targetDir string, filePaths []string) error`: Uploads files to phone.
  - `DownloadFile(remotePath, localTargetDir string) error`: Downloads file from phone.

### Frontend (Tailwind + Lucide + Vite):
- Svelte 5 or React + Vite.
- Tailwind CSS with exact WireFM AMOLED palette:
  - Background: `#0D0D11` / `#000000`
  - Sidebar: `#131418`
  - Card/Folder: `#CCD8DA` (Folder Tab shape)
  - Accent / Border: `#2F8BF0` (WireFM Blue)
  - Text: `#FFFFFF` / `#8F8F9D`
- Lucide Icons for clean iOS/macOS look.
- Fast spring animations with CSS cubic-bezier / Framer Motion.

---

## 6. Verification & Milestones

1. **Milestone 1**: Initialize Wails project structure with Go backend bindings.
2. **Milestone 2**: Build Sidebar (Places & Devices) and Pairing Scanner card matching the user's mockup image.
3. **Milestone 3**: Connect live QR generation and real-time device connection listener from the Go server.
4. **Milestone 4**: Build the 3-column file grid & drag-and-drop file upload engine.
5. **Milestone 5**: Compile single binary `wirefm-desktop` and verify RAM usage (< 40 MB).
