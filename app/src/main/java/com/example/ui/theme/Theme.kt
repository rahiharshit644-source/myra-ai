package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PinkSurfaceVariant,
    onPrimaryContainer = TextPrimary,
    secondary = PinkSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = PinkTertiary,
    onSecondaryContainer = TextPrimary,
    tertiary = PurpleGlow,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    background = PinkBackground,
    onBackground = TextPrimary,
    surface = PinkSurface,
    onSurface = TextPrimary,
    surfaceVariant = PinkSurfaceVariant,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
