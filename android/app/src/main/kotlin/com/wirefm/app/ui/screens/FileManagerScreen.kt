package com.wirefm.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wirefm.app.model.FileItem
import com.wirefm.app.ui.components.BreadcrumbBar
import com.wirefm.app.ui.components.FileActionBottomSheet
import com.wirefm.app.ui.components.FileItemGridCard
import com.wirefm.app.ui.components.FileItemListRow
import com.wirefm.app.ui.components.StorageTabSwitcher
import com.wirefm.app.ui.theme.AccentGreen
import com.wirefm.app.ui.theme.DarkBackground
import com.wirefm.app.ui.theme.DarkCardBg
import com.wirefm.app.ui.theme.DarkCardBorder
import com.wirefm.app.ui.theme.PillPrimary
import com.wirefm.app.ui.theme.PillPrimaryContent
import com.wirefm.app.ui.theme.TextPrimary
import com.wirefm.app.ui.theme.TextSecondary

@Composable
fun FileManagerScreen(
    title: String,
    pcName: String,
    phoneName: String,
    currentPath: String,
    isPhoneStorage: Boolean,
    files: List<FileItem>,
    isLoading: Boolean,
    clipboard: FileItem?,
    clipAction: String,
    onTabSelected: (isPhone: Boolean) -> Unit,
    onNavigateTo: (path: String) -> Unit,
    onFileClick: (file: FileItem) -> Unit,
    onRefresh: () -> Unit,
    onDisconnect: () -> Unit,
    onNewFolder: () -> Unit,
    onUploadClick: () -> Unit,
    onPaste: () -> Unit,
    onDownloadOrUpload: (file: FileItem) -> Unit,
    onCopy: (file: FileItem) -> Unit,
    onCut: (file: FileItem) -> Unit,
    onRename: (file: FileItem) -> Unit,
    onDelete: (file: FileItem) -> Unit,
    onShare: (file: FileItem) -> Unit
) {
    var isGridView by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedFileForAction by remember { mutableStateOf<FileItem?>(null) }

    val filteredFiles = remember(files, searchQuery) {
        if (searchQuery.isBlank()) files
        else files.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Device Info + Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Connection badge & title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AccentGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isPhoneStorage) "Internal Storage" else "Connected via Wi-Fi",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Action Icons (Search, Grid/List, Refresh, Disconnect)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isSearchActive) PillPrimary else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Toggle View",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onDisconnect) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Disconnect",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Search Bar (if active)
            AnimatedVisibility(visible = isSearchActive) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter files & folders...", color = TextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PillPrimary,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkCardBg,
                        unfocusedContainerColor = DarkCardBg
                    )
                )
            }

            // Storage Device Switcher (PC vs Phone Internal)
            StorageTabSwitcher(
                isPhoneSelected = isPhoneStorage,
                onTabSelected = onTabSelected,
                pcName = pcName,
                phoneName = phoneName,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Breadcrumb path chips
            BreadcrumbBar(
                currentPath = currentPath,
                onNavigateTo = onNavigateTo
            )

            // Clipboard Banner (when copy/cut is active)
            AnimatedVisibility(visible = clipboard != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF222034))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📋 ${clipboard?.name} ($clipAction)",
                        fontSize = 12.sp,
                        color = Color(0xFFA78BFA),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "PASTE HERE",
                        fontSize = 12.sp,
                        color = PillPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            // File Listing Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = PillPrimary,
                        strokeWidth = 3.dp,
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.Center)
                    )
                } else if (filteredFiles.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching files" else "Folder is empty",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                } else {
                    if (isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredFiles, key = { it.path }) { file ->
                                FileItemGridCard(
                                    file = file,
                                    onClick = { onFileClick(file) },
                                    onMoreClick = { selectedFileForAction = file }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(vertical = 4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredFiles, key = { it.path }) { file ->
                                FileItemListRow(
                                    file = file,
                                    onClick = { onFileClick(file) },
                                    onMoreClick = { selectedFileForAction = file }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Buttons (New Folder & Paste / Upload)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (clipboard != null) {
                FloatingActionButton(
                    onClick = onPaste,
                    shape = CircleShape,
                    containerColor = Color(0xFF6C63FF),
                    contentColor = PillPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste"
                    )
                }
            }

            FloatingActionButton(
                onClick = onNewFolder,
                shape = CircleShape,
                containerColor = PillPrimary,
                contentColor = PillPrimaryContent
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        }

        // File Action Bottom Sheet
        selectedFileForAction?.let { file ->
            FileActionBottomSheet(
                file = file,
                isPhoneStorage = isPhoneStorage,
                onDismiss = { selectedFileForAction = null },
                onDownloadOrUpload = { onDownloadOrUpload(file) },
                onCopy = { onCopy(file) },
                onCut = { onCut(file) },
                onRename = { onRename(file) },
                onDelete = { onDelete(file) },
                onShare = { onShare(file) }
            )
        }
    }
}
