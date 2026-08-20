package com.vikasshelar.lumenx.feature.flashlight

import com.vikasshelar.lumenx.core.model.FlashlightMode
import com.vikasshelar.lumenx.feature.timer.CountdownState

data class FlashlightUiState(
    val hardwareStatus: FlashlightHardwareStatus = FlashlightHardwareStatus.Loading,
    val isTorchAvailable: Boolean = false,
    val isTorchOn: Boolean = false,
    val brightnessSupported: Boolean = false,
    val brightnessLevel: Int = 1,
    val maxBrightnessLevel: Int = 1,
    val selectedMode: FlashlightMode = FlashlightMode.Beam,
    val countdownState: CountdownState = CountdownState(),
    val errorMessage: String? = null,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
) {
    val brightnessFraction: Float =
        if (maxBrightnessLevel <= 1) 1f else brightnessLevel / maxBrightnessLevel.toFloat()

    val intensityLabel: String = when {
        !isTorchAvailable -> "No hardware torch"
        brightnessSupported -> "LED level $brightnessLevel of $maxBrightnessLevel"
        else -> "LED intensity not exposed by this device"
    }

    val intensityPercent: Int =
        if (!brightnessSupported || maxBrightnessLevel <= 1) {
            100
        } else {
            (((brightnessLevel - 1).coerceAtLeast(0) / (maxBrightnessLevel - 1).toFloat()) * 100f)
                .toInt()
                .coerceIn(0, 100)
        }
}

enum class FlashlightHardwareStatus {
    Loading,
    Available,
    Unavailable,
    Error,
}
