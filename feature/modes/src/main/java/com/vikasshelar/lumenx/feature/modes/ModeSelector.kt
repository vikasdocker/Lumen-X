package com.vikasshelar.lumenx.feature.modes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vikasshelar.lumenx.core.designsystem.LumenColors
import com.vikasshelar.lumenx.core.model.FlashlightMode

@Composable
fun ModeSelector(
    selectedMode: FlashlightMode,
    onModeSelected: (FlashlightMode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Mode",
            style = MaterialTheme.typography.labelLarge,
            color = LumenColors.Steel,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FlashlightMode.entries.forEach { mode ->
                ModeChip(
                    mode = mode,
                    selected = selectedMode == mode,
                    enabled = enabled,
                    onClick = { onModeSelected(mode) },
                )
            }
        }
    }
}

@Composable
private fun ModeChip(
    mode: FlashlightMode,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "modeScale",
    )
    val strokeColor by animateColorAsState(
        targetValue = if (selected) mode.accentColor else LumenColors.GlassStroke,
        label = "modeStroke",
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) LumenColors.Mist else LumenColors.Steel,
        label = "modeText",
    )

    Column(
        modifier = Modifier
            .testTag("mode_${mode.name}")
            .size(width = 116.dp, height = 82.dp)
            .scale(scale)
            .border(BorderStroke(1.dp, strokeColor), RoundedCornerShape(8.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = if (selected) {
                        listOf(mode.accentColor.copy(alpha = 0.2f), LumenColors.Graphite.copy(alpha = 0.94f))
                    } else {
                        listOf(LumenColors.Glass, LumenColors.Carbon.copy(alpha = 0.78f))
                    },
                ),
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ModeGlyph(
            mode = mode,
            active = selected,
            modifier = Modifier
                .width(42.dp)
                .height(18.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = mode.label,
                style = MaterialTheme.typography.labelLarge,
                color = textColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = mode.hardwareLabel.uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (mode.isHardwareImplemented && selected) mode.accentColor else LumenColors.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ModeGlyph(
    mode: FlashlightMode,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = if (active) mode.accentColor else LumenColors.Muted
    Canvas(modifier = modifier) {
        val stroke = 1.6.dp.toPx()
        when (mode) {
            FlashlightMode.Beam -> {
                drawLine(color, Offset(0f, size.height / 2f), Offset(size.width, size.height / 2f), strokeWidth = stroke)
                drawLine(color.copy(alpha = 0.62f), Offset(size.width * 0.15f, 0f), Offset(size.width, size.height / 2f), strokeWidth = stroke)
                drawLine(color.copy(alpha = 0.62f), Offset(size.width * 0.15f, size.height), Offset(size.width, size.height / 2f), strokeWidth = stroke)
            }
            FlashlightMode.Strobe -> {
                repeat(4) { index ->
                    val left = index * size.width / 4f
                    drawRect(
                        color = color.copy(alpha = if (index % 2 == 0) 1f else 0.38f),
                        topLeft = Offset(left, size.height * 0.22f),
                        size = Size(size.width / 6f, size.height * 0.56f),
                    )
                }
            }
            FlashlightMode.Sos -> {
                val dotRadius = size.height * 0.13f
                listOf(0.12f, 0.24f, 0.36f, 0.72f, 0.84f, 0.96f).forEach { x ->
                    drawCircle(color, dotRadius, Offset(size.width * x, size.height / 2f))
                }
                drawLine(color, Offset(size.width * 0.46f, size.height / 2f), Offset(size.width * 0.62f, size.height / 2f), strokeWidth = stroke)
            }
            FlashlightMode.Beacon -> {
                drawCircle(color.copy(alpha = 0.26f), size.height * 0.45f, Offset(size.width * 0.5f, size.height / 2f), style = Stroke(stroke))
                drawCircle(color, size.height * 0.18f, Offset(size.width * 0.5f, size.height / 2f))
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
