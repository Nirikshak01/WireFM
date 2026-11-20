package com.wirefm.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wirefm.app.model.FileItem
import com.wirefm.app.ui.theme.ArchiveColor
import com.wirefm.app.ui.theme.AudioColor
import com.wirefm.app.ui.theme.CodeColor
import com.wirefm.app.ui.theme.DocumentColor
import com.wirefm.app.ui.theme.FolderColor
import com.wirefm.app.ui.theme.ImageColor
import com.wirefm.app.ui.theme.VideoColor
import com.wirefm.app.ui.theme.WireFMTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItemGridCard(
    file: FileItem,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = WireFMTheme.colors
    val selectionBlue = Color(0xFF2F8BF0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .clip(RoundedCornerShape(16.dp))
                .then(
                    if (isSelected) Modifier.border(2.5.dp, selectionBlue, RoundedCornerShape(16.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (file.isDir) {
                // Folder Shaped Card (#CCD8DA)
                val folderCardColor = colors.folderCard
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val r = 16.dp.toPx()
                    val tabW = w * 0.46f
                    val tabH = h * 0.18f

                    val folderPath = Path().apply {
                        moveTo(r, 0f)
                        lineTo(tabW - 10f, 0f)
                        quadraticTo(tabW - 2f, 0f, tabW + 6f, tabH * 0.5f)
                        quadraticTo(tabW + 12f, tabH, tabW + 20f, tabH)
                        lineTo(w - r, tabH)
                        quadraticTo(w, tabH, w, tabH + r)
                        lineTo(w, h - r)
                        quadraticTo(w, h, w - r, h)
                        lineTo(r, h)
                        quadraticTo(0f, h, 0f, h - r)
                        lineTo(0f, r)
                        quadraticTo(0f, 0f, r, 0f)
                        close()
                    }
                    drawPath(folderPath, color = folderCardColor)
                }
            } else {
                // File Card
                val (icon, _) = getFileIconAndColor(file)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.folderCard),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.folderCardContent,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            // Selection Check Badge (Top-Right)
            if (isSelectionMode || isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) selectionBlue else Color.Black.copy(alpha = 0.4f))
                        .then(
                            if (!isSelected) Modifier.border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Label below
        Text(
            text = file.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) selectionBlue else colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItemListRow(
    file: FileItem,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, tint) = getFileIconAndColor(file)
    val colors = WireFMTheme.colors
    val selectionBlue = Color(0xFF2F8BF0)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .background(if (isSelected) selectionBlue.copy(alpha = 0.15f) else Color.Transparent)
            .then(
                if (isSelected) Modifier.border(1.5.dp, selectionBlue, RoundedCornerShape(14.dp))
                else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection Checkbox in List Mode
        if (isSelectionMode || isSelected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) selectionBlue else Color.Transparent)
                    .border(1.5.dp, if (isSelected) selectionBlue else colors.textTertiary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        // Icon container
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Name & Meta Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) selectionBlue else colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (file.isDir) "Folder" else formatFileSize(file.size),
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }

        // More options action
        IconButton(
            onClick = onMoreClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = colors.textTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

fun getFileIconAndColor(file: FileItem): Pair<ImageVector, Color> {
    if (file.isDir) return Pair(Icons.Default.Folder, FolderColor)

    return when (file.ext.lowercase()) {
        "png", "jpg", "jpeg", "webp", "gif", "svg", "bmp" -> Pair(Icons.Default.Image, ImageColor)
        "mp4", "mkv", "avi", "mov", "webm", "flv" -> Pair(Icons.Default.Movie, VideoColor)
        "mp3", "flac", "wav", "m4a", "ogg", "aac" -> Pair(Icons.Default.AudioFile, AudioColor)
        "pdf", "doc", "docx", "txt", "md", "xls", "xlsx", "ppt", "pptx" -> Pair(Icons.Default.Description, DocumentColor)
        "zip", "tar", "gz", "7z", "rar", "bz2" -> Pair(Icons.Default.Archive, ArchiveColor)
        "py", "kt", "java", "c", "cpp", "h", "rs", "go", "js", "ts", "html", "css", "json", "xml", "sh" -> Pair(Icons.Default.Code, CodeColor)
        else -> Pair(Icons.Default.InsertDriveFile, DocumentColor)
    }
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
    return String.format(java.util.Locale.US, "%.1f %s", value, units[digitGroups.coerceIn(0, units.size - 1)])
}
