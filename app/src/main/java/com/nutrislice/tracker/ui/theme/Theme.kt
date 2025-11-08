package com.nutrislice.tracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = SurfaceWhite,
    background = PrimaryBackground,
    surface = SurfaceWhite,
    onSurface = ColorPalette.OnSurface,
    secondary = PrimaryBlue
)

private val DarkColors = darkColorScheme(
    primary = PrimaryGreen,
    secondary = PrimaryBlue
)

private object ColorPalette {
    val OnSurface = Color(0xFF1F2937)
}

@Composable
fun NutrisliceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
