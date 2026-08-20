package com.vikasshelar.lumenx.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LumenDarkScheme = darkColorScheme(
    primary = LumenColors.Beam,
    onPrimary = LumenColors.Void,
    secondary = LumenColors.Cyan,
    onSecondary = LumenColors.Void,
    tertiary = LumenColors.Signal,
    background = LumenColors.Void,
    onBackground = LumenColors.Mist,
    surface = LumenColors.Carbon,
    onSurface = LumenColors.Mist,
    surfaceVariant = LumenColors.Graphite,
    onSurfaceVariant = LumenColors.Steel,
    error = LumenColors.Signal,
)

@Composable
fun LumenXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LumenDarkScheme,
        typography = androidx.compose.material3.Typography(
            displayMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 44.sp,
                lineHeight = 48.sp,
                letterSpacing = 0.sp,
            ),
            headlineSmall = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp,
            ),
            titleMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.sp,
            ),
            labelLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.sp,
            ),
            bodyMedium = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.sp,
            ),
        ),
        content = content,
    )
}
