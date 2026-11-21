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
  Plus,
  X,
} from 'lucide-react'
import { Place, ConnectedDevice } from '../types'

interface SidebarProps {
  activePlace: Place | 'device' | 'scanner'
  activeDeviceId: string | null
  onSelectPlace: (place: Place) => void
  onSelectDevice: (device: ConnectedDevice) => void
  onDisconnectDevice: (deviceId: string) => void
  onSelectScanner: () => void
  connectedDevices: ConnectedDevice[]
}

export const Sidebar: React.FC<SidebarProps> = ({
  activePlace,
  activeDeviceId,
  onSelectPlace,
  onSelectDevice,
  onDisconnectDevice,
  onSelectScanner,
  connectedDevices,
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
        {/* Brand Pill with Official 3 Stacked Wireframe Layers Logo */}
        <div className="pt-1 px-1">
          <div
            onClick={onSelectScanner}
            className="inline-flex items-center gap-2.5 px-3.5 py-1.5 rounded-full bg-white text-black font-extrabold text-sm tracking-tight cursor-pointer shadow-sm hover:scale-[1.02] active:scale-[0.98] transition-transform"
          >
            {/* 3 Stacked Isometric Wireframe Layers */}
            <svg
              className="w-4 h-4"
              viewBox="0 0 24 24"
              fill="none"
              stroke="#111111"
              strokeWidth="2.2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M12 4.1 18.7 7.8 12 11.5 5.3 7.8 12 4.1Z" />
              <path d="m5.3 11.2 6.7 3.7 6.7-3.7" />
              <path d="m5.3 14.6 6.7 3.7 6.7-3.7" />
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

        {/* Devices Section matching user request */}
        <div className="flex flex-col gap-1 mt-1">
          <div className="text-[11px] font-bold text-brand-textTertiary uppercase tracking-wider px-3 mb-1">
            Devices
          </div>

          {/* "+ Add Device" Button (Opens Scanner Card) */}
          <button
            onClick={onSelectScanner}
            className={`flex items-center gap-3 px-3 py-1.5 rounded-xl text-sm font-semibold transition-all duration-150 ${
              activePlace === 'scanner'
                ? 'bg-[#25272C] text-white shadow-sm'
                : 'text-brand-blue hover:bg-brand-blue/10'
            }`}
          >
            <div className="w-5 h-5 rounded-lg bg-brand-blue/20 flex items-center justify-center text-brand-blue flex-shrink-0">
              <Plus className="w-3.5 h-3.5" />
            </div>
            <span>Add Device</span>
          </button>

          {/* List of Connected Devices */}
          <div className="flex flex-col gap-0.5 mt-1">
            {(connectedDevices || []).map((dev) => {
              const isActive = activePlace === 'device' && activeDeviceId === dev.id
              return (
                <div
                  key={dev.id}
                  onClick={() => onSelectDevice(dev)}
                  className={`group flex items-center justify-between px-3 py-2 rounded-xl text-sm font-semibold cursor-pointer transition-all duration-150 ${
                    isActive
                      ? 'bg-[#25272C] text-white shadow-sm'
                      : 'text-brand-textSecondary hover:bg-white/[0.04] hover:text-white'
                  }`}
                >
                  <div className="flex items-center gap-3 truncate">
                    <span className="text-brand-textTertiary">
                      <Smartphone className="w-4 h-4" />
                    </span>
                    <span className="truncate">{dev.name}</span>
                  </div>

                  <div className="flex items-center gap-1.5">
                    <span className="w-2 h-2 rounded-full bg-emerald-400 ring-4 ring-emerald-400/20 animate-pulse flex-shrink-0" />
                    <button
                      onClick={(e) => {
                        e.stopPropagation()
                        onDisconnectDevice(dev.id)
                      }}
                      className="opacity-0 group-hover:opacity-100 p-0.5 rounded hover:bg-white/10 text-brand-textTertiary hover:text-rose-400 transition-all"
                      title="Disconnect device"
                    >
                      <X className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              )
            })}

            {(!connectedDevices || connectedDevices.length === 0) && activePlace !== 'scanner' && (
              <div className="px-3 py-1 text-xs text-brand-textTertiary italic">
                No device connected
              </div>
            )}
          </div>
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
          Scan QR
        </button>
      </div>
    </aside>
  )
}
