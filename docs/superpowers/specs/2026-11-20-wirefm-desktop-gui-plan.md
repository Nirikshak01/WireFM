# WireFM Desktop GUI File Manager Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a standalone, ultra-lightweight desktop GUI file manager for WireFM with an authentic iOS/macOS aesthetic (places/devices sidebar, instant QR pairing scanner, 3-column file grid, and drag-and-drop transfers) using Wails (Go + Vite/React/Tailwind) consuming under 40 MB RAM.

**Architecture:** A Wails v2 desktop architecture embedding the existing WireFM Go engine (`server/`), exposing high-performance Go methods via `desktop/app.go` directly to a lightweight React/Tailwind frontend styled with iOS-grade rounded components (`#0D0D11`), glassmorphism, and Lucide icons.

**Tech Stack:** Go 1.23+, Wails v2, WebKitGTK (Linux) / WebView2 (Windows) / WebKit (macOS), React, Vite, Tailwind CSS, Lucide Icons.

**Spec:** [`docs/superpowers/specs/2026-11-20-wirefm-desktop-gui-design.md`](file:///home/jasmin/Projects/wirefm/docs/superpowers/specs/2026-11-20-wirefm-desktop-gui-design.md)

---

## Global Constraints

- **Memory Limit**: Memory consumption must stay under 40 MB RAM idle (Electron is strictly prohibited).
- **Executable Footprint**: Single standalone binary under 20 MB without external web runtime dependencies.
- **Initial View**: Default screen on application launch is the QR Scanner card matching mockup `uploaded_media_1795157153856.png`.
- **Protocol Compatibility**: Zero breaking changes to the existing Android reverse-polling protocol (`/api/phone/poll`, `/api/phone/respond`, `/api/phone/stream_upload`).
- **Design Tokens**: AMOLED background `#0D0D11`, sidebar `#131418`, folder card `#CCD8DA`, accent blue border `#2F8BF0`, text `#FFFFFF` / `#8F8F9D`.

---

## Review Focus

1. **Port conflict on 8080/8081**: Dynamically probe and bind to next available port (e.g. 8082/8083), with QR code reflecting the actual bound port.
2. **Window drag-and-drop**: OS file drop must upload directly to the currently active directory with visual blue border highlight (`#2F8BF0`).
3. **Connection loss / unmount**: When a phone disconnects or heartbeat times out, UI gracefully transitions back to the Scanner card without crashing.
4. **Path & Filename overflow**: Long paths and names must truncate with ellipsis and show a hover tooltip.
5. **Offline PC browsing**: User can click "Places" (Home, Desktop, Downloads, etc.) in sidebar to browse local files even when no phone is connected.

---

### Task 1: Scaffold Desktop Project Structure & Go Wails Configuration

**Files:**
- Create: `desktop/wails.json`
- Create: `desktop/main.go`
- Create: `desktop/go.mod`
- Create: `desktop/build/appicon.png`

**Interfaces:**
- Produces: Wails project configuration with window bounds (1020x680, min 800x560), dark background `#0D0D11`, title "WireFM", and embedded assets.

- [ ] **Step 1: Create desktop module and initialize Wails configuration**
  Create `desktop/wails.json` with app metadata, frontend path `frontend/dist`, and build settings.
- [ ] **Step 2: Create desktop entrypoint `desktop/main.go`**
  Configure Wails application options (`wails.Run`) with `App` struct binding, window dimensions (1020x680), and title "WireFM".
- [ ] **Step 3: Setup Go module and dependencies**
  Configure `desktop/go.mod` linking to `../server` or reusing existing server packages.
- [ ] **Step 4: Verify Go build and module resolution**
  Run: `cd /home/jasmin/Projects/wirefm/desktop && go mod tidy`
  Expected: Success without unresolved dependencies.
- [ ] **Step 5: Commit**
  Run: `git add desktop/ && git commit -m "chore(desktop): scaffold Wails project structure and entrypoint"`

---

### Task 2: Implement Desktop Backend Go Service & Bridge (`app.go`)

**Files:**
- Create: `desktop/app.go`
- Create: `desktop/app_test.go`
- Reference: `server/main.go`

**Interfaces:**
- Produces:
  - `GetQrCodeData() (QrData, error)`: returns base64 PNG QR code, IP, Port, PIN.
  - `GetConnectionStatus() ConnectionInfo`: returns connected state, device name, battery, IP.
  - `ListLocalDirectory(path string) ([]FileInfo, error)`: lists local PC directories.
  - `ListRemoteDirectory(path string) ([]FileInfo, error)`: lists phone directories via reverse-polling command queue.
  - `UploadFiles(targetDir string, filePaths []string) error`: transfers files to phone.
  - `DownloadFile(remotePath, localTargetDir string) error`: downloads file from phone to PC.

- [ ] **Step 1: Write backend unit test for local directory listing & QR generation**
  Create `desktop/app_test.go` testing QR data generation and local folder listing.
- [ ] **Step 2: Run test to verify initial state**
  Run: `cd /home/jasmin/Projects/wirefm/desktop && go test -v`
- [ ] **Step 3: Implement `App` struct and bindings in `desktop/app.go`**
  Embed the WireFM server engine, handle dynamic port allocation (8080 fallback), and expose Wails callable methods.
- [ ] **Step 4: Run test to verify all tests pass**
  Run: `cd /home/jasmin/Projects/wirefm/desktop && go test -v`
  Expected: PASS
- [ ] **Step 5: Commit**
  Run: `git add desktop/app.go desktop/app_test.go && git commit -m "feat(desktop): implement Go backend service methods and QR generator"`

---

### Task 3: Initialize Vite + React + Tailwind Frontend

**Files:**
- Create: `desktop/frontend/package.json`
- Create: `desktop/frontend/vite.config.ts`
- Create: `desktop/frontend/tailwind.config.js`
- Create: `desktop/frontend/src/index.css`
- Create: `desktop/frontend/src/main.tsx`

**Interfaces:**
- Produces: Fast buildable frontend bundle with Tailwind typography, custom AMOLED color tokens (`#0D0D11`, `#131418`, `#CCD8DA`, `#2F8BF0`), and Lucide icons.

- [ ] **Step 1: Create `package.json` and install dependencies**
  Configure React 18/19, Vite, Tailwind CSS, Lucide React, and clsx.
  Run: `cd /home/jasmin/Projects/wirefm/desktop/frontend && pnpm install`
- [ ] **Step 2: Configure `tailwind.config.js` with WireFM color palette**
  Add `brand-dark: #0D0D11`, `brand-sidebar: #131418`, `brand-folder: #CCD8DA`, `brand-blue: #2F8BF0`.
- [ ] **Step 3: Create base `index.css` and font styling**
  Add custom scrollbar styling, smooth font rendering, and selection colors.
- [ ] **Step 4: Verify frontend build compiles cleanly**
  Run: `cd /home/jasmin/Projects/wirefm/desktop/frontend && pnpm run build`
  Expected: Build finishes with `dist/` created.
- [ ] **Step 5: Commit**
  Run: `git add desktop/frontend/ && git commit -m "feat(desktop): configure Vite, Tailwind, and React frontend pipeline"`

---

### Task 4: Build Sidebar Component (Places & Devices)

**Files:**
- Create: `desktop/frontend/src/components/Sidebar.tsx`
- Modify: `desktop/frontend/src/App.tsx`

**Interfaces:**
- Consumes: Active route / folder selection state, connected device info.
- Produces: Sidebar matching mockup with WireFM pill logo, Places section (Home, Desktop, Documents, Downloads, Music, Pictures, Videos, Trash), and Devices section (`Vivo y36` / connected phone).

- [ ] **Step 1: Create `Sidebar.tsx` with Places and Devices sections**
  Implement rounded pill items (`#25272C` on active), hover animations, Lucide icons, and live green connection dot on devices.
- [ ] **Step 2: Wire sidebar selection handler in `App.tsx`**
  Support selecting Places (PC storage) and Devices (phone storage / scanner).
- [ ] **Step 3: Verify component renders without console errors**
  Run: `cd /home/jasmin/Projects/wirefm/desktop/frontend && pnpm run build`
- [ ] **Step 4: Commit**
  Run: `git add desktop/frontend/src/components/Sidebar.tsx desktop/frontend/src/App.tsx && git commit -m "feat(desktop): build iOS-style Places and Devices sidebar"`

---

### Task 5: Build Instant QR Scanner Card Component

**Files:**
- Create: `desktop/frontend/src/components/ScannerCard.tsx`
- Modify: `desktop/frontend/src/App.tsx`

**Interfaces:**
- Consumes: Base64 QR code, PIN, connection URL from Go backend.
- Produces: Centered rounded card with QR code, "Connect with WireFM Scan with your Mobile" subtitle, and WireFM branding pill badge matching the user's mockup.

- [ ] **Step 1: Create `ScannerCard.tsx` matching mockup `uploaded_media_1795157153856.png`**
  Center card with `#CCD8DA` or soft neutral container, QR code display, subtitle with info icon, and WireFM bottom pill badge.
- [ ] **Step 2: Add quick-copy button for PIN and local IP**
  Enable 1-click clipboard copy for manual connection fallback.
- [ ] **Step 3: Integrate into `App.tsx` as default launch view**
  When app opens or no phone is paired, main stage renders `ScannerCard`.
- [ ] **Step 4: Verify build**
  Run: `cd /home/jasmin/Projects/wirefm/desktop/frontend && pnpm run build`
- [ ] **Step 5: Commit**
  Run: `git add desktop/frontend/src/components/ScannerCard.tsx desktop/frontend/src/App.tsx && git commit -m "feat(desktop): build default QR pairing scanner card matching mockup"`

---

### Task 6: Build 3-Column File Browser & Breadcrumb Component

**Files:**
- Create: `desktop/frontend/src/components/FileBrowser.tsx`
- Create: `desktop/frontend/src/components/BreadcrumbBar.tsx`
- Modify: `desktop/frontend/src/App.tsx`

**Interfaces:**
- Consumes: Current path, list of `FileInfo` items, view mode (grid/list).
- Produces: 3-column grid of `#CCD8DA` tabbed folder cards with bold titles, rich color-coded file icons (code, image, media, documents), and clickable breadcrumb navigation.

- [ ] **Step 1: Create `BreadcrumbBar.tsx`**
  Implement `root > home > jasmin` clickable breadcrumb chips with parent directory jump.
- [ ] **Step 2: Create `FileBrowser.tsx` with 3-column folder/file grid**
  Folder cards with `#CCD8DA` tab shape and dark text; file cards with previews; double-click to enter folder or open file.
- [ ] **Step 3: Wire search filter and view toggle**
  Filter files dynamically as user types in search bar.
- [ ] **Step 4: Test build**
  Run: `cd /home/jasmin/Projects/wirefm/desktop/frontend && pnpm run build`
- [ ] **Step 5: Commit**
  Run: `git add desktop/frontend/src/components/FileBrowser.tsx desktop/frontend/src/components/BreadcrumbBar.tsx && git commit -m "feat(desktop): build 3-column file grid and breadcrumbs"`

---

### Task 7: Build Drag-and-Drop Drop-Zone & Context Actions

**Files:**
- Create: `desktop/frontend/src/components/DropOverlay.tsx`
- Create: `desktop/frontend/src/components/ContextMenu.tsx`
- Modify: `desktop/frontend/src/components/FileBrowser.tsx`

**Interfaces:**
- Consumes: Wails `OnFileDrop` event and right-click mouse events.
- Produces: Full-window drop-zone overlay with `#2F8BF0` dashed border and right-click menu (Download, Copy, Cut, Rename, Delete).

- [ ] **Step 1: Implement `DropOverlay.tsx`**
  Visual indicator active during file drag over the desktop window with "Drop files to upload to [Folder Name]".
- [ ] **Step 2: Implement `ContextMenu.tsx`**
  Custom floating menu with blue border `#2F8BF0` (Download to PC, Rename, Delete, Copy).
- [ ] **Step 3: Wire Go backend file drop and action handlers**
  Connect frontend drop and context actions to `UploadFiles` and `DownloadFile` in Go backend.
- [ ] **Step 4: Verify build**
  Run: `cd /home/jasmin/Projects/wirefm/desktop/frontend && pnpm run build`
- [ ] **Step 5: Commit**
  Run: `git add desktop/frontend/src/components/DropOverlay.tsx desktop/frontend/src/components/ContextMenu.tsx && git commit -m "feat(desktop): implement drag-and-drop file upload and context menu"`

---

### Task 8: End-to-End Build & Memory/Performance Verification

**Files:**
- Create: `desktop/build.sh`
- Modify: `.github/workflows/build.yml`

**Interfaces:**
- Produces: Standalone binary `wirefm-desktop` (< 20 MB size, < 40 MB RAM idle).

- [ ] **Step 1: Create local build script `desktop/build.sh`**
  Compiles frontend and builds standalone desktop binary using `wails build` or Go build.
- [ ] **Step 2: Update GitHub Actions workflow to build Desktop GUI alongside Android and Linux CLI**
  Add `build-desktop` job producing `wirefm-desktop-linux-amd64` release artifact.
- [ ] **Step 3: Execute build and verify binary footprint**
  Run: `chmod +x desktop/build.sh && ./desktop/build.sh`
  Verify size < 20 MB and startup time < 0.5s.
- [ ] **Step 4: Commit and finalize**
  Run: `git add . && git commit -m "ci: add desktop GUI build pipeline to GitHub Actions"`
