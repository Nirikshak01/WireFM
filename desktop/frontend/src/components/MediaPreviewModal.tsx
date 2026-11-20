import React, { useEffect, useState } from 'react'
import { X, ExternalLink, Download, ZoomIn, ZoomOut, RotateCw } from 'lucide-react'
import { FileEntry } from '../types'
import { WailsBridge } from '../services/wailsBridge'

interface MediaPreviewModalProps {
  file: FileEntry | null
  onClose: () => void
  onDownload?: (file: FileEntry) => void
  onOpenSystem?: (file: FileEntry) => void
}

function formatBytes(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

export const MediaPreviewModal: React.FC<MediaPreviewModalProps> = ({
  file,
  onClose,
  onDownload,
  onOpenSystem,
}) => {
  const [dataUrl, setDataUrl] = useState<string>('')
  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [zoom, setZoom] = useState<number>(1)
  const [rotation, setRotation] = useState<number>(0)

  useEffect(() => {
    if (!file) return

    let isMounted = true
    setIsLoading(true)
    setZoom(1)
    setRotation(0)

    WailsBridge.getFileContentBase64(file.path)
      .then((url) => {
        if (isMounted) {
          setDataUrl(url)
          setIsLoading(false)
        }
      })
      .catch((err) => {
        console.error('Failed to load file preview:', err)
        if (isMounted) setIsLoading(false)
      })

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose()
    }
    window.addEventListener('keydown', handleKeyDown)
    return () => {
      isMounted = false
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [file, onClose])

  if (!file) return null

  const isImage = ['png', 'jpg', 'jpeg', 'webp', 'gif', 'svg'].includes(file.ext.toLowerCase())
  const isVideo = ['mp4', 'webm', 'ogg', 'mov', 'mkv'].includes(file.ext.toLowerCase())
  const isAudio = ['mp3', 'wav', 'ogg', 'flac', 'm4a'].includes(file.ext.toLowerCase())

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/85 backdrop-blur-md p-4 animate-fadeIn select-none">
      {/* Top Header Bar */}
      <div className="absolute top-4 left-6 right-6 flex items-center justify-between text-white z-10">
        <div className="flex items-center gap-3 truncate pr-4">
          <span className="font-bold text-base tracking-tight truncate">{file.name}</span>
          <span className="text-xs text-brand-textTertiary bg-white/10 px-2.5 py-0.5 rounded-full flex-shrink-0">
            {formatBytes(file.size)}
          </span>
        </div>

        <div className="flex items-center gap-2">
          {isImage && (
            <>
              <button
                onClick={() => setZoom((z) => Math.min(z + 0.25, 3))}
                className="p-2 rounded-xl bg-white/10 hover:bg-white/20 transition-colors"
                title="Zoom In"
              >
                <ZoomIn className="w-4 h-4" />
              </button>
              <button
                onClick={() => setZoom((z) => Math.max(z - 0.25, 0.5))}
                className="p-2 rounded-xl bg-white/10 hover:bg-white/20 transition-colors"
                title="Zoom Out"
              >
                <ZoomOut className="w-4 h-4" />
              </button>
              <button
                onClick={() => setRotation((r) => (r + 90) % 360)}
                className="p-2 rounded-xl bg-white/10 hover:bg-white/20 transition-colors"
                title="Rotate"
              >
                <RotateCw className="w-4 h-4" />
              </button>
            </>
          )}

          {onOpenSystem && (
            <button
              onClick={() => onOpenSystem(file)}
              className="p-2 rounded-xl bg-white/10 hover:bg-white/20 transition-colors"
              title="Open with Default App"
            >
              <ExternalLink className="w-4 h-4" />
            </button>
          )}

          {onDownload && (
            <button
              onClick={() => onDownload(file)}
              className="p-2 rounded-xl bg-white/10 hover:bg-white/20 transition-colors"
              title="Download File"
            >
              <Download className="w-4 h-4" />
            </button>
          )}

          <button
            onClick={onClose}
            className="p-2 rounded-xl bg-white/10 hover:bg-rose-500/20 hover:text-rose-400 transition-colors"
            title="Close Preview (Esc)"
          >
            <X className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* Main Preview Container */}
      <div className="w-full h-full max-w-5xl max-h-[85vh] flex items-center justify-center p-6 mt-8">
        {isLoading ? (
          <div className="flex flex-col items-center gap-3 text-brand-textSecondary">
            <div className="w-8 h-8 border-2 border-brand-blue border-t-transparent rounded-full animate-spin" />
            <span className="text-xs font-semibold">Loading media preview...</span>
          </div>
        ) : isImage ? (
          <div className="overflow-auto max-w-full max-h-full flex items-center justify-center p-2">
            <img
              src={dataUrl || file.path}
              alt={file.name}
              style={{
                transform: `scale(${zoom}) rotate(${rotation}deg)`,
                transition: 'transform 0.15s ease-out',
              }}
              className="max-w-full max-h-[80vh] object-contain rounded-xl shadow-2xl"
            />
          </div>
        ) : isVideo ? (
          <video
            src={dataUrl || file.path}
            controls
            autoPlay
            className="max-w-full max-h-[80vh] rounded-2xl shadow-2xl"
          />
        ) : isAudio ? (
          <div className="p-8 rounded-3xl bg-brand-surface border border-brand-border flex flex-col items-center gap-4 shadow-2xl">
            <div className="w-16 h-16 rounded-2xl bg-purple-500/20 text-purple-400 flex items-center justify-center font-bold text-2xl">
              🎵
            </div>
            <span className="text-base font-bold text-white">{file.name}</span>
            <audio src={dataUrl || file.path} controls autoPlay className="w-72" />
          </div>
        ) : (
          <div className="flex flex-col items-center gap-3 text-center">
            <span className="text-4xl">📄</span>
            <span className="text-lg font-bold text-white">{file.name}</span>
            <span className="text-xs text-brand-textSecondary">
              Preview not available for this file type.
            </span>
            {onOpenSystem && (
              <button
                onClick={() => onOpenSystem(file)}
                className="mt-2 px-4 py-2 rounded-xl bg-brand-blue text-white text-xs font-bold hover:bg-brand-blueHover transition-colors"
              >
                Open with System Viewer
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
