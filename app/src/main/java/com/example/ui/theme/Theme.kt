package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    onPrimary = StudioBlack,
    primaryContainer = SpotifyGreen.copy(alpha = 0.2f),
    onPrimaryContainer = SpotifyGreenBright,
    secondary = NeonCyan,
    onSecondary = StudioBlack,
    secondaryContainer = NeonCyan.copy(alpha = 0.15f),
    onSecondaryContainer = NeonCyan,
    tertiary = ElectricPurple,
    onTertiary = StudioBlack,
    background = StudioDarkBg,
    onBackground = TextWhitePrimary,
    surface = StudioCardSurface,
    onSurface = TextWhitePrimary,
    surfaceVariant = StudioCardElevated,
    onSurfaceVariant = TextGraySecondary,
    outline = StudioBorder,
    error = CoralRed
)

// For StemSpire music studio experience, dark theme provides the authentic studio atmosphere
private val LightColorScheme = DarkColorScheme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to dark music studio theme
    dynamicColor: Boolean = false, // Keep signature Spotify green and studio aesthetics
    content: @Composable () -> Unit,
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = StudioBlack.toArgb()
                window.navigationBarColor = StudioBlack.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
