package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AuraDarkColorScheme = darkColorScheme(
    primary = AuraAccent,
    onPrimary = Color.White,
    primaryContainer = SurfaceContainerHigh,
    onPrimaryContainer = Color.White,
    secondary = LiquidSilver,
    onSecondary = ObsidianDark,
    secondaryContainer = SurfaceContainerDark,
    onSecondaryContainer = Color.White,
    tertiary = LosslessGold,
    background = VoidBlack,
    onBackground = TextPrimary,
    surface = ObsidianDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondary,
    outline = GlassCardBorder,
    outlineVariant = GlassCardBorderSubtle
)

@Composable
fun AuraMusicTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AuraDarkColorScheme,
        typography = Typography,
        content = content
    )
}
