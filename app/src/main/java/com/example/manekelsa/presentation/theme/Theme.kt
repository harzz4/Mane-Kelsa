package com.example.manekelsa.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = LeafGreen,
    onPrimary = Color.White,
    secondary = DeepTeal,
    onSecondary = Color.White,
    tertiary = WarmYellow,
    onTertiary = Ink,
    background = SoftSurface,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE6EEE8),
    onSurfaceVariant = Color(0xFF3D4A42),
    error = ErrorRed,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FD6B3),
    onPrimary = Color(0xFF003824),
    secondary = Color(0xFF8CCFDB),
    onSecondary = Color(0xFF00363F),
    tertiary = Color(0xFFFFD780),
    onTertiary = Color(0xFF3D2F00),
    background = Color(0xFF101511),
    onBackground = Color(0xFFE0E5DE),
    surface = Color(0xFF171D19),
    onSurface = Color(0xFFE0E5DE),
    surfaceVariant = Color(0xFF3F4942),
    onSurfaceVariant = Color(0xFFBFC9C1),
    error = Color(0xFFFFB4AB),
)

private val AppShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
)

@Composable
fun ManeKelsaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = AppShapes,
        typography = MaterialTheme.typography,
        content = content,
    )
}
