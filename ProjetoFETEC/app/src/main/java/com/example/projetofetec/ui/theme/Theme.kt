package com.example.projetofetec.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CrimsonRed,
    onPrimary = Color.White,
    primaryContainer = CrimsonRed.copy(alpha = 0.35f),
    onPrimaryContainer = Color.White,
    secondary = SlateGrey,
    onSecondary = Color.White,
    background = MidnightBlack,
    surface = DeepGrey,
    onSurface = Color.White,
    onSurfaceVariant = Color.White.copy(alpha = 0.7f),
    outline = Color.White.copy(alpha = 0.5f),
    outlineVariant = Color.White.copy(alpha = 0.2f),
    surfaceVariant = SlateGrey.copy(alpha = 0.5f),
    onBackground = Color.White,
    error = ElectricRed
)

private val LightColorScheme = lightColorScheme(
    primary = CrimsonRed,
    onPrimary = Color.White,
    primaryContainer = CrimsonRed.copy(alpha = 0.1f),
    onPrimaryContainer = CrimsonRed,
    secondary = SlateGrey,
    onSecondary = Color.White,
    background = Color(0xFFF8F9FA),
    surface = Color.White,
    onSurface = MidnightBlack,
    onSurfaceVariant = MidnightBlack.copy(alpha = 0.7f),
    outline = MidnightBlack.copy(alpha = 0.3f),
    outlineVariant = MidnightBlack.copy(alpha = 0.1f),
    surfaceVariant = Color(0xFFE9ECEF),
    onBackground = MidnightBlack,
    error = CrimsonRed
)

@Composable
fun SilentSOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
