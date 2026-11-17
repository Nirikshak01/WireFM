package com.wirefm.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wirefm.app.R
import com.wirefm.app.ui.theme.DarkBackground
import com.wirefm.app.ui.theme.DarkCardBg
import com.wirefm.app.ui.theme.DarkCardBorder
import com.wirefm.app.ui.theme.PillPrimary
import com.wirefm.app.ui.theme.PillPrimaryContent
import com.wirefm.app.ui.theme.TextPrimary
import com.wirefm.app.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    currentThemeMode: String, // "system", "light", "dark"
    onThemeModeChanged: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
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
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1) Circular Logo Container
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E24))
                    .border(1.dp, Color(0xFF2E2E38), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_ninja_white),
                    contentDescription = "Ninja Logo",
                    modifier = Modifier
                        .width(48.dp)
                        .height(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2) App Version Subtitle
            Text(
                text = "app version 2.0.0",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3) WireFM Pill Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(PillPrimary)
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "WireFM",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PillPrimaryContent
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 4) Theme Switcher (System / White / dark)
            ThemeSwitcherCard(
                currentMode = currentThemeMode,
                onSelectMode = onThemeModeChanged
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5) Action Pill Buttons (Check update / Github / Privacy)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    icon = Icons.Default.Settings, // Or GitHub cat
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF16161C))
            .border(1.dp, Color(0xFF262630), RoundedCornerShape(20.dp))
            .padding(5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemeSegmentItem(
                title = "System",
                icon = Icons.Default.Settings,
                isSelected = currentMode == "system",
                onClick = { onSelectMode("system") },
                modifier = Modifier.weight(1f)
            )

            ThemeSegmentItem(
                title = "White",
                icon = Icons.Default.WbSunny,
                isSelected = currentMode == "light",
                onClick = { onSelectMode("light") },
                modifier = Modifier.weight(1f)
            )

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
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF282834) else Color.Transparent,
        animationSpec = tween(200),
        label = "themeSegmentBg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) TextPrimary else TextSecondary,
        animationSpec = tween(200),
        label = "themeSegmentContent"
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
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
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
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF181820),
            contentColor = TextPrimary
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
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}
