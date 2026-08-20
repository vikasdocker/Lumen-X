package com.vikasshelar.lumenx.feature.settings

import com.vikasshelar.lumenx.core.model.FlashlightMode

data class UserSettings(
    val lastTorchEnabled: Boolean = false,
    val brightnessLevel: Int = 1,
    val selectedMode: FlashlightMode = FlashlightMode.Beam,
    val timerSeconds: Int = 0,
    val theme: AppTheme = AppTheme.Dark,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
)
