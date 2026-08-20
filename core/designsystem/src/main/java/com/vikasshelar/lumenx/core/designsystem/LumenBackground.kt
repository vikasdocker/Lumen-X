package com.vikasshelar.lumenx.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun LumenBackground(
    isIlluminated: Boolean,
    illuminationStrength: Float = if (isIlluminated) 1f else 0f,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LumenColors.Void),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val strength = illuminationStrength.coerceIn(0f, 1f)
            val center = Offset(size.width / 2f, size.height * 0.36f)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        LumenColors.Ink,
                        LumenColors.Void,
                        Color.Black,
                    ),
                ),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LumenColors.BeamHot.copy(alpha = 0.05f + 0.2f * strength),
                        LumenColors.Beam.copy(alpha = 0.06f + 0.18f * strength),
                        LumenColors.Cyan.copy(alpha = 0.03f + 0.05f * strength),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = size.minDimension * (0.42f + 0.34f * strength),
                ),
                radius = size.minDimension,
                center = center,
            )
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        LumenColors.Beam.copy(alpha = 0.02f + 0.07f * strength),
                        Color.Transparent,
                    ),
                    startY = size.height * 0.18f,
                    endY = size.height * 0.92f,
                ),
            )
        }
        content()
    }
}
