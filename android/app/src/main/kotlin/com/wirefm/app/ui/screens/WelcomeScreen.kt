package com.wirefm.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wirefm.app.R
import com.wirefm.app.ui.theme.DarkBackground
import com.wirefm.app.ui.theme.PillPrimary
import com.wirefm.app.ui.theme.PillPrimaryContent
import com.wirefm.app.ui.theme.TextPrimary
import com.wirefm.app.ui.theme.TextSecondary

@Composable
fun WelcomeScreen(
    onScanClick: () -> Unit,
    onManualConnectClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {
            // 1) Top Bar: Ninja Logo on left (44dp width), Settings gear on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(
                        id = if (isDark) R.drawable.logo_ninja_white else R.drawable.logo_ninja_black
                    ),
                    contentDescription = "WireFM Ninja Logo",
                    modifier = Modifier
                        .width(44.dp)
                        .height(24.dp),
                    contentScale = ContentScale.Fit
                )

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 2) "Welcome" Heading (Large bold font ~30sp)
            Text(
                text = "Welcome",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3) Illustration Card: qr_scan_card_preview.png (FillWidth, NO CLIPPING so boy's shoes overflow cleanly)
            Image(
                painter = painterResource(id = R.drawable.qr_scan_card_preview),
                contentDescription = "Connect with WireFM Scan with your PC",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                contentScale = ContentScale.FillWidth
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 4) Information Prompt: (!) "Connect with WireFM Scan with you pc" (~16sp bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Info",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Connect with WireFM Scan with you pc",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Push bottom buttons down to the bottom
            Spacer(modifier = Modifier.weight(1f))

            // 5) Bottom Section: Scan Pill Button + Manual IP Option
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.96f else 1.0f,
                    label = "btnScale"
                )

                Button(
                    onClick = onScanClick,
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .scale(scale)
                        .width(190.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PillPrimary,
                        contentColor = PillPrimaryContent
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "Scan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PillPrimaryContent
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Or connect manually with IP",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clickable { onManualConnectClick() }
                        .padding(8.dp)
                )
            }
        }
    }
}
