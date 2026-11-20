package com.wirefm.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wirefm.app.model.FileItem
import com.wirefm.app.ui.components.BreadcrumbBar
import com.wirefm.app.ui.components.FileActionBottomSheet
import com.wirefm.app.ui.components.FileItemGridCard
import com.wirefm.app.ui.components.FileItemListRow
import com.wirefm.app.ui.components.FloatingStoragePillToggle
import com.wirefm.app.ui.theme.WireFMTheme

@OptIn(ExperimentalMaterial3Api::class)
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
    onShare: (file: FileItem) -> Unit,
    onBatchCopy: ((List<FileItem>) -> Unit)? = null,
    onBatchCut: ((List<FileItem>) -> Unit)? = null,
    onBatchDelete: ((List<FileItem>) -> Unit)? = null,
    onBatchDownload: ((List<FileItem>) -> Unit)? = null
) {
    var isGridView by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var showPopupMenu by remember { mutableStateOf(false) }
    var showFabActionSheet by remember { mutableStateOf(false) }
    var selectedFileForAction by remember { mutableStateOf<FileItem?>(null) }

    // Multi-Selection state matching user request
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedFiles by remember { mutableStateOf<Set<FileItem>>(emptySet()) }

    val colors = WireFMTheme.colors
    val selectionBlue = Color(0xFF2F8BF0)

    val filteredFiles = remember(files, searchQuery) {
        if (searchQuery.isBlank()) files
        else files.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1) Header: Normal Mode vs Selection Mode
            if (isSelectionMode) {
                // Selection Mode Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                isSelectionMode = false
                                selectedFiles = emptySet()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Exit Selection",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${selectedFiles.size} Selected",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = selectionBlue
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Select All / Deselect All
                        IconButton(
                            onClick = {
                                selectedFiles = if (selectedFiles.size == filteredFiles.size) {
                                    emptySet()
                                } else {
                                    filteredFiles.toSet()
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SelectAll,
                                contentDescription = "Select All",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // 3-Dot Menu for Batch Actions
                        IconButton(
                            onClick = { showPopupMenu = !showPopupMenu },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Batch Actions",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            } else {
                // Normal Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Crossfade(
                        targetState = isPhoneStorage,
                        label = "headerDeviceCrossfade"
                    ) { phoneMode ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = if (phoneMode) Icons.Outlined.PhoneAndroid else Icons.Outlined.Computer,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (phoneMode) phoneName else title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (searchQuery.isNotEmpty()) searchQuery = ""
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = { showPopupMenu = !showPopupMenu },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // 2) Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .height(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.searchBarBg)
                    .padding(start = 14.dp, end = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.searchBarContent
                        ),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search files and folders",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = colors.textSecondary
                                )
                            }
                            innerTextField()
                        }
                    )

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.searchBtnBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = colors.searchBtnContent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 3) Breadcrumbs
            BreadcrumbBar(
                currentPath = currentPath,
                onNavigateTo = { path ->
                    if (isSelectionMode) {
                        isSelectionMode = false
                        selectedFiles = emptySet()
                    }
                    onNavigateTo(path)
                }
            )

            // Clipboard Banner (when copy/cut is active)
            AnimatedVisibility(visible = clipboard != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceVariant)
                        .clickable { onPaste() }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📋 ${clipboard?.name} ($clipAction)",
                        fontSize = 13.sp,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "PASTE HERE",
                        fontSize = 12.sp,
                        color = selectionBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 4) Content: 3-Column Grid View or List View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = colors.scanPillBg,
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
                            color = colors.textSecondary
                        )
                    }
                } else {
                    if (isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 96.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredFiles, key = { it.path }) { file ->
                                val isSelected = selectedFiles.contains(file)
                                FileItemGridCard(
                                    file = file,
                                    isSelected = isSelected,
                                    isSelectionMode = isSelectionMode,
                                    onClick = {
                                        if (isSelectionMode) {
                                            selectedFiles = if (isSelected) selectedFiles - file else selectedFiles + file
                                        } else {
                                            onFileClick(file)
                                        }
                                    },
                                    onLongClick = {
                                        if (isSelectionMode) {
                                            selectedFiles = if (isSelected) selectedFiles - file else selectedFiles + file
                                        } else {
                                            selectedFileForAction = file
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 96.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredFiles, key = { it.path }) { file ->
                                val isSelected = selectedFiles.contains(file)
                                FileItemListRow(
                                    file = file,
                                    isSelected = isSelected,
                                    isSelectionMode = isSelectionMode,
                                    onClick = {
                                        if (isSelectionMode) {
                                            selectedFiles = if (isSelected) selectedFiles - file else selectedFiles + file
                                        } else {
                                            onFileClick(file)
                                        }
                                    },
                                    onLongClick = {
                                        if (isSelectionMode) {
                                            selectedFiles = if (isSelected) selectedFiles - file else selectedFiles + file
                                        } else {
                                            selectedFileForAction = file
                                        }
                                    },
                                    onMoreClick = { selectedFileForAction = file }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5) Floating Bottom Controls: Centered Pill Toggle + Circular '+' FAB
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp, start = 20.dp, end = 20.dp)
        ) {
            FloatingStoragePillToggle(
                isPhoneSelected = isPhoneStorage,
                onToggle = { selectedPhone ->
                    if (isSelectionMode) {
                        isSelectionMode = false
                        selectedFiles = emptySet()
                    }
                    onTabSelected(selectedPhone)
                },
                modifier = Modifier.align(Alignment.Center)
            )

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .align(Alignment.CenterEnd)
                    .clip(CircleShape)
                    .background(colors.fabBg)
                    .clickable { showFabActionSheet = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = colors.fabContent,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // 6) Custom 3-Dot Popup Menu with blue border matching Figma
        if (showPopupMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showPopupMenu = false }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 52.dp, end = 16.dp)
                        .width(175.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.popupBg)
                        .border(1.5.dp, colors.popupBorder, RoundedCornerShape(16.dp))
                        .padding(vertical = 10.dp, horizontal = 12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (isSelectionMode) {
                            // Menu in Selection Mode
                            PopupMenuItem(
                                title = "Copy (${selectedFiles.size})",
                                icon = Icons.Default.ContentCopy,
                                onClick = {
                                    showPopupMenu = false
                                    onBatchCopy?.invoke(selectedFiles.toList())
                                    isSelectionMode = false
                                }
                            )
                            PopupMenuItem(
                                title = "Cut (${selectedFiles.size})",
                                icon = Icons.Default.ContentCut,
                                onClick = {
                                    showPopupMenu = false
                                    onBatchCut?.invoke(selectedFiles.toList())
                                    isSelectionMode = false
                                }
                            )
                            PopupMenuItem(
                                title = if (clipboard != null) "Paste (Active)" else "Paste",
                                icon = Icons.Default.ContentPaste,
                                isGlowing = clipboard != null,
                                onClick = {
                                    showPopupMenu = false
                                    if (clipboard != null) onPaste()
                                }
                            )
                            PopupMenuItem(
                                title = "Delete (${selectedFiles.size})",
                                icon = Icons.Default.Delete,
                                onClick = {
                                    showPopupMenu = false
                                    onBatchDelete?.invoke(selectedFiles.toList())
                                    selectedFiles = emptySet()
                                    isSelectionMode = false
                                }
                            )
                            if (!isPhoneStorage && onBatchDownload != null) {
                                PopupMenuItem(
                                    title = "Download All",
                                    icon = Icons.Default.FileDownload,
                                    onClick = {
                                        showPopupMenu = false
                                        onBatchDownload.invoke(selectedFiles.toList())
                                        isSelectionMode = false
                                    }
                                )
                            }
                            PopupMenuItem(
                                title = "Exit Selection",
                                icon = Icons.Default.Close,
                                onClick = {
                                    showPopupMenu = false
                                    isSelectionMode = false
                                    selectedFiles = emptySet()
                                }
                            )
                        } else {
                            // Normal 3-Dot Menu
                            PopupMenuItem(
                                title = "Select",
                                icon = Icons.Default.CheckBoxOutlineBlank,
                                onClick = {
                                    showPopupMenu = false
                                    isSelectionMode = true
                                    selectedFiles = emptySet()
                                }
                            )
                            PopupMenuItem(
                                title = if (isGridView) "list" else "Grid",
                                icon = if (isGridView) Icons.Default.FormatListBulleted else Icons.Default.GridView,
                                onClick = {
                                    showPopupMenu = false
                                    isGridView = !isGridView
                                }
                            )
                            PopupMenuItem(
                                title = "Reload",
                                icon = Icons.Default.Refresh,
                                onClick = {
                                    showPopupMenu = false
                                    onRefresh()
                                }
                            )
                            if (clipboard != null) {
                                PopupMenuItem(
                                    title = "Paste (${clipboard.name})",
                                    icon = Icons.Default.ContentPaste,
                                    isGlowing = true,
                                    onClick = {
                                        showPopupMenu = false
                                        onPaste()
                                    }
                                )
                            }
                            PopupMenuItem(
                                title = "Exit",
                                icon = Icons.Default.PowerSettingsNew,
                                onClick = {
                                    showPopupMenu = false
                                    onDisconnect()
                                }
                            )
                        }
                    }
                }
            }
        }

        // 7) '+' FAB Action Bottom Sheet (Upload to PC / New Folder)
        if (showFabActionSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showFabActionSheet = false },
                sheetState = sheetState,
                containerColor = colors.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Quick Actions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surfaceVariant)
                            .clickable {
                                showFabActionSheet = false
                                onNewFolder()
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "New Folder",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surfaceVariant)
                            .clickable {
                                showFabActionSheet = false
                                onUploadClick()
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = if (isPhoneStorage) "Upload to Phone" else "Upload Files to PC",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }

        // 8) File Action Bottom Sheet with glowing Paste & Multi-Select trigger
        selectedFileForAction?.let { file ->
            FileActionBottomSheet(
                file = file,
                isPhoneStorage = isPhoneStorage,
                hasClipboard = clipboard != null,
                onDismiss = { selectedFileForAction = null },
                onSelectMode = {
                    isSelectionMode = true
                    selectedFiles = setOf(file)
                },
                onDownloadOrUpload = { onDownloadOrUpload(file) },
                onCopy = { onCopy(file) },
                onCut = { onCut(file) },
                onPaste = { onPaste() },
                onRename = { onRename(file) },
                onDelete = { onDelete(file) },
                onShare = { onShare(file) }
            )
        }
    }
}

@Composable
private fun PopupMenuItem(
    title: String,
    icon: ImageVector,
    isGlowing: Boolean = false,
    onClick: () -> Unit
) {
    val colors = WireFMTheme.colors
    val selectionBlue = Color(0xFF2F8BF0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .then(
                if (isGlowing) Modifier.background(selectionBlue.copy(alpha = 0.15f))
                else Modifier
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isGlowing) selectionBlue else colors.textPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = if (isGlowing) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isGlowing) selectionBlue else colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
