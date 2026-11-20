import React from 'react'
import {
  Home,
  Monitor,
  FileText,
  Download,
  Music,
  Image as ImageIcon,
  Video,
  Trash2,
  Smartphone,
  QrCode,
} from 'lucide-react'
import { Place, ConnectionInfo } from '../types'

interface SidebarProps {
  activePlace: Place | 'device' | 'scanner'
  onSelectPlace: (place: Place) => void
  onSelectDevice: () => void
  onSelectScanner: () => void
  connectionInfo: ConnectionInfo
}

export const Sidebar: React.FC<SidebarProps> = ({
  activePlace,
  onSelectPlace,
  onSelectDevice,
  onSelectScanner,
  connectionInfo,
}) => {
  const places: { id: Place; label: string; icon: React.ReactNode }[] = [
    { id: 'home', label: 'Home', icon: <Home className="w-4 h-4" /> },
    { id: 'desktop', label: 'Desktop', icon: <Monitor className="w-4 h-4" /> },
    { id: 'documents', label: 'Documents', icon: <FileText className="w-4 h-4" /> },
    { id: 'downloads', label: 'Downloads', icon: <Download className="w-4 h-4" /> },
    { id: 'music', label: 'Music', icon: <Music className="w-4 h-4" /> },
    { id: 'pictures', label: 'Pictures', icon: <ImageIcon className="w-4 h-4" /> },
    { id: 'videos', label: 'Videos', icon: <Video className="w-4 h-4" /> },
    { id: 'trash', label: 'Trash', icon: <Trash2 className="w-4 h-4" /> },
  ]

  return (
    <aside className="w-56 bg-brand-sidebar border-r border-brand-border/60 flex flex-col justify-between p-3 select-none flex-shrink-0 h-full">
      <div className="flex flex-col gap-4">
        {/* Brand Pill matching Mockup */}
        <div className="pt-1 px-1">
          <div
            onClick={onSelectScanner}
            className="inline-flex items-center gap-2.5 px-3.5 py-1.5 rounded-full bg-white text-black font-extrabold text-sm tracking-tight cursor-pointer shadow-sm hover:scale-[1.02] active:scale-[0.98] transition-transform"
          >
            {/* Origami / Ninja logo SVG */}
            <svg
              className="w-4 h-4"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <polygon points="12 2 2 7 12 12 22 7 12 2" />
              <polyline points="2 17 12 22 22 17" />
              <polyline points="2 12 12 17 22 12" />
            </svg>
            <span>WireFM</span>
          </div>
        </div>

        {/* Places Section */}
        <div className="flex flex-col gap-1">
          <div className="text-[11px] font-bold text-brand-textTertiary uppercase tracking-wider px-3 mb-1">
            Places
          </div>
          <div className="flex flex-col gap-0.5">
            {places.map((place) => {
              const isActive = activePlace === place.id
              return (
                <button
                  key={place.id}
                  onClick={() => onSelectPlace(place.id)}
                  className={`flex items-center gap-3 px-3 py-1.5 rounded-xl text-sm font-semibold transition-all duration-150 ${
                    isActive
                      ? 'bg-[#25272C] text-white shadow-sm'
                      : 'text-brand-textSecondary hover:bg-white/[0.04] hover:text-white'
                  }`}
                >
                  <span className={`${isActive ? 'text-white' : 'text-brand-textTertiary'}`}>
                    {place.icon}
                  </span>
                  <span>{place.label}</span>
                </button>
              )
            })}
          </div>
        </div>

        {/* Devices Section */}
        <div className="flex flex-col gap-1 mt-1">
          <div className="text-[11px] font-bold text-brand-textTertiary uppercase tracking-wider px-3 mb-1">
            Devices
          </div>
          <button
            onClick={() => {
              if (connectionInfo.is_connected) {
                onSelectDevice()
              } else {
                onSelectScanner()
              }
            }}
            className={`flex items-center justify-between px-3 py-2 rounded-xl text-sm font-semibold transition-all duration-150 ${
              activePlace === 'device' || (activePlace === 'scanner' && !connectionInfo.is_connected)
                ? 'bg-[#25272C] text-white shadow-sm'
                : 'text-brand-textSecondary hover:bg-white/[0.04] hover:text-white'
            }`}
          >
            <div className="flex items-center gap-3 truncate">
              <span className="text-brand-textTertiary">
                <Smartphone className="w-4 h-4" />
              </span>
              <span className="truncate">
                {connectionInfo.device_name || 'Vivo y36'}
              </span>
            </div>
            {connectionInfo.is_connected ? (
              <span className="w-2 h-2 rounded-full bg-emerald-400 ring-4 ring-emerald-400/20 animate-pulse flex-shrink-0" />
            ) : (
              <span className="text-xs text-brand-textTertiary flex items-center gap-1">
                <QrCode className="w-3.5 h-3.5 text-brand-textTertiary" />
              </span>
            )}
          </button>
        </div>
      </div>

      {/* Footer Info / Version */}
      <div className="px-3 py-2 text-xs text-brand-textTertiary border-t border-brand-border/40 flex items-center justify-between">
        <span>v2.0.0</span>
        <button
          onClick={onSelectScanner}
          className="text-xs hover:text-white transition-colors"
          title="Pair New Device"
        >
          Pair QR
        </button>
      </div>
    </aside>
  )
}
