package com.llins95.comparadordecerveja.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF7A5700),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDEA3),
    onPrimaryContainer = Color(0xFF261A00),
    secondary = Color(0xFF6C5D3F),
    secondaryContainer = Color(0xFFF6E1BB),
    tertiary = Color(0xFF4E6544),
    surface = Color(0xFFFFF8F2),
    background = Color(0xFFFFF8F2)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF4BE49),
    onPrimary = Color(0xFF402D00),
    primaryContainer = Color(0xFF5C4200),
    onPrimaryContainer = Color(0xFFFFDEA3),
    secondary = Color(0xFFD8C4A0),
    secondaryContainer = Color(0xFF51462F),
    tertiary = Color(0xFFB5CEA8),
    surface = Color(0xFF17130B),
    background = Color(0xFF17130B)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun CervaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = AppShapes,
        content = content
    )
}
