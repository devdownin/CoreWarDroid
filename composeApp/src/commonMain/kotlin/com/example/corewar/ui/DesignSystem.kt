package com.example.corewar.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

data class CoreWarColors(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val accent: Color,
    val error: Color
)

val StandardColors = CoreWarColors(
    primary = Color.Green,
    secondary = Color.Gray,
    background = Color.Black,
    surface = Color(0xFF121212),
    accent = Color.Magenta,
    error = Color.Red
)

val RetroColors = CoreWarColors(
    primary = Color(0xFFFFA500), // Amber
    secondary = Color(0xFF553300),
    background = Color(0xFF110800),
    surface = Color(0xFF221100),
    accent = Color(0xFFFFCC00),
    error = Color.Red
)

val MatrixColors = CoreWarColors(
    primary = Color(0xFF00FF41), // Matrix Green
    secondary = Color(0xFF003B00),
    background = Color(0xFF000500),
    surface = Color(0xFF001100),
    accent = Color(0xFF008F11),
    error = Color.Red
)

val NeonColors = CoreWarColors(
    primary = Color(0xFF00FFFF), // Cyan
    secondary = Color(0xFFFF00FF), // Magenta
    background = Color(0xFF050005),
    surface = Color(0xFF110011),
    accent = Color(0xFFFFFF00), // Yellow
    error = Color.Red
)

val LocalCoreWarColors = staticCompositionLocalOf { StandardColors }

@Composable
fun CoreWarTheme(
    themeName: String = "STANDARD",
    content: @Composable () -> Unit
) {
    val colors = when (themeName) {
        "RETRO" -> RetroColors
        "MATRIX" -> MatrixColors
        "NEON" -> NeonColors
        else -> StandardColors
    }

    val materialColors = darkColorScheme(
        primary = colors.primary,
        secondary = colors.secondary,
        background = colors.background,
        surface = colors.surface,
        error = colors.error,
        onPrimary = colors.background,
        onSecondary = colors.primary,
        onBackground = colors.primary,
        onSurface = colors.primary
    )

    CompositionLocalProvider(LocalCoreWarColors provides colors) {
        MaterialTheme(
            colorScheme = materialColors,
            content = content
        )
    }
}
