import React from 'react'
import { ChevronLeft, RotateCw, Search, Grid, List } from 'lucide-react'
import { ViewMode } from '../types'

interface BreadcrumbBarProps {
  currentPath: string
  onNavigatePath: (path: string) => void
  onGoBack: () => void
  canGoBack: boolean
  onRefresh: () => void
  isRefreshing: boolean
  searchQuery: string
  onSearchChange: (query: string) => void
  viewMode: ViewMode
  onToggleViewMode: () => void
}

export const BreadcrumbBar: React.FC<BreadcrumbBarProps> = ({
  currentPath,
  onNavigatePath,
  onGoBack,
  canGoBack,
  onRefresh,
  isRefreshing,
  searchQuery,
  onSearchChange,
  viewMode,
  onToggleViewMode,
}) => {
  // Split path into segments
  const cleanPath = currentPath.replace(/\/+/g, '/').replace(/^\/|\/$/g, '')
  const segments = cleanPath ? cleanPath.split('/') : []

  const handleCrumbClick = (index: number) => {
    const targetPath = '/' + segments.slice(0, index + 1).join('/')
    onNavigatePath(targetPath)
  }

  return (
    <div className="flex items-center justify-between gap-4 px-6 py-3 border-b border-brand-border/60 bg-brand-surface/50 backdrop-blur-md select-none">
      {/* Navigation buttons & Breadcrumbs */}
      <div className="flex items-center gap-3 overflow-hidden flex-1">
        <div className="flex items-center gap-1 flex-shrink-0">
          <button
            onClick={onGoBack}
            disabled={!canGoBack}
            className={`p-1.5 rounded-lg transition-all ${
              canGoBack
                ? 'text-white hover:bg-white/10 active:scale-95'
                : 'text-brand-textTertiary cursor-not-allowed opacity-40'
            }`}
            title="Go Back"
          >
            <ChevronLeft className="w-4 h-4" />
          </button>
          <button
            onClick={onRefresh}
            className="p-1.5 rounded-lg text-brand-textSecondary hover:text-white hover:bg-white/10 active:scale-95 transition-all"
            title="Refresh Directory"
          >
            <RotateCw className={`w-4 h-4 ${isRefreshing ? 'animate-spin text-brand-blue' : ''}`} />
          </button>
        </div>

        {/* Clickable breadcrumbs: root > home > jasmin */}
        <div className="flex items-center gap-1.5 text-sm font-bold tracking-tight text-brand-textSecondary overflow-x-auto no-scrollbar py-1">
          <button
            onClick={() => onNavigatePath('/')}
            className="hover:text-white transition-colors flex-shrink-0"
          >
            root
          </button>
          {segments.map((seg, idx) => {
            const isLast = idx === segments.length - 1
            return (
              <React.Fragment key={idx}>
                <span className="text-brand-textTertiary text-xs">{'>'}</span>
                <button
                  onClick={() => handleCrumbClick(idx)}
                  className={`hover:text-white transition-colors truncate max-w-[120px] ${
                    isLast ? 'text-white font-extrabold' : ''
                  }`}
                  title={seg}
                >
                  {seg}
                </button>
              </React.Fragment>
            )
          })}
        </div>
      </div>

      {/* Search Bar & View Mode Toggle */}
      <div className="flex items-center gap-3 flex-shrink-0">
        <div className="relative">
          <Search className="w-3.5 h-3.5 absolute left-3 top-1/2 -translate-y-1/2 text-brand-textTertiary" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Search files..."
            className="w-40 sm:w-56 pl-8 pr-3 py-1.5 rounded-xl bg-brand-bg/80 border border-brand-border text-xs text-white placeholder-brand-textTertiary focus:outline-none focus:border-brand-blue/70 transition-colors"
          />
        </div>

        <button
          onClick={onToggleViewMode}
          className="p-1.5 rounded-lg text-brand-textSecondary hover:text-white hover:bg-white/10 active:scale-95 transition-all"
          title={viewMode === 'grid' ? 'Switch to List View' : 'Switch to Grid View'}
        >
          {viewMode === 'grid' ? (
            <List className="w-4 h-4" />
          ) : (
            <Grid className="w-4 h-4" />
          )}
        </button>
      </div>
    </div>
  )
}
