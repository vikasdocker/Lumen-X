package com.vikasshelar.lumenx.feature.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vikasshelar.lumenx.core.model.FlashlightMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.lumenSettingsDataStore by preferencesDataStore(name = "lumen_x_settings")

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : SettingsRepository {
    override val settings: Flow<UserSettings> = context.lumenSettingsDataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { preferences ->
            UserSettings(
                lastTorchEnabled = preferences[Keys.LastTorchEnabled] ?: false,
                brightnessLevel = preferences[Keys.BrightnessLevel] ?: 1,
                selectedMode = preferences[Keys.SelectedMode].toModeOrDefault(),
                timerSeconds = preferences[Keys.TimerSeconds] ?: 0,
                theme = preferences[Keys.Theme].toThemeOrDefault(),
                hapticsEnabled = preferences[Keys.HapticsEnabled] ?: true,
                soundEnabled = preferences[Keys.SoundEnabled] ?: true,
            )
        }

    override suspend fun setLastTorchEnabled(enabled: Boolean) {
        context.lumenSettingsDataStore.edit { it[Keys.LastTorchEnabled] = enabled }
    }

    override suspend fun setBrightnessLevel(level: Int) {
        context.lumenSettingsDataStore.edit { it[Keys.BrightnessLevel] = level.coerceAtLeast(1) }
    }

    override suspend fun setSelectedMode(mode: FlashlightMode) {
        context.lumenSettingsDataStore.edit { it[Keys.SelectedMode] = mode.name }
    }

    override suspend fun setTimerSeconds(seconds: Int) {
        context.lumenSettingsDataStore.edit { it[Keys.TimerSeconds] = seconds.coerceAtLeast(0) }
    }

    override suspend fun setTheme(theme: AppTheme) {
        context.lumenSettingsDataStore.edit { it[Keys.Theme] = theme.name }
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        context.lumenSettingsDataStore.edit { it[Keys.HapticsEnabled] = enabled }
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        context.lumenSettingsDataStore.edit { it[Keys.SoundEnabled] = enabled }
    }

    private fun String?.toModeOrDefault(): FlashlightMode {
        return runCatching { FlashlightMode.valueOf(this ?: "") }.getOrDefault(FlashlightMode.Beam)
    }

    private fun String?.toThemeOrDefault(): AppTheme {
        return runCatching { AppTheme.valueOf(this ?: "") }.getOrDefault(AppTheme.Dark)
    }

    private object Keys {
        val LastTorchEnabled = booleanPreferencesKey("last_torch_enabled")
        val BrightnessLevel = intPreferencesKey("brightness_level")
        val SelectedMode = stringPreferencesKey("selected_mode")
        val TimerSeconds = intPreferencesKey("timer_seconds")
        val Theme = stringPreferencesKey("theme")
        val HapticsEnabled = booleanPreferencesKey("haptics_enabled")
        val SoundEnabled = booleanPreferencesKey("sound_enabled")
    }
}
