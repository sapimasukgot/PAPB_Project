package com.example.easycooking.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryYellow,
    onPrimary = DarkText,
    primaryContainer = PrimaryYellow,
    onPrimaryContainer = DarkText,
    secondary = PrimaryOrange,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = DarkText,
    surface = Color.White,
    onSurface = DarkText,
    surfaceVariant = LightGrayBg,
    onSurfaceVariant = DarkTextSecondary
)

@Composable
fun EasyCookingTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
