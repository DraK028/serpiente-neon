package com.jcbernal.serpienteneon.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object Neon {
    val Background = Color(0xFF0B0820)
    val BackgroundTop = Color(0xFF1A1040)
    val Board = Color(0xFF110C2E)
    val GridDot = Color(0xFF2A2160)
    val Cyan = Color(0xFF00F5FF)
    val Magenta = Color(0xFFFF2BD6)
    val Yellow = Color(0xFFFFE14D)
    val Text = Color(0xFFE9E6FF)
    val TextDim = Color(0xFF8C86B8)
    val Danger = Color(0xFFFF4D6D)
}

private val ArcadeFont = FontFamily.Monospace

private val NeonTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = ArcadeFont,
        fontWeight = FontWeight.Black,
        fontSize = 26.sp,
        letterSpacing = 2.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = ArcadeFont,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = 1.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = ArcadeFont,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = ArcadeFont,
        fontSize = 14.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = ArcadeFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 2.sp,
    ),
)

private val NeonColors = darkColorScheme(
    primary = Neon.Cyan,
    secondary = Neon.Magenta,
    tertiary = Neon.Yellow,
    background = Neon.Background,
    surface = Neon.Board,
    onPrimary = Neon.Background,
    onSecondary = Neon.Background,
    onBackground = Neon.Text,
    onSurface = Neon.Text,
    error = Neon.Danger,
)

@Composable
fun SerpienteNeonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NeonColors,
        typography = NeonTypography,
        content = content,
    )
}
