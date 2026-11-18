package com.wirefm.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wirefm.app.ui.theme.WireFMTheme

@Composable
fun FloatingStoragePillToggle(
    isPhoneSelected: Boolean,
    onToggle: (isPhone: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = WireFMTheme.colors

    Box(
        modifier = modifier
            .width(116.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(colors.surfaceVariant)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(26.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PC Storage Button
            StoragePillButton(
                isSelected = !isPhoneSelected,
                onClick = { onToggle(false) },
                icon = { tint ->
                    Icon(
                        imageVector = Icons.Outlined.Computer,
                        contentDescription = "PC Storage",
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Phone Storage Button
            StoragePillButton(
                isSelected = isPhoneSelected,
                onClick = { onToggle(true) },
                icon = { tint ->
                    Icon(
                        imageVector = Icons.Outlined.PhoneAndroid,
                        contentDescription = "Phone Storage",
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun StoragePillButton(
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = WireFMTheme.colors

    val bg by animateColorAsState(
        targetValue = if (isSelected) colors.scanPillBg else Color.Transparent,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "pillToggleBg"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isSelected) colors.scanPillContent else colors.textSecondary,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "pillToggleTint"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        icon(iconTint)
    }
}
