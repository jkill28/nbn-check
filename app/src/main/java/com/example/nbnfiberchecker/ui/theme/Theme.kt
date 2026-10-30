package com.example.nbnfiberchecker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Material 3 Expressive - Shades of Grey with Bright Neon Accents (Dark Mode)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00D2FF),         // Electric Cyan
    onPrimary = Color(0xFF031520),
    primaryContainer = Color(0xFF0A2E3D),
    onPrimaryContainer = Color(0xFFB8EFFF),

    secondary = Color(0xFF00F59B),       // Vivid Mint / Neon Emerald
    onSecondary = Color(0xFF022115),
    secondaryContainer = Color(0xFF083824),
    onSecondaryContainer = Color(0xFFA3FAD6),

    tertiary = Color(0xFFFF9F1C),        // Cyber Amber
    onTertiary = Color(0xFF261300),
    tertiaryContainer = Color(0xFF3D2600),
    onTertiaryContainer = Color(0xFFFFD8A8),

    background = Color(0xFF0C0F15),      // Deep graphite canvas
    onBackground = Color(0xFFF1F5F9),

    surface = Color(0xFF151922),         // Layered slate container
    onSurface = Color(0xFFF1F5F9),

    surfaceVariant = Color(0xFF1C222E),  // Elevated container / chip
    onSurfaceVariant = Color(0xFF94A3B8),

    outline = Color(0xFF2D3548),
    outlineVariant = Color(0xFF1D2330),

    error = Color(0xFFFF4D6D),          // Coral Rose
    onError = Color(0xFFFFFFFF)
)

// Material 3 Expressive - Shades of Pure White with Vibrant Accents (Light Mode)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0066FF),         // Vivid Royal Azure
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0ECFF),
    onPrimaryContainer = Color(0xFF003D99),

    secondary = Color(0xFF059669),       // Brilliant Forest Emerald
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF065F46),

    tertiary = Color(0xFFD97706),        // Warm Amber Gold
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF78350F),

    background = Color(0xFFF7F9FD),      // Ethereal porcelain mist
    onBackground = Color(0xFF0F172A),    // Midnight charcoal slate

    surface = Color(0xFFFFFFFF),         // Pure crisp white
    onSurface = Color(0xFF0F172A),

    surfaceVariant = Color(0xFFF1F4F9),  // Pale pearl white
    onSurfaceVariant = Color(0xFF475569),

    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFEEF2F6),

    error = Color(0xFFEF4444),          // Vibrant Crimson
    onError = Color(0xFFFFFFFF)
)

@Immutable
data class ExtendedColors(
    val eligibleStatus: Color,
    val plannedStatus: Color,
    val activeAccent: Color,
    val cardBorder: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        eligibleStatus = Color.Unspecified,
        plannedStatus = Color.Unspecified,
        activeAccent = Color.Unspecified,
        cardBorder = Color.Unspecified
    )
}

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.3).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.25.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun NBNFiberCheckerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = ExtendedColors(
        eligibleStatus = if (darkTheme) Color(0xFF00F59B) else Color(0xFF059669),
        plannedStatus = if (darkTheme) Color(0xFFFF9F1C) else Color(0xFFD97706),
        activeAccent = if (darkTheme) Color(0xFF00D2FF) else Color(0xFF0066FF),
        cardBorder = if (darkTheme) Color(0x1FFFFFFF) else Color(0x0F000000)
    )

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
