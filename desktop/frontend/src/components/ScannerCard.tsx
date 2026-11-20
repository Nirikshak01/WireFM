import React, { useState } from 'react'
import { Info, Copy, Check, RefreshCw } from 'lucide-react'
import { QrData } from '../types'

interface ScannerCardProps {
  qrData: QrData | null
  isLoading: boolean
  onRefreshQr: () => void
  onSimulateConnect?: () => void
}

export const ScannerCard: React.FC<ScannerCardProps> = ({
  qrData,
  isLoading,
  onRefreshQr,
  onSimulateConnect,
}) => {
  const [copiedField, setCopiedField] = useState<'url' | 'pin' | null>(null)

  const handleCopy = (text: string, field: 'url' | 'pin') => {
    navigator.clipboard.writeText(text)
    setCopiedField(field)
    setTimeout(() => setCopiedField(null), 2000)
  }

  return (
    <div className="flex flex-col items-center justify-center h-full w-full select-none p-6 animate-fadeIn">
      {/* Centered QR Box matching mockup uploaded_media_1795157153856.png */}
      <div className="relative w-64 h-64 md:w-72 md:h-72 rounded-[32px] bg-[#CCD8DA] shadow-2xl flex flex-col items-center justify-center p-6 transition-all duration-300 hover:scale-[1.01]">
        {isLoading ? (
          <div className="flex flex-col items-center justify-center gap-3 text-[#1A1B22]">
            <RefreshCw className="w-8 h-8 animate-spin" />
            <span className="text-sm font-bold tracking-tight">Generating QR...</span>
          </div>
        ) : qrData?.qr_base64 ? (
          <div className="w-full h-full flex flex-col items-center justify-center">
            <img
              src={qrData.qr_base64}
              alt="WireFM Pairing QR"
              className="w-full h-full object-contain rounded-2xl"
            />
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center text-[#1A1B22]">
            <span className="text-2xl font-black tracking-tight">Scanner</span>
            <span className="text-xs font-semibold mt-1 opacity-75">WireFM Instant Connect</span>
          </div>
        )}
      </div>

      {/* Info Line matching Mockup: "(i) Connect with WireFM Scan with your Mobile" */}
      <div className="mt-8 flex items-center gap-2 text-white/90 text-sm md:text-base font-bold tracking-tight">
        <div className="w-5 h-5 rounded-full border-2 border-white/80 flex items-center justify-center text-xs font-extrabold flex-shrink-0">
          i
        </div>
        <span>Connect with WireFM Scan with your Mobile</span>
      </div>

      {/* WireFM Pill Badge matching Mockup */}
      <div className="mt-6">
        <div className="inline-flex items-center gap-2.5 px-6 py-2.5 rounded-full bg-white text-black font-extrabold text-base tracking-tight shadow-lg shadow-white/5">
          <svg
            className="w-5 h-5"
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

      {/* Manual Connection Fallback Details */}
      {qrData && (
        <div className="mt-8 flex flex-wrap items-center justify-center gap-3 text-xs">
          <button
            onClick={() => handleCopy(`http://${qrData.ip}:${qrData.port}`, 'url')}
            className="flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-brand-surface border border-brand-border/80 text-brand-textSecondary hover:text-white hover:border-brand-blue/50 transition-all active:scale-95"
            title="Click to copy IP & Port"
          >
            <span>{qrData.ip}:{qrData.port}</span>
            {copiedField === 'url' ? (
              <Check className="w-3.5 h-3.5 text-emerald-400" />
            ) : (
              <Copy className="w-3.5 h-3.5 opacity-60" />
            )}
          </button>

          <button
            onClick={() => handleCopy(qrData.pin, 'pin')}
            className="flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-brand-surface border border-brand-border/80 text-brand-textSecondary hover:text-white hover:border-brand-blue/50 transition-all active:scale-95"
            title="Click to copy PIN"
          >
            <span>PIN: {qrData.pin}</span>
            {copiedField === 'pin' ? (
              <Check className="w-3.5 h-3.5 text-emerald-400" />
            ) : (
              <Copy className="w-3.5 h-3.5 opacity-60" />
            )}
          </button>

          <button
            onClick={onRefreshQr}
            className="p-1.5 rounded-full bg-brand-surface border border-brand-border/80 text-brand-textSecondary hover:text-white transition-all active:scale-95"
            title="Regenerate QR"
          >
            <RefreshCw className="w-3.5 h-3.5" />
          </button>

          {onSimulateConnect && (
            <button
              onClick={onSimulateConnect}
              className="px-3.5 py-1.5 rounded-full bg-brand-blue/10 border border-brand-blue/30 text-brand-blue hover:bg-brand-blue/20 font-semibold transition-all active:scale-95"
            >
              Preview File Browser
            </button>
          )}
        </div>
      )}
    </div>
  )
}
