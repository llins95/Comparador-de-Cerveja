package com.llins95.comparadordecerveja.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF7A5700),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDEA3),
    onPrimaryContainer = Color(0xFF261A00),
    secondary = Color(0xFF6C5D3F),
    surface = Color(0xFFFFF8F2),
    background = Color(0xFFFFF8F2)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF4BE49),
    primaryContainer = Color(0xFF5C4200),
    secondary = Color(0xFFD8C4A0),
    surface = Color(0xFF17130B),
    background = Color(0xFF17130B)
)

@Composable
fun ComparadorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
