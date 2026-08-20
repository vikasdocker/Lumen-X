package com.vikasshelar.lumenx.feature.flashlight

import com.vikasshelar.lumenx.core.hardware.FlashlightController
import com.vikasshelar.lumenx.core.hardware.TorchCommandResult
import com.vikasshelar.lumenx.core.model.FlashlightMode
import com.vikasshelar.lumenx.core.model.TorchCapability
import com.vikasshelar.lumenx.core.model.TorchError
import com.vikasshelar.lumenx.core.model.TorchSnapshot
import com.vikasshelar.lumenx.feature.settings.AppTheme
import com.vikasshelar.lumenx.feature.settings.SettingsRepository
import com.vikasshelar.lumenx.feature.settings.UserSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlashlightViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun toggleTorchTurnsHardwareOnAndPersistsState() = runTest {
        val controller = FakeFlashlightController(
            TorchSnapshot(
                capability = TorchCapability(
                    isAvailable = true,
                    supportsStrength = true,
                    maxStrengthLevel = 5,
                    defaultStrengthLevel = 1,
                    cameraId = "0",
                ),
            ),
        )
        val settings = FakeSettingsRepository(UserSettings(brightnessLevel = 4))
        val viewModel = FlashlightViewModel(controller, settings)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.toggleTorch()
        advanceUntilIdle()

        assertEquals(listOf(4), controller.brightnessCalls)
        assertTrue(settings.current.lastTorchEnabled)
        assertTrue(viewModel.uiState.value.isTorchOn)
    }

    @Test
    fun unsupportedBrightnessDoesNotCallHardwareOrFakeIntensity() = runTest {
        val controller = FakeFlashlightController(
            TorchSnapshot(
                capability = TorchCapability(
                    isAvailable = true,
                    supportsStrength = false,
                    maxStrengthLevel = 1,
                    cameraId = "0",
                ),
            ),
        )
        val settings = FakeSettingsRepository()
        val viewModel = FlashlightViewModel(controller, settings)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.setBrightnessLevel(5)
        advanceUntilIdle()

        assertTrue(controller.brightnessCalls.isEmpty())
        assertEquals(1, settings.current.brightnessLevel)
        assertFalse(viewModel.uiState.value.brightnessSupported)
    }

    @Test
    fun unavailableTorchMapsToErrorState() = runTest {
        val controller = FakeFlashlightController(
            TorchSnapshot(
                capability = TorchCapability(isAvailable = false, supportsStrength = false),
                error = TorchError.NoFlashUnit,
            ),
        )
        val viewModel = FlashlightViewModel(controller, FakeSettingsRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(FlashlightHardwareStatus.Unavailable, viewModel.uiState.value.hardwareStatus)
        assertEquals("This device does not expose a hardware flash unit.", viewModel.uiState.value.errorMessage)
    }

    private class FakeFlashlightController(
        initialSnapshot: TorchSnapshot,
    ) : FlashlightController {
        private val state = MutableStateFlow(initialSnapshot)
        val brightnessCalls = mutableListOf<Int>()
        val enabledCalls = mutableListOf<Boolean>()

        override suspend fun getCapability(): TorchCapability = state.value.capability

        override suspend fun isTorchAvailable(): Boolean = state.value.capability.isAvailable

        override suspend fun isTorchEnabled(): Boolean = state.value.isEnabled

        override suspend fun setTorchEnabled(enabled: Boolean): TorchCommandResult {
            enabledCalls += enabled
            if (!state.value.capability.isAvailable) {
                return TorchCommandResult.Failure(TorchError.NoFlashUnit)
            }
            state.value = state.value.copy(isEnabled = enabled, error = null)
            return TorchCommandResult.Success
        }

        override suspend fun supportsBrightness(): Boolean = state.value.capability.supportsStrength

        override suspend fun getMaxBrightnessLevel(): Int = state.value.capability.safeMaxStrengthLevel

        override suspend fun setBrightnessLevel(level: Int): TorchCommandResult {
            if (!state.value.capability.supportsStrength) {
                return TorchCommandResult.Failure(TorchError.UnsupportedStrength)
            }
            brightnessCalls += level
            state.value = state.value.copy(isEnabled = true, strengthLevel = level, error = null)
            return TorchCommandResult.Success
        }

        override fun observeTorchState(): Flow<TorchSnapshot> = state
    }

    private class FakeSettingsRepository(
        initialSettings: UserSettings = UserSettings(),
    ) : SettingsRepository {
        private val state = MutableStateFlow(initialSettings)
        val current: UserSettings get() = state.value
        override val settings: Flow<UserSettings> = state

        override suspend fun setLastTorchEnabled(enabled: Boolean) {
            state.value = state.value.copy(lastTorchEnabled = enabled)
        }

        override suspend fun setBrightnessLevel(level: Int) {
            state.value = state.value.copy(brightnessLevel = level)
        }

        override suspend fun setSelectedMode(mode: FlashlightMode) {
            state.value = state.value.copy(selectedMode = mode)
        }

        override suspend fun setTimerSeconds(seconds: Int) {
            state.value = state.value.copy(timerSeconds = seconds)
        }

        override suspend fun setTheme(theme: AppTheme) {
            state.value = state.value.copy(theme = theme)
        }

        override suspend fun setHapticsEnabled(enabled: Boolean) {
            state.value = state.value.copy(hapticsEnabled = enabled)
        }

        override suspend fun setSoundEnabled(enabled: Boolean) {
            state.value = state.value.copy(soundEnabled = enabled)
        }
    }
}
