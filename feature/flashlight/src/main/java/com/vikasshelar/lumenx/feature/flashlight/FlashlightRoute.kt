package com.vikasshelar.lumenx.feature.flashlight

import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vikasshelar.lumenx.core.designsystem.GlassSurface
import com.vikasshelar.lumenx.core.designsystem.LumenBackground
import com.vikasshelar.lumenx.core.designsystem.LumenColors
import com.vikasshelar.lumenx.core.model.FlashlightMode
import com.vikasshelar.lumenx.core.ui.battery.BatteryUiState
import com.vikasshelar.lumenx.core.ui.battery.rememberBatteryState
import com.vikasshelar.lumenx.core.ui.feedback.FeedbackCue
import com.vikasshelar.lumenx.core.ui.feedback.HapticCue
import com.vikasshelar.lumenx.core.ui.feedback.rememberSoundFeedbackPlayer
import com.vikasshelar.lumenx.feature.flashlight.visual.FlashlightInteractionState
import com.vikasshelar.lumenx.feature.flashlight.visual.FlashlightModels
import com.vikasshelar.lumenx.feature.flashlight.visual.FlashlightVisualHost
import com.vikasshelar.lumenx.feature.flashlight.visual.FlashlightVisualState
import com.vikasshelar.lumenx.feature.modes.ModeSelector
import com.vikasshelar.lumenx.feature.timer.TimerControl

@Composable
fun FlashlightRoute(
    onSettingsClick: () -> Unit,
    viewModel: FlashlightViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val batteryState by rememberBatteryState()
    val latestState by rememberUpdatedState(state)
    val hapticFeedback = LocalHapticFeedback.current
    val soundFeedback = rememberSoundFeedbackPlayer()

    LaunchedEffect(viewModel) {
        viewModel.feedback.collect { cue ->
            if (latestState.soundEnabled) {
                soundFeedback.play(cue.sound)
            }
            if (latestState.hapticsEnabled) {
                hapticFeedback.performHapticFeedback(cue.toHapticType())
            }
        }
    }

    FlashlightScreen(
        state = state,
        batteryState = batteryState,
        onToggleTorch = viewModel::toggleTorch,
        onBrightnessChange = viewModel::setBrightnessLevel,
        onModeSelected = viewModel::selectMode,
        onTimerSelected = viewModel::selectTimer,
        onSettingsClick = onSettingsClick,
    )
}

@Composable
fun FlashlightScreen(
    state: FlashlightUiState,
    batteryState: BatteryUiState,
    onToggleTorch: () -> Unit,
    onBrightnessChange: (Int) -> Unit,
    onModeSelected: (FlashlightMode) -> Unit,
    onTimerSelected: (Int) -> Unit,
    onSettingsClick: () -> Unit,
) {
    val motionScale = rememberSystemAnimatorScale()
    val illumination = if (state.isTorchOn) state.brightnessFraction else 0f

    LumenBackground(
        isIlluminated = state.isTorchOn,
        illuminationStrength = illumination,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            val landscape = maxWidth > maxHeight
            val compactHeight = maxHeight < 760.dp
            val compactControlsMaxHeight = maxHeight * 0.48f

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                FlashlightTopBar(
                    batteryState = batteryState,
                    hardwareStatus = state.hardwareStatus,
                    selectedMode = state.selectedMode,
                    onSettingsClick = onSettingsClick,
                )

                if (landscape) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DeviceStage(
                            state = state,
                            motionScale = motionScale,
                            onToggleTorch = onToggleTorch,
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                        )
                        ControlsCluster(
                            state = state,
                            onBrightnessChange = onBrightnessChange,
                            onModeSelected = onModeSelected,
                            onTimerSelected = onTimerSelected,
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(min = 330.dp, max = 430.dp)
                                .verticalScroll(rememberScrollState()),
                        )
                    }
                } else {
                    DeviceStage(
                        state = state,
                        motionScale = motionScale,
                        onToggleTorch = onToggleTorch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = if (compactHeight) 250.dp else 320.dp)
                            .weight(1f),
                    )
                    ControlsCluster(
                        state = state,
                        onBrightnessChange = onBrightnessChange,
                        onModeSelected = onModeSelected,
                        onTimerSelected = onTimerSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (compactHeight) {
                                    Modifier
                                        .heightIn(max = compactControlsMaxHeight)
                                        .verticalScroll(rememberScrollState())
                                } else {
                                    Modifier
                                },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlsCluster(
    state: FlashlightUiState,
    onBrightnessChange: (Int) -> Unit,
    onModeSelected: (FlashlightMode) -> Unit,
    onTimerSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RadialIntensityControl(
            state = state,
            onBrightnessChange = onBrightnessChange,
            modifier = Modifier.fillMaxWidth(),
        )
        ModeSelector(
            selectedMode = state.selectedMode,
            onModeSelected = onModeSelected,
            enabled = state.isTorchAvailable,
            modifier = Modifier.fillMaxWidth(),
        )
        TimerControl(
            countdownState = state.countdownState,
            onTimerSelected = onTimerSelected,
            enabled = state.isTorchAvailable,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun FlashlightTopBar(
    batteryState: BatteryUiState,
    hardwareStatus: FlashlightHardwareStatus,
    selectedMode: FlashlightMode,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "LUMEN X",
                style = MaterialTheme.typography.headlineSmall,
                color = LumenColors.Mist,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                text = "${hardwareStatus.label} / ${selectedMode.hardwareLabel}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedMode.isHardwareImplemented) hardwareStatus.color else LumenColors.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BatteryIndicator(batteryState)
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(LumenColors.Glass),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = LumenColors.Mist,
                )
            }
        }
    }
}

@Composable
private fun BatteryIndicator(
    batteryState: BatteryUiState,
    modifier: Modifier = Modifier,
) {
    GlassSurface(modifier = modifier.height(42.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Canvas(
                modifier = Modifier
                    .width(24.dp)
                    .height(12.dp),
            ) {
                drawRoundRect(
                    color = LumenColors.GlassStroke,
                    size = Size(size.width - 3.dp.toPx(), size.height),
                    cornerRadius = CornerRadius(3.dp.toPx()),
                    style = Stroke(width = 1.4.dp.toPx()),
                )
                drawRoundRect(
                    color = if (batteryState.isCharging) LumenColors.Cyan else LumenColors.Beam,
                    topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                    size = Size(
                        width = ((size.width - 7.dp.toPx()) * (batteryState.levelPercent / 100f))
                            .coerceAtLeast(2.dp.toPx()),
                        height = size.height - 4.dp.toPx(),
                    ),
                    cornerRadius = CornerRadius(2.dp.toPx()),
                )
                drawRoundRect(
                    color = LumenColors.GlassStroke,
                    topLeft = Offset(size.width - 2.dp.toPx(), size.height * 0.32f),
                    size = Size(2.dp.toPx(), size.height * 0.36f),
                    cornerRadius = CornerRadius(1.dp.toPx()),
                )
            }
            Text(
                text = "${batteryState.levelPercent}%",
                style = MaterialTheme.typography.labelLarge,
                color = LumenColors.Steel,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

@Composable
private fun DeviceStage(
    state: FlashlightUiState,
    motionScale: Float,
    onToggleTorch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var isTouching by remember { mutableStateOf(false) }

    val powerFraction by animateFloatAsState(
        targetValue = if (state.isTorchOn) 1f else 0f,
        animationSpec = tween(
            durationMillis = scaledDuration(460, motionScale),
            easing = FastOutSlowInEasing,
        ),
        label = "powerFraction",
    )
    val brightnessFraction by animateFloatAsState(
        targetValue = state.brightnessFraction,
        animationSpec = tween(
            durationMillis = scaledDuration(360, motionScale),
            easing = FastOutSlowInEasing,
        ),
        label = "brightnessFraction",
    )
    val touchScale by animateFloatAsState(
        targetValue = if (isTouching) 0.985f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "deviceTouchScale",
    )
    val animatedDragX by animateFloatAsState(
        targetValue = dragX,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "deviceDragX",
    )
    val animatedDragY by animateFloatAsState(
        targetValue = dragY,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "deviceDragY",
    )
    val visualInteraction = FlashlightInteractionState(
        isTouching = isTouching,
        pressFraction = if (isTouching) 1f else 0f,
        dragX = animatedDragX,
        dragY = animatedDragY,
        rotationXDegrees = -animatedDragY,
        rotationYDegrees = animatedDragX,
        parallaxX = animatedDragX / 9f,
        parallaxY = animatedDragY / 7f,
    )
    val visualState = FlashlightVisualState.from(
        isPowered = state.isTorchOn,
        powerFraction = powerFraction,
        brightnessFraction = brightnessFraction,
        mode = state.selectedMode,
        interaction = visualInteraction,
    )

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isTouching = true
                    },
                    onDragEnd = {
                        isTouching = false
                        dragX = 0f
                        dragY = 0f
                    },
                    onDragCancel = {
                        isTouching = false
                        dragX = 0f
                        dragY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        dragX = (dragX + dragAmount.x / 18f).coerceIn(-9f, 9f)
                        dragY = (dragY + dragAmount.y / 18f).coerceIn(-7f, 7f)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .aspectRatio(0.72f)
                .graphicsLayer {
                    rotationY = animatedDragX
                    rotationX = -animatedDragY
                    scaleX = touchScale
                    scaleY = touchScale
                },
            contentAlignment = Alignment.Center,
        ) {
            FlashlightVisualHost(
                model = FlashlightModels.ProceduralPlaceholder,
                state = visualState,
                modifier = Modifier.fillMaxSize(),
            )

            PowerActuator(
                enabled = state.isTorchAvailable,
                isOn = state.isTorchOn,
                onClick = onToggleTorch,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 88.dp),
            )
        }

        state.errorMessage?.let { message ->
            GlassSurface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LumenColors.Signal,
                )
            }
        }
    }
}

@Composable
private fun PowerActuator(
    enabled: Boolean,
    isOn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "pressScale",
    )
    val knobOffset by animateFloatAsState(
        targetValue = if (isOn) 42f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "knobOffset",
    )
    val strokeColor by animateColorAsState(
        targetValue = if (isOn) LumenColors.Beam else LumenColors.GlassStroke,
        label = "powerStroke",
    )

    Box(
        modifier = modifier
            .testTag("power_button")
            .size(width = 112.dp, height = 58.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .border(BorderStroke(1.dp, strokeColor), RoundedCornerShape(8.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        LumenColors.Glass,
                        LumenColors.Ink.copy(alpha = 0.94f),
                    ),
                ),
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(7.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobOffset.dp)
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.verticalGradient(
                        colors = if (isOn) {
                            listOf(LumenColors.BeamHot, LumenColors.Beam)
                        } else {
                            listOf(LumenColors.Graphite, LumenColors.Carbon)
                        },
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.PowerSettingsNew,
                contentDescription = if (isOn) "Turn flashlight off" else "Turn flashlight on",
                tint = if (isOn) LumenColors.Void else LumenColors.Steel,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

private fun FeedbackCue.toHapticType(): HapticFeedbackType {
    return when (haptic) {
        HapticCue.PowerOn,
        HapticCue.PowerOff,
        HapticCue.Error,
        -> HapticFeedbackType.LongPress
        HapticCue.Selection -> HapticFeedbackType.TextHandleMove
    }
}

@Composable
private fun rememberSystemAnimatorScale(): Float {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            )
        }.getOrDefault(1f)
    }
}

private fun scaledDuration(
    baseDurationMillis: Int,
    motionScale: Float,
): Int = if (motionScale == 0f) 0 else (baseDurationMillis * motionScale).toInt().coerceAtLeast(80)

private val FlashlightHardwareStatus.label: String
    get() = when (this) {
        FlashlightHardwareStatus.Loading -> "Scanning optics"
        FlashlightHardwareStatus.Available -> "Hardware ready"
        FlashlightHardwareStatus.Unavailable -> "No flash unit"
        FlashlightHardwareStatus.Error -> "Hardware attention"
    }

private val FlashlightHardwareStatus.color: Color
    get() = when (this) {
        FlashlightHardwareStatus.Loading -> LumenColors.Cyan
        FlashlightHardwareStatus.Available -> LumenColors.Beam
        FlashlightHardwareStatus.Unavailable -> LumenColors.Muted
        FlashlightHardwareStatus.Error -> LumenColors.Signal
    }
