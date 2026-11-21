#!/usr/bin/env bash
# ==============================================================================
# WireFM One-Command Installer (Standalone Desktop GUI)
# Usage: curl -sSL https://raw.githubusercontent.com/Nirikshak01/WireFM/main/install.sh | bash
# ==============================================================================

set -e

REPO_OWNER="Nirikshak01"
REPO_NAME="WireFM"
RELEASE_BASE="https://github.com/${REPO_OWNER}/${REPO_NAME}/releases/latest/download"
RAW_BASE="https://raw.githubusercontent.com/${REPO_OWNER}/${REPO_NAME}/main"

echo -e "\033[36m"
echo "  ██╗    ██╗██╗██████╗ ███████╗███████╗███╗   ███╗"
echo "  ██║    ██║██║██╔══██╗██╔════╝██╔════╝████╗ ████║"
echo "  ██║ █╗ ██║██║██████╔╝█████╗  █████╗  ██╔████╔██║"
echo "  ██║███╗██║██║██╔══██╗██╔══╝  ██╔══╝  ██║╚██╔╝██║"
echo "  ╚███╔███╔╝██║██║  ██║███████╗██║     ██║ ╚═╝ ██║"
echo "   ╚══╝╚══╝ ╚═╝╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝     ╚═╝"
echo -e "\033[0m"
echo "  Installing WireFM Desktop GUI..."
echo "─────────────────────────────────────────────────────────────"

INSTALL_BIN="$HOME/.local/bin"
APP_DIR="$HOME/.local/share/applications"
ICON_DIR="$HOME/.local/share/icons/hicolor/512x512/apps"
ICON_DIR_SCALABLE="$HOME/.local/share/icons/hicolor/scalable/apps"
PIXMAPS_DIR="$HOME/.local/share/pixmaps"

mkdir -p "$INSTALL_BIN" "$APP_DIR" "$ICON_DIR" "$ICON_DIR_SCALABLE" "$PIXMAPS_DIR"

# Clean up any legacy CLI binary
if [ -f "$INSTALL_BIN/wirefm" ]; then
    rm -f "$INSTALL_BIN/wirefm" 2>/dev/null || true
fi

# 1. Download WireFM Desktop GUI Binary & create GPU-safe wrapper
echo "📦 Downloading WireFM Desktop GUI..."
if ! curl -sL --fail "${RELEASE_BASE}/wirefm-desktop-linux-amd64" -o "$INSTALL_BIN/wirefm-desktop-bin"; then
    echo "⚠️  Remote download failed, checking local binary..."
    if [ -f "$HOME/wirefm-desktop" ]; then
        cp "$HOME/wirefm-desktop" "$INSTALL_BIN/wirefm-desktop-bin"
    elif [ -f "$HOME/.local/bin/wirefm-desktop" ] && [ ! -f "$INSTALL_BIN/wirefm-desktop-bin" ]; then
        cp "$HOME/.local/bin/wirefm-desktop" "$INSTALL_BIN/wirefm-desktop-bin"
    fi
fi
chmod +x "$INSTALL_BIN/wirefm-desktop-bin" 2>/dev/null || true

# Also sync to ~/wirefm-desktop if user runs it directly from home
if [ -f "$INSTALL_BIN/wirefm-desktop-bin" ]; then
    cp "$INSTALL_BIN/wirefm-desktop-bin" "$HOME/wirefm-desktop" 2>/dev/null || true
    chmod +x "$HOME/wirefm-desktop" 2>/dev/null || true
fi

# Create executable wrapper script for wirefm-desktop
cat > "$INSTALL_BIN/wirefm-desktop" << 'EOF'
#!/usr/bin/env bash
export WEBKIT_DISABLE_DMABUF_RENDERER=1
BIN_PATH="$HOME/.local/bin/wirefm-desktop-bin"
if [ ! -f "$BIN_PATH" ]; then
    BIN_PATH="$HOME/wirefm-desktop"
fi
exec "$BIN_PATH" "$@"
EOF
chmod +x "$INSTALL_BIN/wirefm-desktop" 2>/dev/null || true

# 2. Install App Icon
echo "🎨 Installing Official WireFM App Icons..."
if ! curl -sL --fail "${RAW_BASE}/desktop/appicon.png" -o "$ICON_DIR/wirefm.png"; then
    if [ -f "/home/jasmin/Projects/wirefm/desktop/appicon.png" ]; then
        cp "/home/jasmin/Projects/wirefm/desktop/appicon.png" "$ICON_DIR/wirefm.png"
    fi
fi
cp "$ICON_DIR/wirefm.png" "$PIXMAPS_DIR/wirefm.png" 2>/dev/null || true
curl -sL --fail "${RAW_BASE}/Assets/wireframe-layer-dark-bg-redus.svg" -o "$ICON_DIR_SCALABLE/wirefm.svg" 2>/dev/null || true

# 3. Create Desktop Entry in Applications Menu
echo "🚀 Creating Linux Application Menu Launcher..."
cat > "$APP_DIR/wirefm.desktop" << 'EOF'
[Desktop Entry]
Version=1.0
Type=Application
Name=WireFM
GenericName=Wireless File Manager
Comment=Ultra-fast wireless file sharing and desktop manager
Exec=env WEBKIT_DISABLE_DMABUF_RENDERER=1 wirefm-desktop %U
Icon=wirefm
Terminal=false
Categories=Utility;FileManager;Network;FileTransfer;
StartupWMClass=wirefm-desktop
Keywords=wirefm;file;manager;wireless;transfer;wifi;share;phone;android;
EOF
chmod +x "$APP_DIR/wirefm.desktop"

# Refresh desktop & icon databases
update-desktop-database "$APP_DIR" 2>/dev/null || true
gtk-update-icon-cache -f -t "$HOME/.local/share/icons/hicolor" 2>/dev/null || true

# 4. Ensure ~/.local/bin is in PATH
if [[ ":$PATH:" != *":$INSTALL_BIN:"* ]]; then
    echo "export PATH=\"\$PATH:$INSTALL_BIN\"" >> ~/.bashrc 2>/dev/null || true
    [ -f ~/.zshrc ] && echo "export PATH=\"\$PATH:$INSTALL_BIN\"" >> ~/.zshrc 2>/dev/null || true
    if [ -d "$HOME/.config/fish" ]; then
        mkdir -p "$HOME/.config/fish"
        touch "$HOME/.config/fish/config.fish"
        echo "fish_add_path $INSTALL_BIN" >> "$HOME/.config/fish/config.fish" 2>/dev/null || true
    fi
fi

echo "─────────────────────────────────────────────────────────────"
echo "✅ WireFM Desktop installed successfully!"
echo "✨ WireFM is now available in your Application Menu (with logo)!"
echo "👉 Run from App Menu (Rofi/Wofi/Menu) or terminal: wirefm-desktop"
