package com.example.tech_a_breath.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary          = Indigo400,
    onPrimary        = White,
    primaryContainer = Indigo800,
    onPrimaryContainer = OnDark,

    secondary        = Indigo300,
    onSecondary      = White,
    secondaryContainer = Indigo900,
    onSecondaryContainer = OnDarkSub,

    tertiary         = Amber500,
    onTertiary       = Indigo900,

    background       = Indigo900,
    onBackground     = OnDark,

    surface          = Indigo800,
    onSurface        = OnDark,
    surfaceVariant   = Indigo700,
    onSurfaceVariant = OnDarkSub,

    outline          = Indigo600,
    error            = Color(0xFFCF6679)
)

private val LightColorScheme = lightColorScheme(
    primary          = Indigo700,
    onPrimary        = White,
    primaryContainer = Indigo100,
    onPrimaryContainer = Indigo900,

    secondary        = Indigo500,
    onSecondary      = White,
    secondaryContainer = Indigo200,
    onSecondaryContainer = Indigo900,

    tertiary         = Amber500,
    onTertiary       = Indigo900,

    background       = Indigo50,
    onBackground     = Indigo900,

    surface          = White,
    onSurface        = Indigo900,
    surfaceVariant   = Color(0xFFEBF6F8),
    onSurfaceVariant = Indigo700,

    outline          = Indigo200,
    error            = Color(0xFFB00020)
)

@Composable
fun TechABreathTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We favor the dark theme for the "Designed" immersive look
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme // Forcing Dark for now as requested

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
