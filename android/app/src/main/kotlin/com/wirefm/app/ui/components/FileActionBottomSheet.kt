package com.wirefm.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wirefm.app.model.FileItem
import com.wirefm.app.ui.theme.AccentRed
import com.wirefm.app.ui.theme.WireFMTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileActionBottomSheet(
    file: FileItem,
    isPhoneStorage: Boolean,
    hasClipboard: Boolean = false,
    onDismiss: () -> Unit,
    onSelectMode: (() -> Unit)? = null,
    onDownloadOrUpload: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onPaste: (() -> Unit)? = null,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val (icon, tint) = getFileIconAndColor(file)
    val colors = WireFMTheme.colors
    val selectionBlue = Color(0xFF2F8BF0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header: Icon + Name + Meta
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(tint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (file.isDir) "Directory" else "${formatFileSize(file.size)} • ${file.ext.uppercase()}",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Items
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceVariant)
            ) {
                if (onSelectMode != null) {
                    ActionRow(
                        title = "Select Multiple",
                        icon = Icons.Default.CheckCircleOutline,
                        onClick = {
                            onDismiss()
                            onSelectMode()
                        }
                    )
                }

                if (!file.isDir) {
                    ActionRow(
                        title = if (isPhoneStorage) "Send / Upload to PC" else "Download to Phone",
                        icon = if (isPhoneStorage) Icons.Default.FileUpload else Icons.Default.FileDownload,
                        onClick = {
                            onDismiss()
                            onDownloadOrUpload()
                        }
                    )
                }

                ActionRow(
                    title = "Copy",
                    icon = Icons.Default.ContentCopy,
                    onClick = {
                        onDismiss()
                        onCopy()
                    }
                )

                ActionRow(
                    title = "Cut (Move)",
                    icon = Icons.Default.ContentCut,
                    onClick = {
                        onDismiss()
                        onCut()
                    }
                )

                // Paste with Glow Effect matching user requirement
                if (onPaste != null) {
                    ActionRow(
                        title = if (hasClipboard) "Paste (Clipboard Ready)" else "Paste",
                        icon = Icons.Default.ContentPaste,
                        isGlowing = hasClipboard,
                        onClick = {
                            onDismiss()
                            onPaste()
                        }
                    )
                }

                ActionRow(
                    title = "Rename",
                    icon = Icons.Default.DriveFileRenameOutline,
                    onClick = {
                        onDismiss()
                        onRename()
                    }
                )

                if (isPhoneStorage && !file.isDir) {
                    ActionRow(
                        title = "Share",
                        icon = Icons.Default.Share,
                        onClick = {
                            onDismiss()
                            onShare()
                        }
                    )
                }

                ActionRow(
                    title = "Delete",
                    icon = Icons.Default.Delete,
                    isDestructive = true,
                    onClick = {
                        onDismiss()
                        onDelete()
                    }
                )
            }
        }
    }
}

@Composable
private fun ActionRow(
    title: String,
    icon: ImageVector,
    isDestructive: Boolean = false,
    isGlowing: Boolean = false,
    onClick: () -> Unit
) {
    val colors = WireFMTheme.colors
    val selectionBlue = Color(0xFF2F8BF0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .then(
                if (isGlowing) Modifier.background(selectionBlue.copy(alpha = 0.15f))
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = when {
                isDestructive -> AccentRed
                isGlowing -> selectionBlue
                else -> colors.textPrimary
            },
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = if (isGlowing) FontWeight.Bold else FontWeight.Medium,
            color = when {
                isDestructive -> AccentRed
                isGlowing -> selectionBlue
                else -> colors.textPrimary
            }
        )
    }
}
