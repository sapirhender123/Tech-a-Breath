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
    primary          = Teal400,
    onPrimary        = White,
    primaryContainer = Teal700,
    onPrimaryContainer = OnDark,

    secondary        = Teal300,
    onSecondary      = White,
    secondaryContainer = Teal800,
    onSecondaryContainer = OnDarkSub,

    tertiary         = Amber500,
    onTertiary       = Teal900,

    background       = Color(0xFF0D3D47),
    onBackground     = OnDark,

    surface          = Teal700,
    onSurface        = OnDark,
    surfaceVariant   = Teal800,
    onSurfaceVariant = OnDarkSub,

    outline          = Teal600,
    error            = Color(0xFFCF6679)
)

private val LightColorScheme = lightColorScheme(
    primary          = Teal700,
    onPrimary        = White,
    primaryContainer = Teal50,
    onPrimaryContainer = Teal900,

    secondary        = Teal500,
    onSecondary      = White,
    secondaryContainer = Teal100,
    onSecondaryContainer = Teal900,

    tertiary         = Amber500,
    onTertiary       = Teal900,

    background       = Teal50,
    onBackground     = Teal900,

    surface          = White,
    onSurface        = Teal900,
    surfaceVariant   = Color(0xFFEBF6F8),
    onSurfaceVariant = Teal700,

    outline          = Teal200,
    error            = Color(0xFFB00020)
)

@Composable
fun TechABreathTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dynamic color deliberately disabled — we own the palette
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
