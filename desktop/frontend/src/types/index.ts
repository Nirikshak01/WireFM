export interface QrData {
  qr_base64: string
  url: string
  ip: string
  port: string
  pin: string
}

export interface ConnectionInfo {
  is_connected: boolean
  device_name: string
  device_ip: string
  status: string
}

export interface FileEntry {
  name: string
  path: string
  is_dir: boolean
  size: number
  mod_time: string
  ext: string
}

export type Place = 'home' | 'desktop' | 'documents' | 'downloads' | 'music' | 'pictures' | 'videos' | 'trash'

export type ViewMode = 'grid' | 'list'

export interface ConnectedDevice {
  id: string
  name: string
  ip: string
  status: string
  last_seen?: string
}

export interface ClipboardState {
  action: 'copy' | 'cut'
  files: FileEntry[]
  sourcePath: string
}

export interface MediaPreviewItem {
  file: FileEntry
  url?: string
}

export interface BreadcrumbItem {
  name: string
  path: string
}
