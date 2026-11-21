import React, { useState, useEffect, useCallback, useMemo } from 'react'
import { Sidebar } from './components/Sidebar'
import { ScannerCard } from './components/ScannerCard'
import { BreadcrumbBar } from './components/BreadcrumbBar'
import { FileBrowser } from './components/FileBrowser'
import { DropOverlay } from './components/DropOverlay'
import { ContextMenu } from './components/ContextMenu'
import { MediaPreviewModal } from './components/MediaPreviewModal'
import { WailsBridge } from './services/wailsBridge'
import {
  Place,
  ViewMode,
  QrData,
  FileEntry,
  ConnectedDevice,
  ClipboardState,
} from './types'
import { Copy, Scissors, Trash2, X } from 'lucide-react'

export default function App() {
  // Navigation
  const [activePlace, setActivePlace] = useState<Place | 'device' | 'scanner'>('scanner')
  const [activeDeviceId, setActiveDeviceId] = useState<string | null>(null)
  const [currentPath, setCurrentPath] = useState<string>('~')
  const [pathHistory, setPathHistory] = useState<string[]>([])

  // Pairing & Multi-Device state
  const [qrData, setQrData] = useState<QrData | null>(null)
  const [isQrLoading, setIsQrLoading] = useState<boolean>(true)
  const [connectedDevices, setConnectedDevices] = useState<ConnectedDevice[]>([])

  // File browser state
  const [files, setFiles] = useState<FileEntry[]>([])
  const [isFilesLoading, setIsFilesLoading] = useState<boolean>(false)
  const [searchQuery, setSearchQuery] = useState<string>('')
  const [viewMode, setViewMode] = useState<ViewMode>('grid')
  const [selectedPaths, setSelectedPaths] = useState<Set<string>>(new Set())

  // Clipboard (Copy/Cut/Paste)
  const [clipboard, setClipboard] = useState<ClipboardState | null>(null)

  // Media preview modal
  const [previewFile, setPreviewFile] = useState<FileEntry | null>(null)

  // Drag and drop state
  const [isDragging, setIsDragging] = useState<boolean>(false)

  // Context menu state
  const [contextMenu, setContextMenu] = useState<{
    x: number
    y: number
    file: FileEntry | null
  } | null>(null)

  // Modals for New Folder & Rename
  const [showNewFolderModal, setShowNewFolderModal] = useState<boolean>(false)
  const [newFolderName, setNewFolderName] = useState<string>('')
  const [fileToRename, setFileToRename] = useState<FileEntry | null>(null)
  const [renameValue, setRenameValue] = useState<string>('')

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

  // Poll connection status & connected devices
  const checkDevices = useCallback(async () => {
    try {
      const devs = await WailsBridge.getConnectedDevices()
      setConnectedDevices(Array.isArray(devs) ? devs : [])
    } catch (err) {
      console.error('Failed to get connected devices:', err)
      setConnectedDevices([])
    }
  }, [])

  useEffect(() => {
    loadQrData()
    checkDevices()

    const interval = setInterval(() => {
      checkDevices()
    }, 2500)

    return () => clearInterval(interval)
  }, [loadQrData, checkDevices])

  // Load directory files
  const loadDirectory = useCallback(
    async (path: string, isRemote: boolean) => {
      setIsFilesLoading(true)
      setSelectedPaths(new Set())
      try {
        let entries: FileEntry[] = []
        if (isRemote) {
          entries = await WailsBridge.listRemoteDirectory(path)
        } else {
          entries = await WailsBridge.listLocalDirectory(path)
        }
        setFiles(Array.isArray(entries) ? entries : [])
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
      setActiveDeviceId(null)
      const targetPath = place === 'home' ? '~' : place
      setPathHistory((prev) => [...prev, currentPath])
      setCurrentPath(targetPath)
      loadDirectory(targetPath, false)
    },
    [currentPath, loadDirectory]
  )

  // Select Device
  const handleSelectDevice = useCallback(
    (device: ConnectedDevice) => {
      setActivePlace('device')
      setActiveDeviceId(device.id)
      const phoneRoot = '/storage/emulated/0'
      setPathHistory((prev) => [...prev, currentPath])
      setCurrentPath(phoneRoot)
      loadDirectory(phoneRoot, true)
    },
    [currentPath, loadDirectory]
  )

  // Disconnect Device
  const handleDisconnectDevice = useCallback(
    async (deviceId: string) => {
      await WailsBridge.removeConnectedDevice(deviceId)
      checkDevices()
      if (activeDeviceId === deviceId) {
        setActivePlace('scanner')
        setActiveDeviceId(null)
      }
      showToast('Device disconnected')
    },
    [activeDeviceId, checkDevices, showToast]
  )

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

  // Clipboard operations
  const handleCopy = useCallback(
    (file: FileEntry) => {
      const itemsToCopy = selectedPaths.has(file.path)
        ? files.filter((f) => selectedPaths.has(f.path))
        : [file]
      setClipboard({
        action: 'copy',
        files: itemsToCopy,
        sourcePath: currentPath,
      })
      showToast(`Copied ${itemsToCopy.length} item(s)`)
    },
    [selectedPaths, files, currentPath, showToast]
  )

  const handleCut = useCallback(
    (file: FileEntry) => {
      const itemsToCut = selectedPaths.has(file.path)
        ? files.filter((f) => selectedPaths.has(f.path))
        : [file]
      setClipboard({
        action: 'cut',
        files: itemsToCut,
        sourcePath: currentPath,
      })
      showToast(`Cut ${itemsToCut.length} item(s)`)
    },
    [selectedPaths, files, currentPath, showToast]
  )

  const handlePaste = useCallback(async () => {
    if (!clipboard || clipboard.files.length === 0) return

    showToast(`Pasting ${clipboard.files.length} item(s)...`)
    try {
      for (const f of clipboard.files) {
        if (clipboard.action === 'cut') {
          await WailsBridge.moveFile(f.path, currentPath)
        } else {
          await WailsBridge.copyFile(f.path, currentPath)
        }
      }
      if (clipboard.action === 'cut') {
        setClipboard(null)
      }
      showToast('Pasted successfully')
      handleRefresh()
    } catch (err) {
      console.error('Failed to paste:', err)
      showToast('Paste operation failed')
    }
  }, [clipboard, currentPath, showToast, handleRefresh])

  // Delete
  const handleDelete = useCallback(
    async (file: FileEntry) => {
      const itemsToDelete = selectedPaths.has(file.path)
        ? files.filter((f) => selectedPaths.has(f.path))
        : [file]

      try {
        for (const f of itemsToDelete) {
          await WailsBridge.deleteFile(f.path)
        }
        showToast(`Deleted ${itemsToDelete.length} item(s)`)
        handleRefresh()
      } catch (err) {
        console.error('Delete error:', err)
        showToast('Delete failed')
      }
    },
    [selectedPaths, files, showToast, handleRefresh]
  )

  // Create Folder
  const handleCreateFolder = useCallback(async () => {
    if (!newFolderName.trim()) return
    try {
      await WailsBridge.createFolder(currentPath, newFolderName.trim())
      setShowNewFolderModal(false)
      setNewFolderName('')
      showToast('Folder created')
      handleRefresh()
    } catch (err) {
      console.error('Create folder error:', err)
      showToast('Failed to create folder')
    }
  }, [newFolderName, currentPath, showToast, handleRefresh])

  // Rename
  const handleRenameConfirm = useCallback(async () => {
    if (!fileToRename || !renameValue.trim()) return
    try {
      await WailsBridge.renameFile(fileToRename.path, renameValue.trim())
      setFileToRename(null)
      setRenameValue('')
      showToast('Renamed successfully')
      handleRefresh()
    } catch (err) {
      console.error('Rename error:', err)
      showToast('Failed to rename')
    }
  }, [fileToRename, renameValue, showToast, handleRefresh])

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
  const handleContextMenu = useCallback((e: React.MouseEvent, file: FileEntry | null) => {
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
        activeDeviceId={activeDeviceId}
        onSelectPlace={handleSelectPlace}
        onSelectDevice={handleSelectDevice}
        onDisconnectDevice={handleDisconnectDevice}
        onSelectScanner={() => setActivePlace('scanner')}
        connectedDevices={connectedDevices}
      />

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col h-full overflow-hidden bg-brand-bg relative">
        {activePlace === 'scanner' ? (
          <ScannerCard
            qrData={qrData}
            isLoading={isQrLoading}
            onRefreshQr={loadQrData}
            onSimulateConnect={() => {
              const mockDev: ConnectedDevice = {
                id: 'vivo_mock',
                name: 'Vivo y36',
                ip: '192.168.1.42',
                status: 'Connected',
              }
              setConnectedDevices([mockDev])
              handleSelectDevice(mockDev)
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

            {/* Multi-Selection Floating Action Bar */}
            {selectedPaths.size > 0 && (
              <div className="absolute top-16 left-1/2 -translate-x-1/2 z-40 px-5 py-2 rounded-2xl bg-brand-surface/90 border border-brand-blue/50 shadow-2xl backdrop-blur-md flex items-center gap-4 text-xs font-bold text-white animate-fadeIn">
                <span className="text-brand-blue">{selectedPaths.size} selected</span>
                <div className="h-4 w-[1px] bg-white/10" />
                <button
                  onClick={() => {
                    const firstFile = files.find((f) => selectedPaths.has(f.path))
                    if (firstFile) handleCopy(firstFile)
                  }}
                  className="flex items-center gap-1.5 hover:text-brand-blue transition-colors"
                >
                  <Copy className="w-3.5 h-3.5" />
                  <span>Copy</span>
                </button>
                <button
                  onClick={() => {
                    const firstFile = files.find((f) => selectedPaths.has(f.path))
                    if (firstFile) handleCut(firstFile)
                  }}
                  className="flex items-center gap-1.5 hover:text-amber-400 transition-colors"
                >
                  <Scissors className="w-3.5 h-3.5" />
                  <span>Cut</span>
                </button>
                <button
                  onClick={() => {
                    const firstFile = files.find((f) => selectedPaths.has(f.path))
                    if (firstFile) handleDelete(firstFile)
                  }}
                  className="flex items-center gap-1.5 hover:text-rose-400 transition-colors"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                  <span>Delete</span>
                </button>
                <button
                  onClick={() => setSelectedPaths(new Set())}
                  className="p-1 rounded-full hover:bg-white/10 text-brand-textTertiary hover:text-white transition-colors"
                  title="Clear Selection"
                >
                  <X className="w-3.5 h-3.5" />
                </button>
              </div>
            )}

            <FileBrowser
              files={filteredFiles}
              viewMode={viewMode}
              isLoading={isFilesLoading}
              selectedPaths={selectedPaths}
              onSelectionChange={setSelectedPaths}
              onOpenFolder={handleOpenFolder}
              onOpenFile={handleOpenFile}
              onPreviewMedia={(file) => setPreviewFile(file)}
              onContextMenu={handleContextMenu}
            />
          </div>
        )}
      </main>

      {/* Media Preview Modal */}
      {previewFile && (
        <MediaPreviewModal
          file={previewFile}
          onClose={() => setPreviewFile(null)}
          onDownload={handleOpenFile}
          onOpenSystem={handleOpenFile}
        />
      )}

      {/* Right Click Context Menu */}
      {contextMenu && (
        <ContextMenu
          x={contextMenu.x}
          y={contextMenu.y}
          file={contextMenu.file}
          isRemote={activePlace === 'device'}
          clipboard={clipboard}
          onClose={() => setContextMenu(null)}
          onOpen={handleOpenFile}
          onPreview={(f) => setPreviewFile(f)}
          onOpenWith={handleOpenFile}
          onCopy={handleCopy}
          onCut={handleCut}
          onPaste={handlePaste}
          onRename={(f) => {
            setFileToRename(f)
            setRenameValue(f.name)
          }}
          onDelete={handleDelete}
          onDownload={handleOpenFile}
          onCopyPath={(f) => {
            navigator.clipboard.writeText(f.path)
            showToast('Path copied to clipboard')
          }}
          onNewFolder={() => setShowNewFolderModal(true)}
          onRefresh={handleRefresh}
        />
      )}

      {/* Create New Folder Modal */}
      {showNewFolderModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 animate-fadeIn">
          <div className="w-full max-w-sm rounded-3xl bg-brand-surface border border-brand-border p-6 shadow-2xl flex flex-col gap-4">
            <h3 className="text-base font-bold text-white">Create New Folder</h3>
            <input
              type="text"
              value={newFolderName}
              onChange={(e) => setNewFolderName(e.target.value)}
              placeholder="Folder name"
              autoFocus
              onKeyDown={(e) => {
                if (e.key === 'Enter') handleCreateFolder()
                if (e.key === 'Escape') setShowNewFolderModal(false)
              }}
              className="w-full px-4 py-2.5 rounded-xl bg-brand-bg border border-brand-border text-sm text-white focus:outline-none focus:border-brand-blue"
            />
            <div className="flex items-center justify-end gap-2 mt-2">
              <button
                onClick={() => setShowNewFolderModal(false)}
                className="px-4 py-2 rounded-xl text-xs font-bold text-brand-textSecondary hover:bg-white/5 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleCreateFolder}
                className="px-4 py-2 rounded-xl bg-brand-blue text-white text-xs font-bold hover:bg-brand-blueHover transition-colors"
              >
                Create
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Rename File Modal */}
      {fileToRename && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 animate-fadeIn">
          <div className="w-full max-w-sm rounded-3xl bg-brand-surface border border-brand-border p-6 shadow-2xl flex flex-col gap-4">
            <h3 className="text-base font-bold text-white">Rename</h3>
            <input
              type="text"
              value={renameValue}
              onChange={(e) => setRenameValue(e.target.value)}
              autoFocus
              onKeyDown={(e) => {
                if (e.key === 'Enter') handleRenameConfirm()
                if (e.key === 'Escape') setFileToRename(null)
              }}
              className="w-full px-4 py-2.5 rounded-xl bg-brand-bg border border-brand-border text-sm text-white focus:outline-none focus:border-brand-blue"
            />
            <div className="flex items-center justify-end gap-2 mt-2">
              <button
                onClick={() => setFileToRename(null)}
                className="px-4 py-2 rounded-xl text-xs font-bold text-brand-textSecondary hover:bg-white/5 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleRenameConfirm}
                className="px-4 py-2 rounded-xl bg-brand-blue text-white text-xs font-bold hover:bg-brand-blueHover transition-colors"
              >
                Rename
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Bottom Toast Notification */}
      {toast && (
        <div className="absolute bottom-6 left-1/2 -translate-x-1/2 z-50 px-5 py-2.5 rounded-full bg-brand-card/95 border border-brand-border text-white text-xs font-bold shadow-2xl backdrop-blur-md animate-fadeIn flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-brand-blue animate-pulse" />
          <span>{toast}</span>
        </div>
      )}
    </div>
  )
}
