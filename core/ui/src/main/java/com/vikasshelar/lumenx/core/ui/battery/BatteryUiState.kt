package com.vikasshelar.lumenx.core.ui.battery

data class BatteryUiState(
    val levelPercent: Int = 0,
    val isCharging: Boolean = false,
)
