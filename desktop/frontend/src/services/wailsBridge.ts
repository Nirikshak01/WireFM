import { QrData, ConnectionInfo, FileEntry } from '../types'

// Wails bindings interface
interface WailsApp {
  GetQrCodeData: () => Promise<QrData>
  GetConnectionStatus: () => Promise<ConnectionInfo>
  SetConnectedDevice: (deviceName: string, deviceIP: string) => Promise<void>
  DisconnectDevice: () => Promise<void>
  ListLocalDirectory: (path: string) => Promise<FileEntry[]>
  ListRemoteDirectory: (path: string) => Promise<FileEntry[]>
  UploadFiles: (targetDir: string, filePaths: string[]) => Promise<void>
  DownloadFile: (remotePath: string, localTargetDir: string) => Promise<void>
  OpenPath: (path: string) => Promise<void>
}

function getWailsApp(): WailsApp | null {
  if (typeof window !== 'undefined' && (window as any).go?.main?.App) {
    return (window as any).go.main.App as WailsApp
  }
  return null
}

export const WailsBridge = {
  isWailsAvailable(): boolean {
    return getWailsApp() !== null
  },

  async getQrCodeData(): Promise<QrData> {
    const app = getWailsApp()
    if (app) {
      return await app.GetQrCodeData()
    }
    // Fallback mock QR for preview mode
    return {
      qr_base64: '',
      url: 'http://192.168.1.5:8080/?password=739281',
      ip: '192.168.1.5',
      port: '8080',
      pin: '739281',
    }
  },

  async getConnectionStatus(): Promise<ConnectionInfo> {
    const app = getWailsApp()
    if (app) {
      return await app.GetConnectionStatus()
    }
    return {
      is_connected: false,
      device_name: 'Vivo y36',
      device_ip: '192.168.1.42',
      status: 'Waiting for scan',
    }
  },

  async setConnectedDevice(deviceName: string, deviceIP: string): Promise<void> {
    const app = getWailsApp()
    if (app) {
      return await app.SetConnectedDevice(deviceName, deviceIP)
    }
  },

  async disconnectDevice(): Promise<void> {
    const app = getWailsApp()
    if (app) {
      return await app.DisconnectDevice()
    }
  },

  async listLocalDirectory(path: string): Promise<FileEntry[]> {
    const app = getWailsApp()
    if (app) {
      return await app.ListLocalDirectory(path)
    }
    // Fallback mock entries
    return [
      { name: 'Desktop', path: '/home/user/Desktop', is_dir: true, size: 0, mod_time: '', ext: '' },
      { name: 'Documents', path: '/home/user/Documents', is_dir: true, size: 0, mod_time: '', ext: '' },
      { name: 'Downloads', path: '/home/user/Downloads', is_dir: true, size: 0, mod_time: '', ext: '' },
      { name: 'Music', path: '/home/user/Music', is_dir: true, size: 0, mod_time: '', ext: '' },
      { name: 'Pictures', path: '/home/user/Pictures', is_dir: true, size: 0, mod_time: '', ext: '' },
      { name: 'Videos', path: '/home/user/Videos', is_dir: true, size: 0, mod_time: '', ext: '' },
      { name: 'Projects', path: '/home/user/Projects', is_dir: true, size: 0, mod_time: '', ext: '' },
      { name: 'notes.txt', path: '/home/user/notes.txt', is_dir: false, size: 1024, mod_time: '', ext: 'txt' },
      { name: 'report.pdf', path: '/home/user/report.pdf', is_dir: false, size: 2048500, mod_time: '', ext: 'pdf' },
      { name: 'app.apk', path: '/home/user/app.apk', is_dir: false, size: 14500000, mod_time: '', ext: 'apk' },
      { name: 'photo.jpg', path: '/home/user/photo.jpg', is_dir: false, size: 3400000, mod_time: '', ext: 'jpg' },
    ]
  },

  async listRemoteDirectory(path: string): Promise<FileEntry[]> {
    const app = getWailsApp()
    if (app) {
      return await app.ListRemoteDirectory(path)
    }
    return [
      { name: 'DCIM', path: '/storage/emulated/0/DCIM', is_dir: true, size: 0, mod_time: '', ext: '' },
      { name: 'Download', path: '/storage/emulated/0/Download', is_dir: true, size: 0, mod_time: '', ext: '' },
      { name: 'Pictures', path: '/storage/emulated/0/Pictures', is_dir: true, size: 0, mod_time: '', ext: '' },
      { name: 'Documents', path: '/storage/emulated/0/Documents', is_dir: true, size: 0, mod_time: '', ext: '' },
      { name: 'Music', path: '/storage/emulated/0/Music', is_dir: true, size: 0, mod_time: '', ext: '' },
      { name: 'Movies', path: '/storage/emulated/0/Movies', is_dir: true, size: 0, mod_time: '', ext: '' },
    ]
  },

  async uploadFiles(targetDir: string, filePaths: string[]): Promise<void> {
    const app = getWailsApp()
    if (app) {
      return await app.UploadFiles(targetDir, filePaths)
    }
  },

  async downloadFile(remotePath: string, localTargetDir: string): Promise<void> {
    const app = getWailsApp()
    if (app) {
      return await app.DownloadFile(remotePath, localTargetDir)
    }
  },

  async openPath(path: string): Promise<void> {
    const app = getWailsApp()
    if (app) {
      return await app.OpenPath(path)
    }
  },
}
