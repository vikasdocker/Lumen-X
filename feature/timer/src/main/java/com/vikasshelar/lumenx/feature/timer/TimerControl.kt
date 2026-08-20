package com.vikasshelar.lumenx.feature.timer

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vikasshelar.lumenx.core.designsystem.LumenColors

@Composable
fun TimerControl(
    countdownState: CountdownState,
    onTimerSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Timer",
                style = MaterialTheme.typography.labelLarge,
                color = LumenColors.Steel,
            )
            Text(
                text = countdownState.remainingLabel(),
                style = MaterialTheme.typography.labelLarge,
                color = if (countdownState.isRunning) LumenColors.Beam else LumenColors.Muted,
            )
        }

        if (countdownState.isArmed) {
            LinearProgressIndicator(
                progress = { countdownState.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = LumenColors.Beam,
                trackColor = LumenColors.Graphite,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TimerOption.entries.forEach { option ->
                TimerChip(
                    option = option,
                    selected = countdownState.durationSeconds == option.seconds,
                    enabled = enabled,
                    onClick = { onTimerSelected(option.seconds) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TimerChip(
    option: TimerOption,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strokeColor by animateColorAsState(
        targetValue = if (selected) LumenColors.Cyan else LumenColors.GlassStroke,
        label = "timerStroke",
    )

    Text(
        text = option.label,
        modifier = modifier
            .testTag("timer_${option.seconds}")
            .height(46.dp)
            .border(BorderStroke(1.dp, strokeColor), RoundedCornerShape(8.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = if (selected) {
                        listOf(LumenColors.Cyan.copy(alpha = 0.14f), LumenColors.Graphite.copy(alpha = 0.84f))
                    } else {
                        listOf(LumenColors.Glass, LumenColors.Carbon.copy(alpha = 0.68f))
                    },
                ),
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(top = 13.dp),
        color = if (selected) LumenColors.Cyan else LumenColors.Steel,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelLarge,
    )
}

private fun CountdownState.remainingLabel(): String {
    if (!isArmed) return "Not armed"
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
