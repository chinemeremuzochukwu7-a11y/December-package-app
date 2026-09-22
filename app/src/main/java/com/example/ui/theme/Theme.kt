package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = HolidayCrimsonLight,
    onPrimary = Color.White,
    primaryContainer = HolidayCrimsonDark,
    onPrimaryContainer = Color(0xFFFFCDD2),
    secondary = HolidayPineGreen,
    onSecondary = Color.White,
    secondaryContainer = HolidayPineGreenDark,
    onSecondaryContainer = Color(0xFFC8E6C9),
    tertiary = HolidayGoldLight,
    onTertiary = Color(0xFF3E2723),
    tertiaryContainer = HolidayGoldDark,
    onTertiaryContainer = Color(0xFFFFF8E1),
    background = HolidayBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = HolidaySurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = HolidaySurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = HolidayBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = HolidayCrimson,
    onPrimary = Color.White,
    primaryContainer = HolidayCrimsonContainer,
    onPrimaryContainer = HolidayOnCrimsonContainer,
    secondary = HolidayPineGreen,
    onSecondary = Color.White,
    secondaryContainer = HolidayPineContainer,
    onSecondaryContainer = HolidayOnPineContainer,
    tertiary = HolidayGold,
    onTertiary = Color.White,
    tertiaryContainer = HolidayGoldContainer,
    onTertiaryContainer = Color(0xFF5D4037),
    background = HolidayBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = HolidaySurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = HolidaySurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = HolidayBorderLight
)

@Composable
fun HolidayWishesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
