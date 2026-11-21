import React, { useState, useRef, useCallback } from 'react'
import {
  FileText,
  FileCode,
  Image as ImageIcon,
  Music,
  Video,
  Archive,
  File as FileGeneric,
  Folder,
  CheckCircle2,
} from 'lucide-react'
import { FileEntry, ViewMode } from '../types'

interface FileBrowserProps {
  files: FileEntry[]
  viewMode: ViewMode
  isLoading: boolean
  selectedPaths: Set<string>
  onSelectionChange: (paths: Set<string>) => void
  onOpenFolder: (folder: FileEntry) => void
  onOpenFile: (file: FileEntry) => void
  onPreviewMedia: (file: FileEntry) => void
  onContextMenu: (e: React.MouseEvent, file: FileEntry | null) => void
}

function formatBytes(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

function getFileIcon(ext: string) {
  const codeExts = ['js', 'ts', 'tsx', 'jsx', 'json', 'py', 'go', 'html', 'css', 'rs', 'c', 'cpp', 'sh']
  const imgExts = ['png', 'jpg', 'jpeg', 'svg', 'gif', 'webp', 'bmp', 'ico']
  const audioExts = ['mp3', 'wav', 'flac', 'ogg', 'm4a', 'aac']
  const videoExts = ['mp4', 'mkv', 'webm', 'mov', 'avi']
  const archiveExts = ['zip', 'tar', 'gz', '7z', 'rar', 'apk']

  if (codeExts.includes(ext)) return <FileCode className="w-8 h-8 text-blue-400" />
  if (imgExts.includes(ext)) return <ImageIcon className="w-8 h-8 text-emerald-400" />
  if (audioExts.includes(ext)) return <Music className="w-8 h-8 text-purple-400" />
  if (videoExts.includes(ext)) return <Video className="w-8 h-8 text-rose-400" />
  if (archiveExts.includes(ext)) return <Archive className="w-8 h-8 text-amber-400" />
  if (['pdf', 'doc', 'docx', 'txt', 'md'].includes(ext)) return <FileText className="w-8 h-8 text-amber-300" />
  return <FileGeneric className="w-8 h-8 text-brand-textTertiary" />
}

export const FileBrowser: React.FC<FileBrowserProps> = ({
  files,
  viewMode,
  isLoading,
  selectedPaths,
  onSelectionChange,
  onOpenFolder,
  onOpenFile,
  onPreviewMedia,
  onContextMenu,
}) => {
  const containerRef = useRef<HTMLDivElement>(null)
  const itemRefs = useRef<Map<string, HTMLDivElement>>(new Map())

  // Marquee drag-to-select state
  const [isMarqueeActive, setIsMarqueeActive] = useState<boolean>(false)
  const [marqueeBox, setMarqueeBox] = useState<{
    startX: number
    startY: number
    x: number
    y: number
    w: number
    h: number
  } | null>(null)

  const handleMouseDown = useCallback(
    (e: React.MouseEvent) => {
      // Only trigger on left-click on the background
      if (e.button !== 0 || !containerRef.current) return

      const target = e.target as HTMLElement
      if (target.closest('.file-card-item')) return

      // Clicking blank area clears selection unless Shift/Ctrl is held
      if (!e.shiftKey && !e.ctrlKey && !e.metaKey) {
        onSelectionChange(new Set())
      }

      const rect = containerRef.current.getBoundingClientRect()
      const startX = e.clientX - rect.left + containerRef.current.scrollLeft
      const startY = e.clientY - rect.top + containerRef.current.scrollTop

      setIsMarqueeActive(true)
      setMarqueeBox({
        startX,
        startY,
        x: startX,
        y: startY,
        w: 0,
        h: 0,
      })
    },
    [onSelectionChange]
  )

  const handleMouseMove = useCallback(
    (e: React.MouseEvent) => {
      if (!isMarqueeActive || !marqueeBox || !containerRef.current) return

      const rect = containerRef.current.getBoundingClientRect()
      const currentX = e.clientX - rect.left + containerRef.current.scrollLeft
      const currentY = e.clientY - rect.top + containerRef.current.scrollTop

      const x = Math.min(marqueeBox.startX, currentX)
      const y = Math.min(marqueeBox.startY, currentY)
      const w = Math.abs(currentX - marqueeBox.startX)
      const h = Math.abs(currentY - marqueeBox.startY)

      setMarqueeBox({
        ...marqueeBox,
        x,
        y,
        w,
        h,
      })

      // Check intersection with file items
      const newSelected = new Set(selectedPaths)
      const marqueeRight = x + w
      const marqueeBottom = y + h

      itemRefs.current.forEach((el, path) => {
        if (!el || !containerRef.current) return
        const itemLeft = el.offsetLeft
        const itemTop = el.offsetTop
        const itemRight = itemLeft + el.offsetWidth
        const itemBottom = itemTop + el.offsetHeight

        const isIntersecting = !(
          marqueeRight < itemLeft ||
          x > itemRight ||
          marqueeBottom < itemTop ||
          y > itemBottom
        )

        if (isIntersecting) {
          newSelected.add(path)
        }
      })

      onSelectionChange(newSelected)
    },
    [isMarqueeActive, marqueeBox, selectedPaths, onSelectionChange]
  )

  const handleMouseUp = useCallback(() => {
    setIsMarqueeActive(false)
    setMarqueeBox(null)
  }, [])

  const handleItemClick = (e: React.MouseEvent, file: FileEntry) => {
    e.stopPropagation()

    // Multi-selection if Ctrl, Shift, or Command is held
    if (e.ctrlKey || e.metaKey || e.shiftKey) {
      const newSelected = new Set(selectedPaths)
      if (newSelected.has(file.path)) {
        newSelected.delete(file.path)
      } else {
        newSelected.add(file.path)
      }
      onSelectionChange(newSelected)
      return
    }

    // 1-Click to enter folder per user's explicit request
    if (file.is_dir) {
      onSelectionChange(new Set())
      onOpenFolder(file)
      return
    }

    // 1-Click to preview media files (images, videos, music)
    const isMedia = ['png', 'jpg', 'jpeg', 'webp', 'gif', 'svg', 'mp4', 'webm', 'mp3', 'wav', 'flac'].includes(
      file.ext.toLowerCase()
    )
    if (isMedia) {
      onPreviewMedia(file)
      return
    }

    // Normal file click -> select
    const newSelected = new Set<string>()
    newSelected.add(file.path)
    onSelectionChange(newSelected)
  }

  const handleItemDoubleClick = (file: FileEntry) => {
    if (file.is_dir) {
      onOpenFolder(file)
    } else {
      const isMedia = ['png', 'jpg', 'jpeg', 'webp', 'gif', 'svg', 'mp4', 'webm', 'mp3', 'wav'].includes(
        file.ext.toLowerCase()
      )
      if (isMedia) {
        onPreviewMedia(file)
      } else {
        onOpenFile(file)
      }
    }
  }

  if (isLoading) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center text-brand-textTertiary">
        <div className="w-8 h-8 border-2 border-brand-blue border-t-transparent rounded-full animate-spin mb-3" />
        <span className="text-sm font-semibold">Loading directory contents...</span>
      </div>
    )
  }

  const safeFiles = Array.isArray(files) ? files : []

  if (safeFiles.length === 0) {
    return (
      <div
        onContextMenu={(e) => onContextMenu(e, null)}
        className="flex-1 flex flex-col items-center justify-center text-brand-textTertiary select-none"
      >
        <Folder className="w-12 h-12 opacity-30 mb-2" />
        <span className="text-base font-bold text-brand-textSecondary">Empty Folder</span>
        <span className="text-xs mt-1">Right-click or drag files to upload</span>
      </div>
    )
  }

  return (
    <div
      ref={containerRef}
      onMouseDown={handleMouseDown}
      onMouseMove={handleMouseMove}
      onMouseUp={handleMouseUp}
      onContextMenu={(e) => {
        const target = e.target as HTMLElement
        if (!target.closest('.file-card-item')) {
          onContextMenu(e, null)
        }
      }}
      className="relative flex-1 overflow-y-auto select-none p-6"
    >
      {/* Marquee Selection Rectangle matching user request */}
      {isMarqueeActive && marqueeBox && marqueeBox.w > 3 && marqueeBox.h > 3 && (
        <div
          style={{
            position: 'absolute',
            left: `${marqueeBox.x}px`,
            top: `${marqueeBox.y}px`,
            width: `${marqueeBox.w}px`,
            height: `${marqueeBox.h}px`,
            pointerEvents: 'none',
          }}
          className="border-[1.5px] border-dashed border-[#2F8BF0] bg-[#2F8BF0]/15 rounded-lg z-30"
        />
      )}

      {viewMode === 'list' ? (
        <div className="divide-y divide-brand-border/40">
          {safeFiles.map((file) => {
            const isSelected = selectedPaths.has(file.path)
            return (
              <div
                key={file.path}
                ref={(el) => {
                  if (el) itemRefs.current.set(file.path, el)
                  else itemRefs.current.delete(file.path)
                }}
                onClick={(e) => handleItemClick(e, file)}
                onDoubleClick={() => handleItemDoubleClick(file)}
                onContextMenu={(e) => {
                  e.stopPropagation()
                  if (!selectedPaths.has(file.path)) {
                    const s = new Set<string>()
                    s.add(file.path)
                    onSelectionChange(s)
                  }
                  onContextMenu(e, file)
                }}
                className={`file-card-item flex items-center justify-between px-4 py-2.5 rounded-xl cursor-pointer transition-colors group ${
                  isSelected
                    ? 'bg-brand-blue/20 border border-brand-blue/60 text-white'
                    : 'hover:bg-white/[0.04]'
                }`}
              >
                <div className="flex items-center gap-3 truncate pr-4">
                  {isSelected && <CheckCircle2 className="w-4 h-4 text-brand-blue flex-shrink-0" />}
                  {file.is_dir ? (
                    <div className="w-7 h-7 rounded-lg bg-[#CCD8DA] flex items-center justify-center flex-shrink-0 text-black">
                      <Folder className="w-4 h-4 fill-current" />
                    </div>
                  ) : (
                    <div className="w-7 h-7 flex items-center justify-center flex-shrink-0">
                      {getFileIcon(file.ext)}
                    </div>
                  )}
                  <span className="text-sm font-bold text-brand-textPrimary truncate group-hover:text-white">
                    {file.name}
                  </span>
                </div>
                <div className="text-xs text-brand-textTertiary flex-shrink-0">
                  {file.is_dir ? 'Folder' : formatBytes(file.size)}
                </div>
              </div>
            )
          })}
        </div>
      ) : (
        /* 3-Column Grid matching Figma Mockup */
        <div className="grid grid-cols-3 gap-6 auto-rows-max">
          {safeFiles.map((file) => {
            const isSelected = selectedPaths.has(file.path)
            return (
              <div
                key={file.path}
                ref={(el) => {
                  if (el) itemRefs.current.set(file.path, el)
                  else itemRefs.current.delete(file.path)
                }}
                onClick={(e) => handleItemClick(e, file)}
                onDoubleClick={() => handleItemDoubleClick(file)}
                onContextMenu={(e) => {
                  e.stopPropagation()
                  if (!selectedPaths.has(file.path)) {
                    const s = new Set<string>()
                    s.add(file.path)
                    onSelectionChange(s)
                  }
                  onContextMenu(e, file)
                }}
                className="file-card-item relative flex flex-col items-center group cursor-pointer"
              >
                {/* Selection Check Badge */}
                {isSelected && (
                  <div className="absolute top-2 right-2 z-20 w-6 h-6 rounded-full bg-brand-blue text-white flex items-center justify-center shadow-lg">
                    <CheckCircle2 className="w-4 h-4" />
                  </div>
                )}

                {file.is_dir ? (
                  // Figma Styled Tabbed Folder Card (#CCD8DA)
                  <div
                    className={`relative w-full aspect-[4/3] rounded-2xl bg-[#CCD8DA] shadow-md transition-all duration-200 group-hover:scale-[1.02] flex items-center justify-center overflow-hidden ${
                      isSelected
                        ? 'ring-4 ring-brand-blue shadow-[0_0_15px_rgba(47,139,240,0.4)]'
                        : ''
                    }`}
                  >
                    <div className="absolute top-0 left-0 w-1/3 h-2.5 bg-[#B8C8CC] rounded-br-lg" />
                    <Folder className="w-12 h-12 text-[#2C3337] fill-[#2C3337]/20 transition-transform group-hover:scale-105" />
                  </div>
                ) : (
                  // File Card
                  <div
                    className={`relative w-full aspect-[4/3] rounded-2xl bg-brand-surface/70 border shadow-sm transition-all duration-200 group-hover:scale-[1.02] flex flex-col items-center justify-center p-3 ${
                      isSelected
                        ? 'border-brand-blue bg-brand-blue/15 ring-2 ring-brand-blue'
                        : 'border-brand-border/70 group-hover:border-brand-blue/50'
                    }`}
                  >
                    {getFileIcon(file.ext)}
                    <span className="text-[11px] font-semibold text-brand-textTertiary mt-2">
                      {formatBytes(file.size)}
                    </span>
                  </div>
                )}

                {/* File/Folder Label */}
                <span
                  className={`mt-2 text-sm font-bold text-center truncate w-full px-1 ${
                    isSelected ? 'text-brand-blue' : 'text-brand-textPrimary group-hover:text-white'
                  }`}
                  title={file.name}
                >
                  {file.name}
                </span>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
