#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== 1. Building WireFM Desktop Frontend ==="
cd frontend
if command -v pnpm &> /dev/null; then
    pnpm install
    pnpm run build
elif command -v npm &> /dev/null; then
    npm install
    npm run build
fi
cd ..

echo "=== 2. Building WireFM Desktop Linux Binary ==="
mkdir -p ../dist
if command -v wails &> /dev/null; then
    wails build -platform linux/amd64 -ldflags="-s -w" -o wirefm-desktop
    cp build/bin/wirefm-desktop ../dist/wirefm-desktop-linux-amd64
else
    echo "Compiling via Go build..."
    go build -ldflags="-s -w" -o ../dist/wirefm-desktop-linux-amd64 .
fi

echo "=== WireFM Desktop Build Finished ==="
