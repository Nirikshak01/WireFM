import React, { useEffect, useRef } from 'react'
import { Download, ExternalLink, Copy, Trash2 } from 'lucide-react'
import { FileEntry } from '../types'

interface ContextMenuProps {
  x: number
  y: number
  file: FileEntry | null
  isRemote: boolean
  onClose: () => void
  onOpen: (file: FileEntry) => void
  onDownload?: (file: FileEntry) => void
  onCopyPath: (file: FileEntry) => void
  onDelete?: (file: FileEntry) => void
}

export const ContextMenu: React.FC<ContextMenuProps> = ({
  x,
  y,
  file,
  isRemote,
  onClose,
  onOpen,
  onDownload,
  onCopyPath,
  onDelete,
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

  if (!file) return null

  // Ensure menu stays inside window bounds
  const adjustedX = Math.min(x, window.innerWidth - 220)
  const adjustedY = Math.min(y, window.innerHeight - 200)

  return (
    <div
      ref={menuRef}
      style={{ left: `${adjustedX}px`, top: `${adjustedY}px` }}
      className="fixed z-50 w-52 rounded-2xl bg-[#16171D] border-[1.5px] border-[#2F8BF0] shadow-2xl p-1.5 flex flex-col gap-1 select-none animate-fadeIn text-sm font-bold"
    >
      <div className="px-3 py-1.5 text-xs text-brand-textTertiary truncate border-b border-white/5 pb-2">
        {file.name}
      </div>

      <button
        onClick={() => {
          onOpen(file)
          onClose()
        }}
        className="flex items-center gap-2.5 px-3 py-2 rounded-xl text-brand-textPrimary hover:bg-white/10 hover:text-white transition-colors"
      >
        <ExternalLink className="w-4 h-4 text-brand-blue" />
        <span>Open</span>
      </button>

      {isRemote && !file.is_dir && onDownload && (
        <button
          onClick={() => {
            onDownload(file)
            onClose()
          }}
          className="flex items-center gap-2.5 px-3 py-2 rounded-xl text-brand-textPrimary hover:bg-white/10 hover:text-white transition-colors"
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
        className="flex items-center gap-2.5 px-3 py-2 rounded-xl text-brand-textPrimary hover:bg-white/10 hover:text-white transition-colors"
      >
        <Copy className="w-4 h-4 text-brand-textTertiary" />
        <span>Copy Path</span>
      </button>

      {onDelete && (
        <button
          onClick={() => {
            onDelete(file)
            onClose()
          }}
          className="flex items-center gap-2.5 px-3 py-2 rounded-xl text-rose-400 hover:bg-rose-500/10 transition-colors"
        >
          <Trash2 className="w-4 h-4" />
          <span>Delete</span>
        </button>
      )}
    </div>
  )
}
