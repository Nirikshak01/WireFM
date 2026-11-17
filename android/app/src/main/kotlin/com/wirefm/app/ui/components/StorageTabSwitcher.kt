package com.wirefm.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wirefm.app.ui.theme.DarkCardBg
import com.wirefm.app.ui.theme.DarkCardBorder
import com.wirefm.app.ui.theme.DarkSurfaceVariant
import com.wirefm.app.ui.theme.PillPrimary
import com.wirefm.app.ui.theme.PillPrimaryContent
import com.wirefm.app.ui.theme.TextPrimary
import com.wirefm.app.ui.theme.TextSecondary

@Composable
fun StorageTabSwitcher(
    isPhoneSelected: Boolean,
    onTabSelected: (isPhone: Boolean) -> Unit,
    pcName: String = "PC Storage",
    phoneName: String = "Internal Storage",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(DarkCardBg)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(24.dp))
            .padding(4.dp)
    ) {
        // Tab row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PC Storage Tab
            TabItem(
                title = pcName,
                isSelected = !isPhoneSelected,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Computer,
                        contentDescription = "PC Storage",
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = { onTabSelected(false) },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Phone Internal Storage Tab
            TabItem(
                title = phoneName,
                isSelected = isPhoneSelected,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.PhoneAndroid,
                        contentDescription = "Phone Storage",
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = { onTabSelected(true) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TabItem(
    title: String,
    isSelected: Boolean,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PillPrimary else Color.Transparent,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "tabBg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) PillPrimaryContent else TextSecondary,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "tabColor"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Box(modifier = Modifier.size(18.dp)) {
                icon()
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}
