import React, { useState, useEffect, useCallback, useMemo } from 'react'
import { Sidebar } from './components/Sidebar'
import { ScannerCard } from './components/ScannerCard'
import { BreadcrumbBar } from './components/BreadcrumbBar'
import { FileBrowser } from './components/FileBrowser'
import { DropOverlay } from './components/DropOverlay'
import { ContextMenu } from './components/ContextMenu'
import { WailsBridge } from './services/wailsBridge'
import { Place, ViewMode, QrData, ConnectionInfo, FileEntry } from './types'

export default function App() {
  // App navigation state: defaults to 'scanner' per user requirement
  const [activePlace, setActivePlace] = useState<Place | 'device' | 'scanner'>('scanner')
  const [currentPath, setCurrentPath] = useState<string>('~')
  const [pathHistory, setPathHistory] = useState<string[]>([])

  // Pairing & Connection state
  const [qrData, setQrData] = useState<QrData | null>(null)
  const [isQrLoading, setIsQrLoading] = useState<boolean>(true)
  const [connectionInfo, setConnectionInfo] = useState<ConnectionInfo>({
    is_connected: false,
    device_name: 'Vivo y36',
    device_ip: '',
    status: 'Waiting for scan',
  })

  // File browser state
  const [files, setFiles] = useState<FileEntry[]>([])
  const [isFilesLoading, setIsFilesLoading] = useState<boolean>(false)
  const [searchQuery, setSearchQuery] = useState<string>('')
  const [viewMode, setViewMode] = useState<ViewMode>('grid')

  // Drag and drop state
  const [isDragging, setIsDragging] = useState<boolean>(false)

  // Context menu state
  const [contextMenu, setContextMenu] = useState<{
    x: number
    y: number
    file: FileEntry
  } | null>(null)

  // Toast notification
  const [toast, setToast] = useState<string | null>(null)

  const showToast = useCallback((msg: string) => {
    setToast(msg)
    setTimeout(() => setToast(null), 3000)
  }, [])

  // Load QR Code on startup
  const loadQrData = useCallback(async () => {
    setIsQrLoading(true)
    try {
      const data = await WailsBridge.getQrCodeData()
      setQrData(data)
    } catch (err) {
      console.error('Failed to load QR code data:', err)
    } finally {
      setIsQrLoading(false)
    }
  }, [])

  // Poll connection status
  const checkConnection = useCallback(async () => {
    try {
      const status = await WailsBridge.getConnectionStatus()
      setConnectionInfo(status)

      // Auto-transition to phone device when newly connected
      if (status.is_connected && activePlace === 'scanner') {
        setActivePlace('device')
        setCurrentPath('/storage/emulated/0')
      }
    } catch (err) {
      console.error('Failed to check connection:', err)
    }
  }, [activePlace])

  useEffect(() => {
    loadQrData()
    checkConnection()

    const interval = setInterval(() => {
      checkConnection()
    }, 2500)

    return () => clearInterval(interval)
  }, [loadQrData, checkConnection])

  // Load directory files
  const loadDirectory = useCallback(
    async (path: string, isRemote: boolean) => {
      setIsFilesLoading(true)
      try {
        let entries: FileEntry[] = []
        if (isRemote) {
          entries = await WailsBridge.listRemoteDirectory(path)
        } else {
          entries = await WailsBridge.listLocalDirectory(path)
        }
        setFiles(entries)
      } catch (err) {
        console.error('Failed to list directory:', err)
        showToast('Failed to open directory')
      } finally {
        setIsFilesLoading(false)
      }
    },
    [showToast]
  )

  // Navigate to Place
  const handleSelectPlace = useCallback(
    (place: Place) => {
      setActivePlace(place)
      const targetPath = place === 'home' ? '~' : place
      setPathHistory((prev) => [...prev, currentPath])
      setCurrentPath(targetPath)
      loadDirectory(targetPath, false)
    },
    [currentPath, loadDirectory]
  )

  // Select Device
  const handleSelectDevice = useCallback(() => {
    setActivePlace('device')
    const phoneRoot = '/storage/emulated/0'
    setPathHistory((prev) => [...prev, currentPath])
    setCurrentPath(phoneRoot)
    loadDirectory(phoneRoot, true)
  }, [currentPath, loadDirectory])

  // Open Folder
  const handleOpenFolder = useCallback(
    (folder: FileEntry) => {
      setPathHistory((prev) => [...prev, currentPath])
      setCurrentPath(folder.path)
      loadDirectory(folder.path, activePlace === 'device')
    },
    [currentPath, activePlace, loadDirectory]
  )

  // Open File
  const handleOpenFile = useCallback(
    async (file: FileEntry) => {
      if (activePlace === 'device') {
        showToast(`Downloading ${file.name} to PC...`)
        await WailsBridge.downloadFile(file.path, 'downloads')
        showToast(`Saved ${file.name} to Downloads`)
      } else {
        await WailsBridge.openPath(file.path)
      }
    },
    [activePlace, showToast]
  )

  // Back Navigation
  const handleGoBack = useCallback(() => {
    if (pathHistory.length === 0) return
    const prevPath = pathHistory[pathHistory.length - 1]
    setPathHistory((prev) => prev.slice(0, -1))
    setCurrentPath(prevPath)
    loadDirectory(prevPath, activePlace === 'device')
  }, [pathHistory, activePlace, loadDirectory])

  // Direct Path Navigate
  const handleNavigatePath = useCallback(
    (path: string) => {
      setPathHistory((prev) => [...prev, currentPath])
      setCurrentPath(path)
      loadDirectory(path, activePlace === 'device')
    },
    [currentPath, activePlace, loadDirectory]
  )

  // Refresh
  const handleRefresh = useCallback(() => {
    if (activePlace === 'scanner') {
      loadQrData()
    } else {
      loadDirectory(currentPath, activePlace === 'device')
    }
  }, [activePlace, currentPath, loadQrData, loadDirectory])

  // Filtered files by search
  const filteredFiles = useMemo(() => {
    if (!searchQuery.trim()) return files
    const q = searchQuery.toLowerCase()
    return files.filter((f) => f.name.toLowerCase().includes(q))
  }, [files, searchQuery])

  // Drag and Drop listeners
  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault()
    setIsDragging(true)
  }, [])

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault()
    setIsDragging(false)
  }, [])

  const handleDrop = useCallback(
    async (e: React.DragEvent) => {
      e.preventDefault()
      setIsDragging(false)

      const droppedFiles = Array.from(e.dataTransfer.files)
      if (droppedFiles.length === 0) return

      showToast(`Transferring ${droppedFiles.length} file(s)...`)
      try {
        const filePaths = droppedFiles.map((f) => (f as any).path || f.name)
        await WailsBridge.uploadFiles(currentPath, filePaths)
        showToast(`Transferred ${droppedFiles.length} file(s) successfully!`)
        handleRefresh()
      } catch (err) {
        console.error('File drop error:', err)
        showToast('File transfer failed')
      }
    },
    [currentPath, handleRefresh, showToast]
  )

  // Context Menu
  const handleContextMenu = useCallback((e: React.MouseEvent, file: FileEntry) => {
    e.preventDefault()
    setContextMenu({
      x: e.clientX,
      y: e.clientY,
      file,
    })
  }, [])

  return (
    <div
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
      className="relative flex h-screen w-screen bg-brand-bg text-brand-textPrimary font-sans antialiased overflow-hidden select-none"
    >
      {/* OS Drag Overlay */}
      <DropOverlay isDragging={isDragging} targetFolderName={currentPath} />

      {/* Places & Devices Sidebar */}
      <Sidebar
        activePlace={activePlace}
        onSelectPlace={handleSelectPlace}
        onSelectDevice={handleSelectDevice}
        onSelectScanner={() => setActivePlace('scanner')}
        connectionInfo={connectionInfo}
      />

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col h-full overflow-hidden bg-brand-bg">
        {activePlace === 'scanner' ? (
          <ScannerCard
            qrData={qrData}
            isLoading={isQrLoading}
            onRefreshQr={loadQrData}
            onSimulateConnect={() => {
              setActivePlace('device')
              setCurrentPath('/storage/emulated/0')
              loadDirectory('/storage/emulated/0', true)
            }}
          />
        ) : (
          <div className="flex-1 flex flex-col h-full overflow-hidden">
            <BreadcrumbBar
              currentPath={currentPath}
              onNavigatePath={handleNavigatePath}
              onGoBack={handleGoBack}
              canGoBack={pathHistory.length > 0}
              onRefresh={handleRefresh}
              isRefreshing={isFilesLoading}
              searchQuery={searchQuery}
              onSearchChange={setSearchQuery}
              viewMode={viewMode}
              onToggleViewMode={() =>
                setViewMode((prev) => (prev === 'grid' ? 'list' : 'grid'))
              }
            />

            <FileBrowser
              files={filteredFiles}
              viewMode={viewMode}
              isLoading={isFilesLoading}
              onOpenFolder={handleOpenFolder}
              onOpenFile={handleOpenFile}
              onContextMenu={handleContextMenu}
            />
          </div>
        )}
      </main>

      {/* Right Click Context Menu */}
      {contextMenu && (
        <ContextMenu
          x={contextMenu.x}
          y={contextMenu.y}
          file={contextMenu.file}
          isRemote={activePlace === 'device'}
          onClose={() => setContextMenu(null)}
          onOpen={handleOpenFile}
          onDownload={handleOpenFile}
          onCopyPath={(f) => {
            navigator.clipboard.writeText(f.path)
            showToast('Path copied to clipboard')
          }}
        />
      )}

      {/* Bottom Floating Toast Notification */}
      {toast && (
        <div className="absolute bottom-6 left-1/2 -translate-x-1/2 z-50 px-5 py-2.5 rounded-full bg-brand-card/95 border border-brand-border text-white text-xs font-bold shadow-2xl backdrop-blur-md animate-fadeIn flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-brand-blue animate-pulse" />
          <span>{toast}</span>
        </div>
      )}
    </div>
  )
}
