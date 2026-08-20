package com.vikasshelar.lumenx.feature.settings

import com.vikasshelar.lumenx.core.model.FlashlightMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<UserSettings>

    suspend fun setLastTorchEnabled(enabled: Boolean)
    suspend fun setBrightnessLevel(level: Int)
    suspend fun setSelectedMode(mode: FlashlightMode)
    suspend fun setTimerSeconds(seconds: Int)
    suspend fun setTheme(theme: AppTheme)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setSoundEnabled(enabled: Boolean)
}
