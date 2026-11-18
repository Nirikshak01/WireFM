package com.wirefm.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wirefm.app.ui.theme.WireFMTheme

@Composable
fun BreadcrumbBar(
    currentPath: String,
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val colors = WireFMTheme.colors

    val normalized = currentPath.replace('\\', '/')
    val segments = normalized.split('/').filter { it.isNotEmpty() }

    LaunchedEffect(currentPath) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Root Chip
        BreadcrumbChip(
            title = "root",
            isLast = segments.isEmpty(),
            onClick = { onNavigateTo("/") }
        )

        var accumulated = ""
        segments.forEachIndexed { index, seg ->
            accumulated += "/$seg"
            val targetPath = accumulated
            val isLast = index == segments.lastIndex

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .size(16.dp)
            )

            BreadcrumbChip(
                title = seg,
                isLast = isLast,
                onClick = { onNavigateTo(targetPath) }
            )
        }
    }
}

@Composable
private fun BreadcrumbChip(
    title: String,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val colors = WireFMTheme.colors

    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = if (isLast) FontWeight.Bold else FontWeight.SemiBold,
        color = if (isLast) colors.textPrimary else colors.textSecondary,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isLast) colors.surfaceVariant.copy(alpha = 0.6f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 3.dp)
    )
}
