package com.wirefm.app.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class WireFMColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val switcherBox: Color,
    val switcherSelected: Color,
    val switcherSelectedContent: Color,
    val switcherUnselectedContent: Color,
    val buttonContainer: Color,
    val buttonContent: Color,
    val folderCard: Color,
    val folderCardContent: Color,
    val searchBarBg: Color,
    val searchBarContent: Color,
    val searchBtnBg: Color,
    val searchBtnContent: Color,
    val scanPillBg: Color,
    val scanPillContent: Color,
    val fabBg: Color,
    val fabContent: Color,
    val cardBorder: Color,
    val accentGreen: Color,
    val popupBg: Color,
    val popupBorder: Color,
    val isDark: Boolean
)

val LocalWireFMColors = compositionLocalOf<WireFMColors> {
    error("No WireFMColors provided")
}

object WireFMTheme {
    val colors: WireFMColors
        @Composable
        @ReadOnlyComposable
        get() = LocalWireFMColors.current
}

@Composable
fun WireFMTheme(
    themeMode: String = "system",
    darkTheme: Boolean = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    },
    content: @Composable () -> Unit
) {
    val isDark = darkTheme
    val animSpec = tween<Color>(durationMillis = 400, easing = FastOutSlowInEasing)

    // Animated semantic tokens
    val animatedBackground by animateColorAsState(
        targetValue = if (isDark) DarkBackground else LightBackground,
        animationSpec = animSpec, label = "bg"
    )
    val animatedSurface by animateColorAsState(
        targetValue = if (isDark) DarkSurface else LightSurface,
        animationSpec = animSpec, label = "surface"
    )
    val animatedSurfaceVariant by animateColorAsState(
        targetValue = if (isDark) DarkSurfaceVariant else LightSurfaceVariant,
        animationSpec = animSpec, label = "surfaceVariant"
    )
    val animatedOnSurface by animateColorAsState(
        targetValue = if (isDark) TextPrimary else TextPrimaryLight,
        animationSpec = animSpec, label = "onSurface"
    )
    val animatedTextPrimary by animateColorAsState(
        targetValue = if (isDark) TextPrimary else TextPrimaryLight,
        animationSpec = animSpec, label = "textPrimary"
    )
    val animatedTextSecondary by animateColorAsState(
        targetValue = if (isDark) TextSecondary else TextSecondaryLight,
        animationSpec = animSpec, label = "textSecondary"
    )
    val animatedTextTertiary by animateColorAsState(
        targetValue = if (isDark) TextTertiary else TextTertiaryLight,
        animationSpec = animSpec, label = "textTertiary"
    )
    val animatedSwitcherBox by animateColorAsState(
        targetValue = if (isDark) Color(0xFF2A2A2D) else Color(0xFFE2E8F0),
        animationSpec = animSpec, label = "switcherBox"
    )
    val animatedSwitcherSelected by animateColorAsState(
        targetValue = if (isDark) Color(0xFF3A3C40) else Color(0xFFFFFFFF),
        animationSpec = animSpec, label = "switcherSelected"
    )
    val animatedSwitcherSelectedContent by animateColorAsState(
        targetValue = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0B0B0C),
        animationSpec = animSpec, label = "switcherSelectedContent"
    )
    val animatedSwitcherUnselectedContent by animateColorAsState(
        targetValue = if (isDark) Color(0xFF8F8F9D) else Color(0xFF64748B),
        animationSpec = animSpec, label = "switcherUnselectedContent"
    )
    val animatedButtonContainer by animateColorAsState(
        targetValue = if (isDark) Color(0xFF2E3236) else Color(0xFFE3E8EA),
        animationSpec = animSpec, label = "buttonContainer"
    )
    val animatedButtonContent by animateColorAsState(
        targetValue = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0B0B0C),
        animationSpec = animSpec, label = "buttonContent"
    )
    val animatedFolderCard by animateColorAsState(
        targetValue = if (isDark) FolderCardDark else FolderCardLight,
        animationSpec = animSpec, label = "folderCard"
    )
    val animatedFolderCardContent by animateColorAsState(
        targetValue = FolderIconDark,
        animationSpec = animSpec, label = "folderCardContent"
    )
    val animatedSearchBarBg by animateColorAsState(
        targetValue = if (isDark) Color(0xFF1E2024) else Color(0xFFE2E8F0),
        animationSpec = animSpec, label = "searchBarBg"
    )
    val animatedSearchBarContent by animateColorAsState(
        targetValue = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0B0B0C),
        animationSpec = animSpec, label = "searchBarContent"
    )
    val animatedSearchBtnBg by animateColorAsState(
        targetValue = if (isDark) Color(0xFF2E3238) else Color(0xFFCBD5E1),
        animationSpec = animSpec, label = "searchBtnBg"
    )
    val animatedSearchBtnContent by animateColorAsState(
        targetValue = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0B0B0C),
        animationSpec = animSpec, label = "searchBtnContent"
    )
    val animatedScanPillBg by animateColorAsState(
        targetValue = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0B0B0C),
        animationSpec = animSpec, label = "scanPillBg"
    )
    val animatedScanPillContent by animateColorAsState(
        targetValue = if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF),
        animationSpec = animSpec, label = "scanPillContent"
    )
    val animatedFabBg by animateColorAsState(
        targetValue = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0B0B0C),
        animationSpec = animSpec, label = "fabBg"
    )
    val animatedFabContent by animateColorAsState(
        targetValue = if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF),
        animationSpec = animSpec, label = "fabContent"
    )
    val animatedCardBorder by animateColorAsState(
        targetValue = if (isDark) DarkCardBorder else LightCardBorder,
        animationSpec = animSpec, label = "cardBorder"
    )
    val animatedPopupBg by animateColorAsState(
        targetValue = if (isDark) Color(0xFF131417) else Color(0xFFFFFFFF),
        animationSpec = animSpec, label = "popupBg"
    )

    val wireFMColors = WireFMColors(
        background = animatedBackground,
        surface = animatedSurface,
        surfaceVariant = animatedSurfaceVariant,
        onSurface = animatedOnSurface,
        textPrimary = animatedTextPrimary,
        textSecondary = animatedTextSecondary,
        textTertiary = animatedTextTertiary,
        switcherBox = animatedSwitcherBox,
        switcherSelected = animatedSwitcherSelected,
        switcherSelectedContent = animatedSwitcherSelectedContent,
        switcherUnselectedContent = animatedSwitcherUnselectedContent,
        buttonContainer = animatedButtonContainer,
        buttonContent = animatedButtonContent,
        folderCard = animatedFolderCard,
        folderCardContent = animatedFolderCardContent,
        searchBarBg = animatedSearchBarBg,
        searchBarContent = animatedSearchBarContent,
        searchBtnBg = animatedSearchBtnBg,
        searchBtnContent = animatedSearchBtnContent,
        scanPillBg = animatedScanPillBg,
        scanPillContent = animatedScanPillContent,
        fabBg = animatedFabBg,
        fabContent = animatedFabContent,
        cardBorder = animatedCardBorder,
        accentGreen = AccentGreen,
        popupBg = animatedPopupBg,
        popupBorder = PopupBorderBlue,
        isDark = isDark
    )

    // Update system status bar icons based on theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    val m3ColorScheme = if (isDark) {
        darkColorScheme(
            primary = animatedScanPillBg,
            onPrimary = animatedScanPillContent,
            background = animatedBackground,
            onBackground = animatedTextPrimary,
            surface = animatedSurface,
            onSurface = animatedOnSurface,
            surfaceVariant = animatedSurfaceVariant,
            onSurfaceVariant = animatedTextSecondary,
            outline = animatedCardBorder,
            error = AccentRed
        )
    } else {
        lightColorScheme(
            primary = animatedScanPillBg,
            onPrimary = animatedScanPillContent,
            background = animatedBackground,
            onBackground = animatedTextPrimary,
            surface = animatedSurface,
            onSurface = animatedOnSurface,
            surfaceVariant = animatedSurfaceVariant,
            onSurfaceVariant = animatedTextSecondary,
            outline = animatedCardBorder,
            error = AccentRed
        )
    }

    CompositionLocalProvider(LocalWireFMColors provides wireFMColors) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = WireFMTypography,
            content = content
        )
    }
}
