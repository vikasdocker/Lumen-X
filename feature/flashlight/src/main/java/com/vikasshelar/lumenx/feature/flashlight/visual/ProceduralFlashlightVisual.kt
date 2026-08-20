package com.vikasshelar.lumenx.feature.flashlight.visual

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.vikasshelar.lumenx.core.designsystem.LumenColors
import com.vikasshelar.lumenx.core.model.FlashlightMode
import kotlin.math.cos
import kotlin.math.sin

object ProceduralFlashlightVisual : FlashlightVisual {
    @Composable
    override fun Render(
        model: FlashlightModel,
        state: FlashlightVisualState,
        modifier: Modifier,
    ) {
        val accent = state.mode.accentColor
        val powerFraction = state.powerFraction
        val brightnessFraction = state.brightnessFraction

        Canvas(modifier = modifier) {
            val centerX = size.width / 2f + state.camera.parallaxX * 10.dp.toPx()
            val lensRadius = size.width * 0.22f
            val lensCenter = Offset(
                x = centerX + state.camera.parallaxX * 8.dp.toPx(),
                y = size.height * 0.18f + state.camera.parallaxY * 5.dp.toPx(),
            )
            val bodyTop = size.height * 0.2f
            val bodyHeight = size.height * 0.66f
            val bodyWidth = size.width * 0.46f
            val bodyLeft = centerX - bodyWidth / 2f
            val bodyRadius = bodyWidth * 0.22f
            val glowRadius = size.minDimension * (0.33f + 0.24f * brightnessFraction)
            val ambientGlow = state.glow.ambientGlow

            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = state.environment.floorShadowStrength),
                        Color.Transparent,
                    ),
                    center = Offset(centerX, size.height * 0.9f),
                    radius = size.width * 0.38f,
                ),
                topLeft = Offset(centerX - size.width * 0.36f, size.height * 0.82f),
                size = Size(size.width * 0.72f, size.height * 0.14f),
            )

            if (state.glow.lensGlow > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LumenColors.BeamHot.copy(alpha = 0.36f * state.glow.lensGlow),
                            LumenColors.Beam.copy(alpha = 0.2f * state.glow.beamGlow.coerceAtLeast(0.18f)),
                            accent.copy(alpha = 0.08f * powerFraction),
                            accent.copy(alpha = state.environment.atmosphericGlow),
                            Color.Transparent,
                        ),
                        center = lensCenter,
                        radius = glowRadius,
                    ),
                    radius = glowRadius,
                    center = lensCenter,
                )
            }

            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        LumenColors.Ink,
                        LumenColors.Steel.copy(alpha = 0.3f + state.lighting.keyLightIntensity * 0.12f),
                        LumenColors.Graphite,
                        LumenColors.Void,
                    ),
                ),
                topLeft = Offset(bodyLeft, bodyTop),
                size = Size(bodyWidth, bodyHeight),
                cornerRadius = CornerRadius(bodyRadius, bodyRadius),
            )

            drawRoundRect(
                color = Color.Black.copy(alpha = 0.32f),
                topLeft = Offset(bodyLeft + bodyWidth * 0.82f, bodyTop + bodyHeight * 0.05f),
                size = Size(bodyWidth * 0.08f, bodyHeight * 0.86f),
                cornerRadius = CornerRadius(bodyRadius, bodyRadius),
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.1f + state.lighting.fillLightIntensity * 0.12f),
                topLeft = Offset(bodyLeft + bodyWidth * 0.14f, bodyTop + bodyHeight * 0.07f),
                size = Size(bodyWidth * 0.1f, bodyHeight * 0.78f),
                cornerRadius = CornerRadius(bodyRadius, bodyRadius),
            )

            val gripTop = bodyTop + bodyHeight * 0.58f
            repeat(5) { index ->
                val y = gripTop + index * bodyHeight * 0.062f
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.22f),
                    topLeft = Offset(bodyLeft + bodyWidth * 0.2f, y),
                    size = Size(bodyWidth * 0.6f, 2.2.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.08f),
                    topLeft = Offset(bodyLeft + bodyWidth * 0.22f, y + 2.8.dp.toPx()),
                    size = Size(bodyWidth * 0.56f, 1.dp.toPx()),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx()),
                )
            }

            val switchSlotTop = bodyTop + bodyHeight * 0.42f
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.38f),
                topLeft = Offset(bodyLeft + bodyWidth * 0.21f, switchSlotTop),
                size = Size(bodyWidth * 0.58f, bodyHeight * 0.13f),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
            )
            drawRoundRect(
                color = accent.copy(alpha = 0.18f + 0.16f * powerFraction),
                topLeft = Offset(bodyLeft + bodyWidth * 0.31f, switchSlotTop + bodyHeight * 0.095f),
                size = Size(bodyWidth * 0.38f, 3.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            )
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.32f + 0.12f * powerFraction),
                        if (state.isPowered) accent else LumenColors.Graphite,
                        Color.Black.copy(alpha = 0.32f),
                    ),
                ),
                topLeft = Offset(
                    x = bodyLeft + bodyWidth * (0.25f + 0.28f * powerFraction),
                    y = switchSlotTop + bodyHeight * 0.022f,
                ),
                size = Size(bodyWidth * 0.18f, bodyHeight * 0.084f),
                cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx()),
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LumenColors.BeamHot.copy(alpha = 0.72f + 0.2f * powerFraction),
                        accent.copy(alpha = 0.28f + 0.22f * powerFraction),
                        LumenColors.Graphite,
                        LumenColors.Void,
                    ),
                    center = lensCenter,
                    radius = lensRadius,
                ),
                radius = lensRadius,
                center = lensCenter,
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.45f),
                radius = lensRadius * 1.03f,
                center = lensCenter,
                style = Stroke(width = 4.dp.toPx()),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.16f + state.lighting.keyLightIntensity * 0.04f),
                radius = lensRadius * 0.86f,
                center = lensCenter,
                style = Stroke(width = 1.2.dp.toPx()),
            )
            drawCircle(
                color = LumenColors.Void.copy(alpha = 0.56f - 0.18f * powerFraction),
                radius = lensRadius * 0.48f,
                center = lensCenter,
                style = Stroke(width = 1.4.dp.toPx()),
            )

            repeat(6) { index ->
                val angle = (index * 60f).toRadians()
                val ledCenter = Offset(
                    x = lensCenter.x + cos(angle) * lensRadius * 0.48f,
                    y = lensCenter.y + sin(angle) * lensRadius * 0.48f,
                )
                drawCircle(
                    color = LumenColors.BeamHot.copy(alpha = 0.2f + 0.44f * powerFraction),
                    radius = lensRadius * 0.08f,
                    center = ledCenter,
                )
            }
            drawCircle(
                color = LumenColors.BeamHot.copy(alpha = 0.28f + 0.44f * powerFraction),
                radius = lensRadius * 0.11f,
                center = lensCenter,
            )

            val modeY = bodyTop + bodyHeight * 0.78f
            drawRoundRect(
                color = accent.copy(alpha = 0.34f + powerFraction * 0.3f),
                topLeft = Offset(centerX - bodyWidth * 0.24f, modeY),
                size = Size(bodyWidth * 0.48f, 4.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx()),
            )
            repeat(3) { index ->
                drawCircle(
                    color = if (state.mode.isHardwareImplemented || index == 0) {
                        accent.copy(alpha = 0.5f + 0.24f * powerFraction)
                    } else {
                        LumenColors.Muted.copy(alpha = 0.34f)
                    },
                    radius = 2.2.dp.toPx(),
                    center = Offset(
                        x = centerX - bodyWidth * 0.12f + index * bodyWidth * 0.12f,
                        y = modeY + 13.dp.toPx(),
                    ),
                )
            }
        }
    }
}

private val FlashlightMode.accentColor: Color
    get() = when (this) {
        FlashlightMode.Beam -> LumenColors.Beam
        FlashlightMode.Strobe -> LumenColors.Signal
        FlashlightMode.Sos -> LumenColors.BeamHot
        FlashlightMode.Beacon -> LumenColors.Cyan
    }

private fun Float.toRadians(): Float = (this * Math.PI / 180f).toFloat()
