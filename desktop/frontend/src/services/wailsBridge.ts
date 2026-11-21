import { QrData, ConnectionInfo, FileEntry, ConnectedDevice } from '../types'

// Wails bindings interface
interface WailsApp {
  GetQrCodeData: () => Promise<QrData>
  GetConnectionStatus: () => Promise<ConnectionInfo>
  SetConnectedDevice: (deviceName: string, deviceIP: string) => Promise<void>
  DisconnectDevice: () => Promise<void>
  GetConnectedDevices: () => Promise<ConnectedDevice[]>
  RemoveConnectedDevice: (id: string) => Promise<void>
  ListLocalDirectory: (path: string) => Promise<FileEntry[]>
  ListRemoteDirectory: (path: string) => Promise<FileEntry[]>
  UploadFiles: (targetDir: string, filePaths: string[]) => Promise<void>
  DownloadFile: (remotePath: string, localTargetDir: string) => Promise<void>
  OpenPath: (path: string) => Promise<void>
  RenameFile: (oldPath: string, newName: string) => Promise<void>
  DeleteFile: (path: string) => Promise<void>
  CreateFolder: (parentPath: string, folderName: string) => Promise<void>
  CopyFile: (srcPath: string, destDir: string) => Promise<void>
  MoveFile: (srcPath: string, destDir: string) => Promise<void>
  OpenWith: (path: string, appName: string) => Promise<void>
  GetFileContentBase64: (path: string) => Promise<string>
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

  async getConnectedDevices(): Promise<ConnectedDevice[]> {
    const app = getWailsApp()
    if (app) {
      try {
        const res = await app.GetConnectedDevices()
        return Array.isArray(res) ? res : []
      } catch (err) {
        console.error('Failed to get connected devices:', err)
        return []
      }
    }
    return [
      { id: 'dev_vivo', name: 'Vivo y36', ip: '192.168.1.42', status: 'Connected', last_seen: 'Just now' }
    ]
  },

  async removeConnectedDevice(id: string): Promise<void> {
    const app = getWailsApp()
    if (app) {
      return await app.RemoveConnectedDevice(id)
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
      try {
        const res = await app.ListLocalDirectory(path)
        return Array.isArray(res) ? res : []
      } catch (err) {
        console.error('Failed to list local directory:', err)
        return []
      }
    }
    // Fallback mock entries
    return [
      { name: 'Desktop', path: '/home/user/Desktop', is_dir: true, size: 0, mod_time: '', ext: '' },
      { name: 'Documents', path: '/home/user/Documents', is_dir: true, size: 0, mod_time: '', ext: '' },
      { name: 'Downloads', path: '/home/user/Downloads', is_dir: true, size: 0, mod_time: '', ext: '' },
      { name: 'Music', path: '/home/user/Music', is_dir: true, size: 0, mod_time: '', ext: '' },
      { name: 'Pictures', path: '/home/user/Pictures', is_dir: true, size: 0, mod_time: '', ext: '' },
      { name: 'Videos', path: '/home/user/Videos', is_dir: true, size: 0, mod_time: '', ext: '' },
      { name: 'Wallpapers', path: '/home/user/Wallpapers', is_dir: true, size: 0, mod_time: '', ext: '' },
      { name: 'photo.jpg', path: '/home/user/photo.jpg', is_dir: false, size: 3400000, mod_time: '', ext: 'jpg' },
      { name: 'video.mp4', path: '/home/user/video.mp4', is_dir: false, size: 18400000, mod_time: '', ext: 'mp4' },
      { name: 'audio.mp3', path: '/home/user/audio.mp3', is_dir: false, size: 4500000, mod_time: '', ext: 'mp3' },
      { name: 'document.pdf', path: '/home/user/document.pdf', is_dir: false, size: 2048500, mod_time: '', ext: 'pdf' },
    ]
  },

  async listRemoteDirectory(path: string): Promise<FileEntry[]> {
    const app = getWailsApp()
    if (app) {
      try {
        const res = await app.ListRemoteDirectory(path)
        return Array.isArray(res) ? res : []
      } catch (err) {
        console.error('Failed to list remote directory:', err)
        return []
      }
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

  async renameFile(oldPath: string, newName: string): Promise<void> {
    const app = getWailsApp()
    if (app) {
      return await app.RenameFile(oldPath, newName)
    }
  },

  async deleteFile(path: string): Promise<void> {
    const app = getWailsApp()
    if (app) {
      return await app.DeleteFile(path)
    }
  },

  async createFolder(parentPath: string, folderName: string): Promise<void> {
    const app = getWailsApp()
    if (app) {
      return await app.CreateFolder(parentPath, folderName)
    }
  },

  async copyFile(srcPath: string, destDir: string): Promise<void> {
    const app = getWailsApp()
    if (app) {
      return await app.CopyFile(srcPath, destDir)
    }
  },

  async moveFile(srcPath: string, destDir: string): Promise<void> {
    const app = getWailsApp()
    if (app) {
      return await app.MoveFile(srcPath, destDir)
    }
  },

  async openWith(path: string, appName: string): Promise<void> {
    const app = getWailsApp()
    if (app) {
      return await app.OpenWith(path, appName)
    }
  },

  async getFileContentBase64(path: string): Promise<string> {
    const app = getWailsApp()
    if (app) {
      return await app.GetFileContentBase64(path)
    }
    return ''
  },
}
