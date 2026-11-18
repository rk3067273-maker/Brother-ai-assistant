package com.brother.ai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Neon palette (matches the reference dashboard) ──
val NeonCyan = Color(0xFF00E5FF)
val NeonBlue = Color(0xFF2979FF)
val NeonPurple = Color(0xFFB388FF)
val NeonGreen = Color(0xFF00FF9D)
val NeonPink = Color(0xFFFF4081)
val DeepSpace = Color(0xFF0A0E1A)
val DeepPanel = Color(0xFF111827)
val PanelEdge = Color(0xFF1E2A4A)
val TextPrimary = Color(0xFFEAF6FF)
val TextDim = Color(0xFF8FA3C8)

private val BrotherColors = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonPurple,
    tertiary = NeonGreen,
    background = DeepSpace,
    surface = DeepPanel,
    surfaceVariant = PanelEdge,
    onPrimary = DeepSpace,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextDim
)

@Composable
fun BrotherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BrotherColors,
        content = content
    )
}