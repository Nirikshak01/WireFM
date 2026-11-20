import React from 'react'
import { UploadCloud } from 'lucide-react'

interface DropOverlayProps {
  isDragging: boolean
  targetFolderName: string
}

export const DropOverlay: React.FC<DropOverlayProps> = ({ isDragging, targetFolderName }) => {
  if (!isDragging) return null

  return (
    <div className="absolute inset-0 z-50 pointer-events-none flex items-center justify-center bg-brand-bg/80 backdrop-blur-sm animate-fadeIn p-8">
      <div className="w-full h-full rounded-3xl border-2 border-dashed border-brand-blue bg-brand-blue/5 flex flex-col items-center justify-center gap-4 text-center p-6 shadow-2xl">
        <div className="w-20 h-20 rounded-full bg-brand-blue/20 border border-brand-blue/40 flex items-center justify-center text-brand-blue animate-bounce">
          <UploadCloud className="w-10 h-10" />
        </div>
        <div>
          <h3 className="text-xl font-extrabold text-white tracking-tight">
            Drop files to transfer
          </h3>
          <p className="text-sm text-brand-textSecondary mt-1">
            Uploading directly to <span className="text-brand-blue font-bold">{targetFolderName || 'current folder'}</span>
          </p>
        </div>
      </div>
    </div>
  )
}
