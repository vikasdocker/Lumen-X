package com.vikasshelar.lumenx.feature.flashlight

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.vikasshelar.lumenx.core.designsystem.GlassSurface
import com.vikasshelar.lumenx.core.designsystem.LumenColors
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private const val ArcStartDegrees = 140f
private const val ArcSweepDegrees = 260f

@Composable
fun RadialIntensityControl(
    state: FlashlightUiState,
    onBrightnessChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = state.isTorchAvailable && state.brightnessSupported
    val activeFraction = if (enabled) state.intensityPercent / 100f else 0f
    val animatedFraction by animateFloatAsState(
        targetValue = activeFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "radialIntensityFraction",
    )
    val activeColor by animateColorAsState(
        targetValue = if (enabled) LumenColors.Beam else LumenColors.Muted,
        animationSpec = tween(durationMillis = 220),
        label = "radialIntensityColor",
    )

    GlassSurface(modifier = modifier) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            val dialSize = if (maxWidth < 360.dp) 164.dp else 188.dp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(dialSize)
                        .testTag("intensity_slider")
                        .semantics {
                            contentDescription = "Intensity control"
                            stateDescription = if (enabled) {
                                "${state.intensityPercent}% intensity"
                            } else {
                                "LED intensity unavailable"
                            }
                            progressBarRangeInfo = ProgressBarRangeInfo(
                                current = state.intensityPercent.toFloat(),
                                range = 0f..100f,
                                steps = state.maxBrightnessLevel.coerceAtLeast(1),
                            )
                            if (!enabled) disabled()
                        }
                        .clip(CircleShape)
                        .pointerInput(enabled, state.maxBrightnessLevel) {
                            if (!enabled) return@pointerInput
                            detectDragGestures(
                                onDragStart = { position ->
                                    onBrightnessChange(position.toBrightnessLevel(size, state.maxBrightnessLevel))
                                },
                                onDrag = { change, _ ->
                                    onBrightnessChange(change.position.toBrightnessLevel(size, state.maxBrightnessLevel))
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.size(dialSize)) {
                        val strokeWidth = 13.dp.toPx()
                        val inset = 19.dp.toPx()
                        val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
                        val radius = arcSize.width / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    activeColor.copy(alpha = 0.16f * animatedFraction),
                                    LumenColors.Glass.copy(alpha = 0.18f),
                                    LumenColors.Void.copy(alpha = 0.18f),
                                ),
                                center = center,
                                radius = radius,
                            ),
                            radius = radius + 10.dp.toPx(),
                            center = center,
                        )

                        drawArc(
                            color = LumenColors.GlassStroke.copy(alpha = 0.58f),
                            startAngle = ArcStartDegrees,
                            sweepAngle = ArcSweepDegrees,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    LumenColors.Cyan.copy(alpha = 0.64f),
                                    LumenColors.Beam,
                                    LumenColors.BeamHot,
                                    LumenColors.Cyan.copy(alpha = 0.64f),
                                ),
                                center = center,
                            ),
                            startAngle = ArcStartDegrees,
                            sweepAngle = ArcSweepDegrees * animatedFraction,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )

                        repeat(13) { index ->
                            val tickFraction = index / 12f
                            val angle = (ArcStartDegrees + ArcSweepDegrees * tickFraction).toRadians()
                            val outer = Offset(
                                x = center.x + cos(angle) * (radius + 2.dp.toPx()),
                                y = center.y + sin(angle) * (radius + 2.dp.toPx()),
                            )
                            val inner = Offset(
                                x = center.x + cos(angle) * (radius - 9.dp.toPx()),
                                y = center.y + sin(angle) * (radius - 9.dp.toPx()),
                            )
                            drawLine(
                                color = if (tickFraction <= animatedFraction) activeColor else LumenColors.GlassStroke,
                                start = inner,
                                end = outer,
                                strokeWidth = if (index % 6 == 0) 1.8.dp.toPx() else 1.dp.toPx(),
                                cap = StrokeCap.Round,
                            )
                        }

                        val knobAngle = (ArcStartDegrees + ArcSweepDegrees * animatedFraction).toRadians()
                        val knobCenter = Offset(
                            x = center.x + cos(knobAngle) * radius,
                            y = center.y + sin(knobAngle) * radius,
                        )
                        drawCircle(
                            color = activeColor.copy(alpha = if (enabled) 0.28f else 0.1f),
                            radius = 14.dp.toPx(),
                            center = knobCenter,
                        )
                        drawCircle(
                            color = activeColor,
                            radius = 6.dp.toPx(),
                            center = knobCenter,
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = if (enabled) "${state.intensityPercent}%" else "--",
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (enabled) LumenColors.Mist else LumenColors.Muted,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "INTENSITY",
                            style = MaterialTheme.typography.labelLarge,
                            color = LumenColors.Muted,
                            maxLines = 1,
                        )
                    }

                    Text(
                        text = "MEDIUM",
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 7.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = LumenColors.Muted,
                    )
                    Text(
                        text = "LOW",
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = 12.dp, y = (-18).dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = LumenColors.Muted,
                    )
                    Text(
                        text = "HIGH",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-10).dp, y = (-18).dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = LumenColors.Muted,
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Physical LED Output",
                        style = MaterialTheme.typography.labelLarge,
                        color = LumenColors.Steel,
                    )
                    Text(
                        text = state.intensityLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (enabled) LumenColors.Beam else LumenColors.Muted,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (enabled) {
                            "Drag the arc to request a real torch strength level from Android."
                        } else {
                            "This control is disabled because Android does not expose LED strength levels here."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = LumenColors.Muted,
                        textAlign = TextAlign.Start,
                    )
                }
            }
        }
    }
}

private fun Offset.toBrightnessLevel(
    canvasSize: IntSize,
    maxBrightnessLevel: Int,
): Int {
    val maxLevel = maxBrightnessLevel.coerceAtLeast(1)
    if (maxLevel == 1) return 1

    val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
    val angle = atan2(y - center.y, x - center.x).toDegrees().normalizeDegrees()
    val fromStart = if (angle >= ArcStartDegrees) {
        angle - ArcStartDegrees
    } else {
        angle + 360f - ArcStartDegrees
    }.coerceIn(0f, ArcSweepDegrees)
    val fraction = fromStart / ArcSweepDegrees
    return (1 + fraction * (maxLevel - 1)).roundToInt().coerceIn(1, maxLevel)
}

private fun Float.toRadians(): Float = (this * PI / 180f).toFloat()

private fun Float.normalizeDegrees(): Float {
    val normalized = this % 360f
    return if (normalized < 0f) normalized + 360f else normalized
}

private fun Float.toDegrees(): Float = (this * 180f / PI).toFloat()
