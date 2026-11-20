import React, { useEffect, useRef } from 'react'
import {
  ExternalLink,
  Eye,
  Copy,
  Scissors,
  Clipboard,
  Edit3,
  Trash2,
  Download,
  Link,
  FolderPlus,
  RotateCw,
  AppWindow,
} from 'lucide-react'
import { FileEntry, ClipboardState } from '../types'

interface ContextMenuProps {
  x: number
  y: number
  file: FileEntry | null
  isRemote: boolean
  clipboard: ClipboardState | null
  onClose: () => void
  onOpen: (file: FileEntry) => void
  onPreview?: (file: FileEntry) => void
  onOpenWith?: (file: FileEntry) => void
  onCopy: (file: FileEntry) => void
  onCut: (file: FileEntry) => void
  onPaste: () => void
  onRename?: (file: FileEntry) => void
  onDelete?: (file: FileEntry) => void
  onDownload?: (file: FileEntry) => void
  onCopyPath: (file: FileEntry) => void
  onNewFolder?: () => void
  onRefresh?: () => void
}

export const ContextMenu: React.FC<ContextMenuProps> = ({
  x,
  y,
  file,
  isRemote,
  clipboard,
  onClose,
  onOpen,
  onPreview,
  onOpenWith,
  onCopy,
  onCut,
  onPaste,
  onRename,
  onDelete,
  onDownload,
  onCopyPath,
  onNewFolder,
  onRefresh,
}) => {
  const menuRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        onClose()
      }
    }
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose()
    }

    document.addEventListener('mousedown', handleClickOutside)
    document.addEventListener('keydown', handleKeyDown)
    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
      document.removeEventListener('keydown', handleKeyDown)
    }
  }, [onClose])

  // Stay inside window bounds
  const adjustedX = Math.min(x, window.innerWidth - 220)
  const adjustedY = Math.min(y, window.innerHeight - 340)

  const hasClipboardItems = clipboard !== null && clipboard.files.length > 0
  const isMedia = file && ['png', 'jpg', 'jpeg', 'webp', 'gif', 'svg', 'mp4', 'webm', 'mp3', 'wav'].includes(file.ext.toLowerCase())

  // Empty canvas context menu
  if (!file) {
    return (
      <div
        ref={menuRef}
        style={{ left: `${adjustedX}px`, top: `${adjustedY}px` }}
        className="fixed z-50 w-52 rounded-2xl bg-[#16171D] border-[1.5px] border-[#2F8BF0] shadow-2xl p-1.5 flex flex-col gap-1 select-none animate-fadeIn text-sm font-bold"
      >
        {onNewFolder && (
          <button
            onClick={() => {
              onNewFolder()
              onClose()
            }}
            className="flex items-center gap-2.5 px-3 py-2 rounded-xl text-brand-textPrimary hover:bg-white/10 hover:text-white transition-colors"
          >
            <FolderPlus className="w-4 h-4 text-brand-blue" />
            <span>New Folder</span>
          </button>
        )}

        <button
          onClick={() => {
            if (hasClipboardItems) {
              onPaste()
              onClose()
            }
          }}
          disabled={!hasClipboardItems}
          className={`flex items-center gap-2.5 px-3 py-2 rounded-xl transition-all ${
            hasClipboardItems
              ? 'text-brand-blue bg-brand-blue/10 shadow-[0_0_12px_rgba(47,139,240,0.3)] hover:bg-brand-blue/20'
              : 'text-brand-textTertiary cursor-not-allowed opacity-40'
          }`}
        >
          <Clipboard className={`w-4 h-4 ${hasClipboardItems ? 'text-brand-blue animate-pulse' : ''}`} />
          <span>Paste</span>
        </button>

        {onRefresh && (
          <button
            onClick={() => {
              onRefresh()
              onClose()
            }}
            className="flex items-center gap-2.5 px-3 py-2 rounded-xl text-brand-textPrimary hover:bg-white/10 hover:text-white transition-colors"
          >
            <RotateCw className="w-4 h-4 text-brand-textTertiary" />
            <span>Refresh</span>
          </button>
        )}
      </div>
    )
  }

  // File or folder context menu
  return (
    <div
      ref={menuRef}
      style={{ left: `${adjustedX}px`, top: `${adjustedY}px` }}
      className="fixed z-50 w-56 rounded-2xl bg-[#16171D] border-[1.5px] border-[#2F8BF0] shadow-2xl p-1.5 flex flex-col gap-0.5 select-none animate-fadeIn text-sm font-bold"
    >
      <div className="px-3 py-1.5 text-xs text-brand-textTertiary truncate border-b border-white/5 pb-2">
        {file.name}
      </div>

      <button
        onClick={() => {
          onOpen(file)
          onClose()
        }}
        className="flex items-center gap-2.5 px-3 py-1.5 rounded-xl text-brand-textPrimary hover:bg-white/10 hover:text-white transition-colors"
      >
        <ExternalLink className="w-4 h-4 text-brand-blue" />
        <span>Open</span>
      </button>

      {isMedia && onPreview && (
        <button
          onClick={() => {
            onPreview(file)
            onClose()
          }}
          className="flex items-center gap-2.5 px-3 py-1.5 rounded-xl text-brand-textPrimary hover:bg-white/10 hover:text-white transition-colors"
        >
          <Eye className="w-4 h-4 text-emerald-400" />
          <span>Quick Preview</span>
        </button>
      )}

      {onOpenWith && (
        <button
          onClick={() => {
            onOpenWith(file)
            onClose()
          }}
          className="flex items-center gap-2.5 px-3 py-1.5 rounded-xl text-brand-textPrimary hover:bg-white/10 hover:text-white transition-colors"
        >
          <AppWindow className="w-4 h-4 text-purple-400" />
          <span>Open With...</span>
        </button>
      )}

      <div className="h-[1px] bg-white/5 my-1" />

      <button
        onClick={() => {
          onCopy(file)
          onClose()
        }}
        className="flex items-center gap-2.5 px-3 py-1.5 rounded-xl text-brand-textPrimary hover:bg-white/10 hover:text-white transition-colors"
      >
        <Copy className="w-4 h-4 text-brand-textSecondary" />
        <span>Copy</span>
      </button>

      <button
        onClick={() => {
          onCut(file)
          onClose()
        }}
        className="flex items-center gap-2.5 px-3 py-1.5 rounded-xl text-brand-textPrimary hover:bg-white/10 hover:text-white transition-colors"
      >
        <Scissors className="w-4 h-4 text-amber-400" />
        <span>Cut</span>
      </button>

      <button
        onClick={() => {
          if (hasClipboardItems) {
            onPaste()
            onClose()
          }
        }}
        disabled={!hasClipboardItems}
        className={`flex items-center gap-2.5 px-3 py-1.5 rounded-xl transition-all ${
          hasClipboardItems
            ? 'text-brand-blue bg-brand-blue/10 shadow-[0_0_12px_rgba(47,139,240,0.3)] hover:bg-brand-blue/20'
            : 'text-brand-textTertiary cursor-not-allowed opacity-40'
        }`}
      >
        <Clipboard className={`w-4 h-4 ${hasClipboardItems ? 'text-brand-blue animate-pulse' : ''}`} />
        <span>Paste</span>
      </button>

      {onRename && (
        <button
          onClick={() => {
            onRename(file)
            onClose()
          }}
          className="flex items-center gap-2.5 px-3 py-1.5 rounded-xl text-brand-textPrimary hover:bg-white/10 hover:text-white transition-colors"
        >
          <Edit3 className="w-4 h-4 text-sky-400" />
          <span>Rename</span>
        </button>
      )}

      {isRemote && !file.is_dir && onDownload && (
        <button
          onClick={() => {
            onDownload(file)
            onClose()
          }}
          className="flex items-center gap-2.5 px-3 py-1.5 rounded-xl text-brand-textPrimary hover:bg-white/10 hover:text-white transition-colors"
        >
          <Download className="w-4 h-4 text-emerald-400" />
          <span>Download to PC</span>
        </button>
      )}

      <button
        onClick={() => {
          onCopyPath(file)
          onClose()
        }}
        className="flex items-center gap-2.5 px-3 py-1.5 rounded-xl text-brand-textPrimary hover:bg-white/10 hover:text-white transition-colors"
      >
        <Link className="w-4 h-4 text-brand-textTertiary" />
        <span>Copy Path</span>
      </button>

      {onDelete && (
        <button
          onClick={() => {
            onDelete(file)
            onClose()
          }}
          className="flex items-center gap-2.5 px-3 py-1.5 rounded-xl text-rose-400 hover:bg-rose-500/10 transition-colors"
        >
          <Trash2 className="w-4 h-4" />
          <span>Delete</span>
        </button>
      )}
    </div>
  )
}
