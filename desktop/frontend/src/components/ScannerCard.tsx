import React, { useState } from 'react'
import { Copy, Check, RefreshCw, QrCode, Sparkles } from 'lucide-react'
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
  const [isGenerated, setIsGenerated] = useState<boolean>(false)
  const [copiedField, setCopiedField] = useState<'url' | 'pin' | null>(null)

  const handleCopy = (text: string, field: 'url' | 'pin') => {
    navigator.clipboard.writeText(text)
    setCopiedField(field)
    setTimeout(() => setCopiedField(null), 2000)
  }

  const handleGenerate = () => {
    setIsGenerated(true)
    onRefreshQr()
  }

  return (
    <div className="flex flex-col items-center justify-center h-full w-full select-none p-6 animate-fadeIn">
      {/* Centered QR Box */}
      <div className="relative w-64 h-64 md:w-72 md:h-72 rounded-[32px] bg-[#CCD8DA] shadow-2xl flex flex-col items-center justify-center p-6 transition-all duration-300 hover:scale-[1.01]">
        {!isGenerated ? (
          <div className="flex flex-col items-center justify-center text-[#1A1B22] p-4 text-center">
            <div className="w-16 h-16 rounded-2xl bg-black/10 flex items-center justify-center mb-3">
              <QrCode className="w-9 h-9 text-[#1A1B22]" />
            </div>
            <span className="text-xl font-black tracking-tight">Add Device</span>
            <span className="text-xs font-semibold text-black/60 mt-1 mb-4 max-w-[200px]">
              Generate pairing QR code to connect your phone
            </span>
            <button
              onClick={handleGenerate}
              className="px-5 py-2.5 rounded-full bg-[#1A1B22] text-white text-xs font-bold tracking-tight shadow-md hover:bg-black transition-all active:scale-95 flex items-center gap-2"
            >
              <Sparkles className="w-3.5 h-3.5 text-blue-400" />
              <span>Generate QR</span>
            </button>
          </div>
        ) : isLoading ? (
          <div className="flex flex-col items-center justify-center gap-3 text-[#1A1B22]">
            <RefreshCw className="w-8 h-8 animate-spin" />
            <span className="text-sm font-bold tracking-tight">Generating QR...</span>
          </div>
        ) : qrData?.qr_base64 ? (
          <div className="w-full h-full flex flex-col items-center justify-center relative">
            <button
              onClick={onRefreshQr}
              className="absolute -top-2 -right-2 p-2 rounded-full bg-black/10 hover:bg-black/20 text-[#1A1B22] transition-colors"
              title="Regenerate QR / PIN"
            >
              <RefreshCw className="w-4 h-4" />
            </button>
            <img
              src={qrData.qr_base64}
              alt="WireFM Pairing QR"
              className="w-full h-full object-contain rounded-2xl"
            />
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center text-[#1A1B22]">
            <span className="text-2xl font-black tracking-tight">Scanner</span>
            <button
              onClick={handleGenerate}
              className="mt-3 px-4 py-2 rounded-full bg-[#1A1B22] text-white text-xs font-bold"
            >
              Retry Generate
            </button>
          </div>
        )}
      </div>

      {/* Info Line */}
      <div className="mt-8 flex items-center gap-2 text-white/90 text-sm md:text-base font-bold tracking-tight">
        <div className="w-5 h-5 rounded-full border-2 border-white/80 flex items-center justify-center text-xs font-extrabold flex-shrink-0">
          i
        </div>
        <span>Connect with WireFM Scan with your Mobile</span>
      </div>

      {/* WireFM Pill Badge */}
      <div className="mt-6">
        <div className="inline-flex items-center gap-2.5 px-6 py-2.5 rounded-full bg-white text-black font-extrabold text-base tracking-tight shadow-lg shadow-white/5">
          <svg
            className="w-5 h-5"
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

      {/* Manual Connection Fallback Details */}
      {isGenerated && qrData && (
        <div className="mt-8 flex flex-wrap items-center justify-center gap-3 text-xs animate-fadeIn">
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
        </div>
      )}
    </div>
  )
}
