package com.wirefm.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.wirefm.app.model.FileItem
import com.wirefm.app.model.PhoneCommand
import com.wirefm.app.network.WireFMClient
import com.wirefm.app.ui.components.FullScreenMediaViewer
import com.wirefm.app.ui.screens.FileManagerScreen
import com.wirefm.app.ui.theme.WireFMTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileManagerActivity : ComponentActivity() {

    private lateinit var ip: String
    private lateinit var port: String
    private lateinit var pass: String
    private lateinit var hostname: String
    private lateinit var user: String
    private lateinit var root: String

    // Reactive Compose States
    private var isPhoneStorage by mutableStateOf(false)
    private var currentPcPath by mutableStateOf("")
    private var currentPhoneDir by mutableStateOf(Environment.getExternalStorageDirectory() ?: File("/"))
    private var fileList by mutableStateOf<List<FileItem>>(emptyList())
    private var isLoading by mutableStateOf(false)

    // Clipboard
    private var clipboard by mutableStateOf<FileItem?>(null)
    private var clipboardList by mutableStateOf<List<FileItem>>(emptyList())
    private var clipAction by mutableStateOf("")
    private var clipboardSource by mutableStateOf("")

    // Dialog state
    private var showNewFolderDialog by mutableStateOf(false)
    private var newFolderName by mutableStateOf("")
    private var fileToRename by mutableStateOf<FileItem?>(null)
    private var renameText by mutableStateOf("")

    // Media preview & Download confirmation states
    private var previewMediaFile by mutableStateOf<FileItem?>(null)
    private var confirmDownloadFile by mutableStateOf<FileItem?>(null)

    private var heartbeatJob: Job? = null
    private var phoneWorkerJob: Job? = null

    // Multi-file picker for uploading from phone to PC
    private val pickFilesLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri>? ->
        if (!uris.isNullOrEmpty()) {
            uploadSelectedUris(uris)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ip = intent.getStringExtra("ip") ?: ""
        port = intent.getStringExtra("port") ?: "8080"
        pass = intent.getStringExtra("pass") ?: ""
        hostname = intent.getStringExtra("hostname") ?: "PC"
        user = intent.getStringExtra("user") ?: "User"
        root = intent.getStringExtra("root") ?: "/"

        currentPcPath = root

        val prefs = getSharedPreferences("wirefm", MODE_PRIVATE)
        val themeMode = prefs.getString("theme_mode", "system") ?: "system"

        setContent {
            WireFMTheme(themeMode = themeMode) {
                val colors = WireFMTheme.colors
                Box(modifier = Modifier.fillMaxSize()) {
                    val phoneDeviceName = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"

                    FileManagerScreen(
                        title = "$user@$hostname",
                        pcName = "PC Storage",
                        phoneName = phoneDeviceName,
                        currentPath = if (isPhoneStorage) currentPhoneDir.absolutePath else currentPcPath,
                        isPhoneStorage = isPhoneStorage,
                        files = fileList,
                        isLoading = isLoading,
                        clipboard = clipboard,
                        clipAction = clipAction,
                        onTabSelected = { selectedPhone ->
                            isPhoneStorage = selectedPhone
                            if (isPhoneStorage) {
                                if (checkStoragePermission()) {
                                    loadPhoneFiles(currentPhoneDir)
                                } else {
                                    requestStoragePermission()
                                }
                            } else {
                                navigateToPc(currentPcPath.ifEmpty { root })
                            }
                        },
                        onNavigateTo = { path ->
                            if (isPhoneStorage) {
                                loadPhoneFiles(File(path))
                            } else {
                                navigateToPc(path)
                            }
                        },
                        onFileClick = { file ->
                            if (file.isDir) {
                                if (isPhoneStorage) {
                                    loadPhoneFiles(File(file.path))
                                } else {
                                    navigateToPc(file.path)
                                }
                            } else {
                                handleFileClick(file)
                            }
                        },
                        onRefresh = {
                            if (isPhoneStorage) loadPhoneFiles(currentPhoneDir) else navigateToPc(currentPcPath)
                        },
                        onDisconnect = {
                            finish()
                        },
                        onNewFolder = {
                            newFolderName = ""
                            showNewFolderDialog = true
                        },
                        onUploadClick = {
                            pickFilesLauncher.launch("*/*")
                        },
                        onPaste = {
                            handlePasteAction()
                        },
                        onDownloadOrUpload = { file ->
                            if (isPhoneStorage) {
                                uploadPhoneFileToPc(file)
                            } else {
                                downloadPcFile(file)
                            }
                        },
                        onCopy = { file ->
                            clipboard = file
                            clipboardList = listOf(file)
                            clipAction = "copy"
                            clipboardSource = if (isPhoneStorage) "phone" else "pc"
                            toast("Copied ${file.name}")
                        },
                        onCut = { file ->
                            clipboard = file
                            clipboardList = listOf(file)
                            clipAction = "cut"
                            clipboardSource = if (isPhoneStorage) "phone" else "pc"
                            toast("Cut ${file.name}")
                        },
                        onRename = { file ->
                            fileToRename = file
                            renameText = file.name
                        },
                        onDelete = { file ->
                            if (isPhoneStorage) deletePhoneFile(file) else deletePcFile(file)
                        },
                        onShare = { file ->
                            sharePhoneFile(file)
                        },
                        onBatchCopy = { files ->
                            clipboardList = files
                            clipboard = files.firstOrNull()
                            clipAction = "copy"
                            clipboardSource = if (isPhoneStorage) "phone" else "pc"
                            toast("Copied ${files.size} items")
                        },
                        onBatchCut = { files ->
                            clipboardList = files
                            clipboard = files.firstOrNull()
                            clipAction = "cut"
                            clipboardSource = if (isPhoneStorage) "phone" else "pc"
                            toast("Cut ${files.size} items")
                        },
                        onBatchDelete = { files ->
                            handleBatchDelete(files)
                        },
                        onBatchDownload = { files ->
                            handleBatchDownload(files)
                        }
                    )

                    // 1) Create Folder Dialog
                    if (showNewFolderDialog) {
                        Dialog(onDismissRequest = { showNewFolderDialog = false }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.surface, shape = RoundedCornerShape(20.dp))
                                    .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
                                    .padding(20.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Create New Folder",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    OutlinedTextField(
                                        value = newFolderName,
                                        onValueChange = { newFolderName = it },
                                        placeholder = { Text("Folder name", color = colors.textSecondary) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = colors.scanPillBg,
                                            unfocusedBorderColor = colors.cardBorder,
                                            focusedTextColor = colors.textPrimary,
                                            unfocusedTextColor = colors.textPrimary,
                                            focusedContainerColor = colors.surfaceVariant,
                                            unfocusedContainerColor = colors.surfaceVariant
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(18.dp))
                                    Button(
                                        onClick = {
                                            if (newFolderName.isNotBlank()) {
                                                val name = newFolderName.trim()
                                                showNewFolderDialog = false
                                                if (isPhoneStorage) createFolderPhone(name) else createFolderPc(name)
                                            }
                                        },
                                        shape = RoundedCornerShape(25.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colors.scanPillBg,
                                            contentColor = colors.scanPillContent
                                        ),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) {
                                        Text("Create", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }
                            }
                        }
                    }

                    // 2) Rename File Dialog
                    fileToRename?.let { targetFile ->
                        Dialog(onDismissRequest = { fileToRename = null }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.surface, shape = RoundedCornerShape(20.dp))
                                    .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
                                    .padding(20.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Rename",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    OutlinedTextField(
                                        value = renameText,
                                        onValueChange = { renameText = it },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = colors.scanPillBg,
                                            unfocusedBorderColor = colors.cardBorder,
                                            focusedTextColor = colors.textPrimary,
                                            unfocusedTextColor = colors.textPrimary,
                                            focusedContainerColor = colors.surfaceVariant,
                                            unfocusedContainerColor = colors.surfaceVariant
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(18.dp))
                                    Button(
                                        onClick = {
                                            if (renameText.isNotBlank()) {
                                                val newName = renameText.trim()
                                                fileToRename = null
                                                if (isPhoneStorage) renamePhoneFile(targetFile, newName) else renamePcFile(targetFile, newName)
                                            }
                                        },
                                        shape = RoundedCornerShape(25.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colors.scanPillBg,
                                            contentColor = colors.scanPillContent
                                        ),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) {
                                        Text("Rename", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }
                            }
                        }
                    }

                    // 3) Fullscreen Media Viewer (Images & Videos)
                    previewMediaFile?.let { mediaFile ->
                        FullScreenMediaViewer(
                            file = mediaFile,
                            isPhoneStorage = isPhoneStorage,
                            ip = ip,
                            port = port,
                            pass = pass,
                            onClose = { previewMediaFile = null },
                            onDownload = { targetFile ->
                                if (isPhoneStorage) {
                                    toast("File is already stored on this phone")
                                } else {
                                    downloadPcFile(targetFile)
                                    previewMediaFile = null
                                }
                            }
                        )
                    }

                    // 4) Confirm Download Dialog for non-media PC files
                    confirmDownloadFile?.let { pcFile ->
                        Dialog(onDismissRequest = { confirmDownloadFile = null }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.surface, shape = RoundedCornerShape(20.dp))
                                    .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
                                    .padding(20.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Download File",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Do you want to download \"${pcFile.name}\" to your phone?",
                                        fontSize = 14.sp,
                                        color = colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { confirmDownloadFile = null },
                                            shape = RoundedCornerShape(20.dp),
                                            modifier = Modifier.weight(1f).height(46.dp)
                                        ) {
                                            Text("Cancel", color = colors.textPrimary)
                                        }
                                        Button(
                                            onClick = {
                                                val target = pcFile
                                                confirmDownloadFile = null
                                                downloadPcFile(target)
                                            },
                                            shape = RoundedCornerShape(20.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = colors.scanPillBg,
                                                contentColor = colors.scanPillContent
                                            ),
                                            modifier = Modifier.weight(1f).height(46.dp)
                                        ) {
                                            Text("Download", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        navigateToPc(root)
    }

    override fun onResume() {
        super.onResume()
        if (isPhoneStorage && checkStoragePermission()) {
            loadPhoneFiles(currentPhoneDir)
        }
        if (heartbeatJob == null || !heartbeatJob!!.isActive) {
            startHeartbeat()
        }
        if (phoneWorkerJob == null || !phoneWorkerJob!!.isActive) {
            startPhoneWorker()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        heartbeatJob?.cancel()
        phoneWorkerJob?.cancel()
    }

    // ==========================================
    // NAVIGATION & FILE LOADING
    // ==========================================

    private fun navigateToPc(path: String) {
        isLoading = true
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                WireFMClient.listFiles(ip, port, pass, path)
            }
            isLoading = false
            if (result != null) {
                currentPcPath = path
                fileList = result.sortedWith(
                    compareByDescending<FileItem> { it.isDir }.thenBy { it.name.lowercase(Locale.getDefault()) }
                )
            } else {
                toast("Failed to load PC directory")
            }
        }
    }

    private fun loadPhoneFiles(dir: File) {
        if (!dir.exists() || !dir.isDirectory) return
        currentPhoneDir = dir
        val raw = dir.listFiles() ?: arrayOf()

        val items = raw.map { f ->
            FileItem(
                name = f.name,
                path = f.absolutePath,
                isDir = f.isDirectory,
                size = if (f.isDirectory) 0L else f.length(),
                modTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(f.lastModified())),
                ext = if (f.isDirectory) "" else "." + f.extension.lowercase(Locale.getDefault())
            )
        }.sortedWith(
            compareByDescending<FileItem> { it.isDir }.thenBy { it.name.lowercase(Locale.getDefault()) }
        )

        fileList = items
    }

    // ==========================================
    // FILE CLICK ROUTING (PREVIEW vs STREAM vs DOWNLOAD)
    // ==========================================

    private fun handleFileClick(file: FileItem) {
        val ext = file.ext.lowercase(Locale.getDefault()).removePrefix(".")
        val imageExts = setOf("png", "jpg", "jpeg", "webp", "gif", "bmp", "svg")
        val videoExts = setOf("mp4", "mkv", "mov", "webm", "avi", "3gp", "flv", "ts")
        val audioExts = setOf("mp3", "wav", "flac", "ogg", "m4a", "aac", "opus")

        when {
            imageExts.contains(ext) || videoExts.contains(ext) || audioExts.contains(ext) -> {
                previewMediaFile = file
            }
            else -> {
                if (isPhoneStorage) {
                    openLocalFile(file)
                } else {
                    confirmDownloadFile = file
                }
            }
        }
    }

    // ==========================================
    // BACKGROUND HEARTBEAT & PHONE WORKER
    // ==========================================

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = lifecycleScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val phoneRoot = Environment.getExternalStorageDirectory() ?: File("/storage/emulated/0")
                    val filesList = if (checkStoragePermission()) {
                        phoneRoot.listFiles()?.take(60)?.map { f ->
                            mapOf<String, Any>(
                                "name" to f.name,
                                "path" to f.absolutePath,
                                "is_dir" to f.isDirectory,
                                "size" to if (f.isDirectory) 0L else f.length(),
                                "ext" to if (f.isDirectory) "" else "." + f.extension.lowercase(Locale.getDefault())
                            )
                        } ?: emptyList()
                    } else emptyList()

                    val devName = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
                    WireFMClient.sendPhoneHeartbeat(ip, port, pass, devName, phoneRoot.absolutePath, filesList)
                } catch (_: Exception) {}
                delay(12000)
            }
        }
    }

    private fun startPhoneWorker() {
        phoneWorkerJob?.cancel()
        phoneWorkerJob = lifecycleScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val cmd = WireFMClient.pollPhoneCommand(ip, port, pass)
                    if (cmd != null && cmd.id.isNotBlank()) {
                        handlePhoneCommand(cmd)
                    }
                } catch (_: Exception) {
                    delay(1000)
                }
            }
        }
    }

    private suspend fun handlePhoneCommand(cmd: PhoneCommand) {
        withContext(Dispatchers.IO) {
            try {
                when (cmd.action) {
                    "list" -> {
                        val targetPath = if (cmd.path.isBlank() || cmd.path == "/" || cmd.path == "~") {
                            Environment.getExternalStorageDirectory()?.absolutePath ?: "/storage/emulated/0"
                        } else {
                            cmd.path
                        }
                        val dir = File(targetPath)
                        if (!checkStoragePermission()) {
                            WireFMClient.respondPhoneError(ip, port, pass, cmd.id, "Storage permission not granted")
                        } else if (dir.exists() && dir.isDirectory) {
                            val files = dir.listFiles()?.map { f ->
                                mapOf<String, Any>(
                                    "name" to f.name,
                                    "path" to f.absolutePath,
                                    "is_dir" to f.isDirectory,
                                    "size" to if (f.isDirectory) 0L else f.length(),
                                    "ext" to if (f.isDirectory) "" else "." + f.extension.lowercase(Locale.getDefault())
                                )
                            }?.sortedWith(compareByDescending<Map<String, Any>> { it["is_dir"] as Boolean }.thenBy { (it["name"] as String).lowercase(Locale.getDefault()) }) ?: emptyList()
                            WireFMClient.respondPhoneCommand(ip, port, pass, cmd.id, true, files)
                        } else {
                            WireFMClient.respondPhoneError(ip, port, pass, cmd.id, "Folder does not exist")
                        }
                    }
                    "read" -> {
                        val file = File(cmd.path)
                        if (file.exists() && file.isFile && !cmd.token.isNullOrBlank()) {
                            WireFMClient.uploadPhoneStream(ip, port, pass, cmd.token, file)
                        } else {
                            WireFMClient.respondPhoneError(ip, port, pass, cmd.id, "File not found")
                        }
                    }
                    "delete" -> {
                        val file = File(cmd.path)
                        val ok = file.delete()
                        WireFMClient.respondPhoneCommand(ip, port, pass, cmd.id, ok, emptyList())
                    }
                    "mkdir" -> {
                        val dir = File(cmd.path)
                        val ok = dir.mkdirs()
                        WireFMClient.respondPhoneCommand(ip, port, pass, cmd.id, ok, emptyList())
                    }
                    "rename" -> {
                        val src = File(cmd.path)
                        val dest = File(cmd.destPath ?: "")
                        val ok = src.renameTo(dest)
                        WireFMClient.respondPhoneCommand(ip, port, pass, cmd.id, ok, emptyList())
                    }
                    "write_from_pc" -> {
                        val destFile = File(cmd.destPath ?: "")
                        val ok = WireFMClient.downloadFileToExactPath(ip, port, pass, cmd.path, destFile)
                        WireFMClient.respondPhoneCommand(ip, port, pass, cmd.id, ok, emptyList())
                    }
                }
            } catch (e: Exception) {
                WireFMClient.respondPhoneError(ip, port, pass, cmd.id, e.message ?: "Error")
            }
        }
    }

    // ==========================================
    // FILE ACTIONS (DOWNLOAD / UPLOAD / PASTE / BATCH)
    // ==========================================

    private fun downloadPcFile(file: FileItem) {
        toast("Downloading ${file.name}...")
        lifecycleScope.launch {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val downloaded = withContext(Dispatchers.IO) {
                WireFMClient.downloadFile(ip, port, pass, file.path, downloadsDir)
            }
            if (downloaded != null && downloaded.exists()) {
                toast("Saved to Downloads/${file.name}")
                MediaScannerConnection.scanFile(this@FileManagerActivity, arrayOf(downloaded.absolutePath), null, null)
            } else {
                toast("Download failed")
            }
        }
    }

    private fun uploadPhoneFileToPc(file: FileItem) {
        toast("Uploading ${file.name} to PC...")
        lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) {
                WireFMClient.uploadFile(ip, port, pass, currentPcPath, File(file.path))
            }
            if (ok) {
                toast("Uploaded to PC!")
            } else {
                toast("Upload failed")
            }
        }
    }

    private fun handleBatchCopy(files: List<FileItem>) {
        if (files.isEmpty()) return
        clipboardList = files
        clipboard = files.firstOrNull()
        clipAction = "copy"
        clipboardSource = if (isPhoneStorage) "phone" else "pc"
        toast("Copied ${files.size} items")
    }

    private fun handleBatchCut(files: List<FileItem>) {
        if (files.isEmpty()) return
        clipboardList = files
        clipboard = files.firstOrNull()
        clipAction = "cut"
        clipboardSource = if (isPhoneStorage) "phone" else "pc"
        toast("Cut ${files.size} items")
    }

    private fun handleBatchDelete(files: List<FileItem>) {
        if (files.isEmpty()) return
        lifecycleScope.launch {
            toast("Deleting ${files.size} item(s)...")
            var count = 0
            withContext(Dispatchers.IO) {
                for (f in files) {
                    val ok = if (isPhoneStorage) {
                        File(f.path).delete()
                    } else {
                        WireFMClient.deleteFile(ip, port, pass, f.path)
                    }
                    if (ok) count++
                }
            }
            toast("Deleted $count item(s)")
            if (isPhoneStorage) loadPhoneFiles(currentPhoneDir) else navigateToPc(currentPcPath)
        }
    }

    private fun handleBatchDownload(files: List<FileItem>) {
        if (files.isEmpty()) return
        lifecycleScope.launch {
            toast("Downloading ${files.size} file(s)...")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            var count = 0
            withContext(Dispatchers.IO) {
                for (f in files) {
                    val downloaded = WireFMClient.downloadFile(ip, port, pass, f.path, downloadsDir)
                    if (downloaded != null && downloaded.exists()) {
                        count++
                        MediaScannerConnection.scanFile(this@FileManagerActivity, arrayOf(downloaded.absolutePath), null, null)
                    }
                }
            }
            toast("Saved $count file(s) to Downloads")
        }
    }

    private fun handlePasteAction() {
        val itemsToPaste = if (clipboardList.isNotEmpty()) clipboardList else listOfNotNull(clipboard)
        if (itemsToPaste.isEmpty()) return
        val currentDest = if (isPhoneStorage) currentPhoneDir.absolutePath else currentPcPath

        lifecycleScope.launch {
            toast("Pasting ${itemsToPaste.size} item(s)...")
            var successCount = 0
            withContext(Dispatchers.IO) {
                for (cb in itemsToPaste) {
                    if (clipboardSource == "pc" && !isPhoneStorage) {
                        // PC to PC copy/move
                        val destFile = if (currentDest.endsWith("/")) currentDest + cb.name else "$currentDest/${cb.name}"
                        val ok = if (clipAction == "cut") WireFMClient.moveFile(ip, port, pass, cb.path, destFile)
                        else WireFMClient.copyFile(ip, port, pass, cb.path, destFile)
                        if (ok) successCount++
                    } else if (clipboardSource == "phone" && isPhoneStorage) {
                        // Phone to Phone copy/move
                        val src = File(cb.path)
                        val dest = File(currentPhoneDir, cb.name)
                        val ok = if (clipAction == "cut") src.renameTo(dest)
                        else {
                            src.copyTo(dest, overwrite = true)
                            true
                        }
                        if (ok) successCount++
                    } else if (clipboardSource == "phone" && !isPhoneStorage) {
                        // Phone to PC Upload
                        val ok = WireFMClient.uploadFile(ip, port, pass, currentPcPath, File(cb.path))
                        if (ok) successCount++
                    } else if (clipboardSource == "pc" && isPhoneStorage) {
                        // PC to Phone Download
                        val destFile = File(currentPhoneDir, cb.name)
                        val ok = WireFMClient.downloadFileToExactPath(ip, port, pass, cb.path, destFile)
                        if (ok) successCount++
                    }
                }
            }
            toast("Pasted $successCount item(s)")
            clipboard = null
            clipboardList = emptyList()
            if (isPhoneStorage) loadPhoneFiles(currentPhoneDir) else navigateToPc(currentPcPath)
        }
    }

    private fun deletePcFile(file: FileItem) {
        lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) {
                WireFMClient.deleteFile(ip, port, pass, file.path)
            }
            if (ok) {
                toast("Moved to Recycle Bin")
                navigateToPc(currentPcPath)
            } else toast("Delete failed")
        }
    }

    private fun deletePhoneFile(file: FileItem) {
        val f = File(file.path)
        val ok = f.delete()
        if (ok) {
            toast("Deleted ${file.name}")
            loadPhoneFiles(currentPhoneDir)
        } else toast("Delete failed")
    }

    private fun createFolderPc(name: String) {
        lifecycleScope.launch {
            val path = if (currentPcPath.endsWith("/")) currentPcPath + name else "$currentPcPath/$name"
            val ok = withContext(Dispatchers.IO) {
                WireFMClient.createFolder(ip, port, pass, path)
            }
            if (ok) navigateToPc(currentPcPath) else toast("Failed to create folder")
        }
    }

    private fun createFolderPhone(name: String) {
        val f = File(currentPhoneDir, name)
        if (f.mkdirs()) loadPhoneFiles(currentPhoneDir) else toast("Failed to create folder")
    }

    private fun renamePcFile(file: FileItem, newName: String) {
        val parent = file.path.substringBeforeLast("/")
        val dest = "$parent/$newName"
        lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) {
                WireFMClient.moveFile(ip, port, pass, file.path, dest)
            }
            if (ok) navigateToPc(currentPcPath) else toast("Rename failed")
        }
    }

    private fun renamePhoneFile(file: FileItem, newName: String) {
        val src = File(file.path)
        val dest = File(src.parentFile, newName)
        if (src.renameTo(dest)) loadPhoneFiles(currentPhoneDir) else toast("Rename failed")
    }

    private fun openLocalFile(file: FileItem) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                File(file.path)
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (_: Exception) {
            toast("No application found to open this file")
        }
    }

    private fun sharePhoneFile(file: FileItem) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                File(file.path)
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share ${file.name}"))
        } catch (_: Exception) {
            toast("Cannot share file")
        }
    }

    private fun uploadSelectedUris(uris: List<Uri>) {
        toast("Uploading ${uris.size} file(s)...")
        lifecycleScope.launch {
            var uploadedCount = 0
            withContext(Dispatchers.IO) {
                for (uri in uris) {
                    val name = getFileNameFromUri(uri) ?: "upload_${System.currentTimeMillis()}"
                    try {
                        contentResolver.openInputStream(uri)?.use { stream ->
                            val ok = WireFMClient.uploadStream(ip, port, pass, currentPcPath, name, stream)
                            if (ok) uploadedCount++
                        }
                    } catch (_: Exception) {}
                }
            }
            toast("Uploaded $uploadedCount file(s) to PC")
            navigateToPc(currentPcPath)
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                cursor.getString(nameIndex)
            } else null
        } ?: uri.lastPathSegment
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (_: Exception) {
                startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
