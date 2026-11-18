package com.wirefm.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wirefm.app.R
import com.wirefm.app.ui.theme.WireFMTheme

@Composable
fun SettingsScreen(
    currentThemeMode: String, // "system", "light", "dark"
    onThemeModeChanged: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Back Arrow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 1) Logo: ~72dp rounded WireFM tile (Crossfade between dark/light tile)
            Crossfade(
                targetState = colors.isDark,
                animationSpec = tween(400, easing = FastOutSlowInEasing),
                label = "settingsLogoCrossfade"
            ) { isDark ->
                Image(
                    painter = painterResource(
                        id = if (isDark) R.drawable.wirefm_tile_white else R.drawable.wirefm_tile_dark
                    ),
                    contentDescription = "WireFM Logo Tile",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2) App Version Subtitle (14sp)
            Text(
                text = "app version 2.0.0",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3) WireFM Pill Badge (height ~44dp, text 24sp bold)
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.scanPillBg)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "WireFM",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.scanPillContent
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 4) Theme Switcher (System / White / dark)
            // Screen width ~75%, center, height ~78dp, narrow look
            Box(
                modifier = Modifier.fillMaxWidth(0.75f),
                contentAlignment = Alignment.Center
            ) {
                ThemeSwitcherCard(
                    currentMode = currentThemeMode,
                    onSelectMode = onThemeModeChanged
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 5) Action Pill Buttons (Check update / Github / Privacy)
            // Screen width ~83%, height 56dp, full pill, text 20sp bold, icon 20dp, 10dp gap
            Column(
                modifier = Modifier.fillMaxWidth(0.83f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SettingsActionPill(
                    title = "Check update",
                    icon = Icons.Default.CheckCircleOutline,
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/jasmin1727/WireFM/releases")
                        )
                        context.startActivity(intent)
                    }
                )

                SettingsActionPill(
                    title = "Github",
                    icon = Icons.Default.Settings,
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/jasmin1727/WireFM")
                        )
                        context.startActivity(intent)
                    }
                )

                SettingsActionPill(
                    title = "Privacy",
                    icon = Icons.Default.Security,
                    onClick = {
                        Toast.makeText(
                            context,
                            "WireFM works 100% locally on your Wi-Fi network without telemetry.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }
    }
}

@Composable
fun ThemeSwitcherCard(
    currentMode: String,
    onSelectMode: (String) -> Unit
) {
    val colors = WireFMTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(colors.switcherBox)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(22.dp))
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ThemeSegmentItem(
                title = "System",
                icon = Icons.Default.Settings,
                isSelected = currentMode == "system",
                onClick = { onSelectMode("system") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(4.dp))

            ThemeSegmentItem(
                title = "White",
                icon = Icons.Default.WbSunny,
                isSelected = currentMode == "light",
                onClick = { onSelectMode("light") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(4.dp))

            ThemeSegmentItem(
                title = "dark",
                icon = Icons.Default.DarkMode,
                isSelected = currentMode == "dark",
                onClick = { onSelectMode("dark") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeSegmentItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = WireFMTheme.colors

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) colors.switcherSelected else Color.Transparent,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "segmentBg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) colors.switcherSelectedContent else colors.switcherUnselectedContent,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "segmentContent"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
fun SettingsActionPill(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colors = WireFMTheme.colors

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.buttonContainer,
            contentColor = colors.buttonContent
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.buttonContent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.buttonContent
            )
        }
    }
}
