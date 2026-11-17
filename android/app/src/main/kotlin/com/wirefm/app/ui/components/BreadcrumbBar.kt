package com.wirefm.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wirefm.app.ui.theme.DarkCardBg
import com.wirefm.app.ui.theme.TextPrimary
import com.wirefm.app.ui.theme.TextSecondary
import com.wirefm.app.ui.theme.TextTertiary

@Composable
fun BreadcrumbBar(
    currentPath: String,
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Clean up segments
    val normalized = currentPath.replace('\\', '/')
    val segments = normalized.split('/').filter { it.isNotEmpty() }

    LaunchedEffect(currentPath) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Root / Home Chip
        BreadcrumbChip(
            title = "Root",
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
                tint = TextTertiary,
                modifier = Modifier.size(16.dp)
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
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
        color = if (isLast) TextPrimary else TextSecondary,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isLast) DarkCardBg else androidx.compose.ui.graphics.Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
