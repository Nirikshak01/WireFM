import React from 'react'
import {
  FileText,
  FileCode,
  Image as ImageIcon,
  Music,
  Video,
  Archive,
  File as FileGeneric,
  Folder,
} from 'lucide-react'
import { FileEntry, ViewMode } from '../types'

interface FileBrowserProps {
  files: FileEntry[]
  viewMode: ViewMode
  isLoading: boolean
  onOpenFolder: (folder: FileEntry) => void
  onOpenFile: (file: FileEntry) => void
  onContextMenu: (e: React.MouseEvent, file: FileEntry) => void
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

  if (codeExts.includes(ext)) {
    return <FileCode className="w-8 h-8 text-blue-400" />
  }
  if (imgExts.includes(ext)) {
    return <ImageIcon className="w-8 h-8 text-emerald-400" />
  }
  if (audioExts.includes(ext)) {
    return <Music className="w-8 h-8 text-purple-400" />
  }
  if (videoExts.includes(ext)) {
    return <Video className="w-8 h-8 text-rose-400" />
  }
  if (archiveExts.includes(ext)) {
    return <Archive className="w-8 h-8 text-amber-400" />
  }
  if (ext === 'pdf' || ext === 'doc' || ext === 'docx' || ext === 'txt' || ext === 'md') {
    return <FileText className="w-8 h-8 text-amber-300" />
  }
  return <FileGeneric className="w-8 h-8 text-brand-textTertiary" />
}

export const FileBrowser: React.FC<FileBrowserProps> = ({
  files,
  viewMode,
  isLoading,
  onOpenFolder,
  onOpenFile,
  onContextMenu,
}) => {
  if (isLoading) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center text-brand-textTertiary">
        <div className="w-8 h-8 border-2 border-brand-blue border-t-transparent rounded-full animate-spin mb-3" />
        <span className="text-sm font-semibold">Loading directory contents...</span>
      </div>
    )
  }

  if (files.length === 0) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center text-brand-textTertiary select-none">
        <Folder className="w-12 h-12 opacity-30 mb-2" />
        <span className="text-base font-bold text-brand-textSecondary">Empty Folder</span>
        <span className="text-xs mt-1">Drag and drop files to upload</span>
      </div>
    )
  }

  if (viewMode === 'list') {
    return (
      <div className="flex-1 overflow-y-auto p-4 select-none">
        <div className="divide-y divide-brand-border/40">
          {files.map((file) => (
            <div
              key={file.path}
              onDoubleClick={() => (file.is_dir ? onOpenFolder(file) : onOpenFile(file))}
              onContextMenu={(e) => onContextMenu(e, file)}
              className="flex items-center justify-between px-4 py-2.5 hover:bg-white/[0.04] rounded-xl cursor-pointer transition-colors group"
            >
              <div className="flex items-center gap-3 truncate pr-4">
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
          ))}
        </div>
      </div>
    )
  }

  // 3-Column Grid matching Figma Mockup
  return (
    <div className="flex-1 overflow-y-auto p-6 select-none">
      <div className="grid grid-cols-3 gap-6 auto-rows-max">
        {files.map((file) => {
          if (file.is_dir) {
            // Figma Styled Tabbed Folder Card (#CCD8DA)
            return (
              <div
                key={file.path}
                onDoubleClick={() => onOpenFolder(file)}
                onContextMenu={(e) => onContextMenu(e, file)}
                className="flex flex-col items-center group cursor-pointer"
              >
                {/* Folder Graphic */}
                <div className="relative w-full aspect-[4/3] rounded-2xl bg-[#CCD8DA] shadow-md transition-all duration-200 group-hover:scale-[1.02] group-hover:shadow-lg flex items-center justify-center overflow-hidden">
                  {/* Subtle folder tab top-left notch */}
                  <div className="absolute top-0 left-0 w-1/3 h-2.5 bg-[#B8C8CC] rounded-br-lg" />
                  <Folder className="w-12 h-12 text-[#2C3337] fill-[#2C3337]/20 transition-transform group-hover:scale-105" />
                </div>
                {/* Folder Label */}
                <span className="mt-2 text-sm font-bold text-center text-brand-textPrimary group-hover:text-white truncate w-full px-1">
                  {file.name}
                </span>
              </div>
            )
          }

          // File Card
          return (
            <div
              key={file.path}
              onDoubleClick={() => onOpenFile(file)}
              onContextMenu={(e) => onContextMenu(e, file)}
              className="flex flex-col items-center group cursor-pointer"
            >
              <div className="relative w-full aspect-[4/3] rounded-2xl bg-brand-surface/70 border border-brand-border/70 shadow-sm transition-all duration-200 group-hover:scale-[1.02] group-hover:border-brand-blue/50 flex flex-col items-center justify-center p-3">
                {getFileIcon(file.ext)}
                <span className="text-[11px] font-semibold text-brand-textTertiary mt-2">
                  {formatBytes(file.size)}
                </span>
              </div>
              <span className="mt-2 text-xs font-bold text-center text-brand-textSecondary group-hover:text-white truncate w-full px-1" title={file.name}>
                {file.name}
              </span>
            </div>
          )
        })}
      </div>
    </div>
  )
}
