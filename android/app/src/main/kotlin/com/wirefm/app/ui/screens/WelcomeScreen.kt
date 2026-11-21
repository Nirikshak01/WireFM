package com.wirefm.app.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wirefm.app.R
import com.wirefm.app.ui.theme.WireFMTheme

@Composable
fun WelcomeScreen(
    onScanClick: () -> Unit,
    onManualConnectClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val colors = WireFMTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {
            // 1) Top Bar: WireFM Logo on left (40dp tile), Settings gear on right, vertically centered
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Crossfade(
                    targetState = colors.isDark,
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                    label = "welcomeLogoCrossfade"
                ) { isDark ->
                    Image(
                        painter = painterResource(
                            id = if (isDark) R.drawable.ic_wirefm_tile_white else R.drawable.ic_wirefm_tile_dark
                        ),
                        contentDescription = "WireFM Tile Logo",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Fit
                    )
                }

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2) "Welcome" Heading (Bold ~30sp)
            Text(
                text = "Welcome",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3) Illustration Card: qr_scan_card_preview.png (FillWidth, unclipped so shoes overflow naturally)
            Image(
                painter = painterResource(id = R.drawable.qr_scan_card_preview),
                contentDescription = "Connect with WireFM Scan with your PC",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                contentScale = ContentScale.FillWidth
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 4) Explanatory line with info icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Connect with WireFM Scan with your pc",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5) Secondary fallback: Manual IP connect link
            Text(
                text = "Or connect manually with IP",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary,
                modifier = Modifier
                    .padding(start = 27.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onManualConnectClick() }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 6) Bottom Pill Button: "Scan" (~56dp height, ~18sp bold text, animated press feedback)
            val scanInteractionSource = remember { MutableInteractionSource() }
            val isPressed by scanInteractionSource.collectIsPressedAsState()
            val buttonScale by animateFloatAsState(
                targetValue = if (isPressed) 0.96f else 1.0f,
                animationSpec = tween(durationMillis = 100),
                label = "scanPressScale"
            )

            Button(
                onClick = onScanClick,
                interactionSource = scanInteractionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(buttonScale)
                    .padding(bottom = 4.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.scanPillBg,
                    contentColor = colors.scanPillContent
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Scan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.scanPillContent,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
