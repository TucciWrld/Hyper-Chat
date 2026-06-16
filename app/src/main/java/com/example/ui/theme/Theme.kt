package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberGreen,
    onPrimary = Color.Black,
    secondary = NeonAccent,
    background = DarkMidnight,
    surface = SlateCard,
    onBackground = Color.White,
    onSurface = Color.White,
    tertiary = CyberGreenDark,
    surfaceVariant = IncomingSlate,
    onSurfaceVariant = TextWhite
)

private val LightColorScheme = lightColorScheme(
    primary = CyberGreenDark,
    onPrimary = Color.White,
    secondary = CyberGreen,
    background = Color(0xFFF0F2F5),
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    tertiary = NeonAccent,
    surfaceVariant = Color(0xFFE8F5E9),
    onSurfaceVariant = Color.Black
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable to force high-fidelity hyper green branding
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
