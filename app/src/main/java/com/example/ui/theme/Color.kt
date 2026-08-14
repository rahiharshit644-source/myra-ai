package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Vibrant Palette - Core Colors
val PinkPrimary = Color(0xFFDB2777)       // Pink-600 (Primary Action & Branding)
val PinkSecondary = Color(0xFFEC4899)     // Pink-500 (Vibrant Hot Pink)
val PinkTertiary = Color(0xFFF472B6)      // Pink-400 (Soft Warm Accent)
val PinkAccent = Color(0xFFE11D48)        // Rose-600 (Punchy Glow)
val PinkBackground = Color(0xFFFFF0F6)    // #FFF0F6 Vibrant Palette Canvas
val PinkSurface = Color(0xFFFFF7FA)       // High-key Clean Surface
val PinkSurfaceVariant = Color(0xFFFCE7F3)// Pink-100 Subtle Tint

val PurpleGlow = Color(0xFFD946EF)        // Fuchsia-500 Glow
val FuchsiaSoft = Color(0xFFF0ABFC)       // Fuchsia-300
val LavenderSoft = Color(0xFFE8DEF8)

val TextPrimary = Color(0xFF0F172A)       // Slate-900 (High Contrast)
val TextSecondary = Color(0xFF64748B)     // Slate-500 (Clean Neutral Subtitles)
val TextTertiary = Color(0xFF94A3B8)      // Slate-400 (Muted Meta)

val GlassWhite = Color(0xEBFFFFFF)
val GlassWhiteFaint = Color(0x80FFFFFF)
val GlassBorder = Color(0x66FFFFFF)
val GlassBorderPink = Color(0x40EC4899)

val GradientPinkCanvas = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFF0F6),
        Color(0xFFFFECF4),
        Color(0xFFFCE7F3),
        Color(0xFFFBCFE8)
    )
)

val GradientVibrantOrb = Brush.linearGradient(
    colors = listOf(
        Color(0xFFF472B6), // Pink-400
        Color(0xFFEC4899), // Pink-500
        Color(0xFFD946EF)  // Fuchsia-500
    )
)

val GradientGlassButton = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFF7FB),
        Color(0xFFFCE7F3)
    )
)

val GradientNeonGlow = Brush.radialGradient(
    colors = listOf(
        Color(0x99EC4899),
        Color(0x4DD946EF),
        Color(0x00F472B6)
    )
)

