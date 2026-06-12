package com.asitsounds.android.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Hangul transliteration is shown in this blue (matches iOS app accent).
val HangulBlue = Color(0xFF2563EB)
val HangulBlueDark = Color(0xFF60A5FA)

private val LightColors = lightColorScheme(
    primary = HangulBlue,
    secondary = Color(0xFF475569)
)

private val DarkColors = darkColorScheme(
    primary = HangulBlueDark,
    secondary = Color(0xFF94A3B8)
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Composable
fun AsItSoundsTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT  -> false
        ThemeMode.DARK   -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content
    )
}
