package com.vikasshelar.lumenx.feature.flashlight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vikasshelar.lumenx.core.hardware.FlashlightController
import com.vikasshelar.lumenx.core.hardware.TorchCommandResult
import com.vikasshelar.lumenx.core.model.FlashlightMode
import com.vikasshelar.lumenx.core.model.TorchError
import com.vikasshelar.lumenx.core.model.TorchSnapshot
import com.vikasshelar.lumenx.core.ui.feedback.FeedbackCue
import com.vikasshelar.lumenx.core.ui.feedback.HapticCue
import com.vikasshelar.lumenx.core.ui.feedback.SoundCue
import com.vikasshelar.lumenx.feature.flashlight.engine.FlashModeEngine
import com.vikasshelar.lumenx.feature.flashlight.engine.FlashModeEngineState
import com.vikasshelar.lumenx.feature.flashlight.engine.FlashModeStartResult
import com.vikasshelar.lumenx.feature.settings.SettingsRepository
import com.vikasshelar.lumenx.feature.settings.UserSettings
import com.vikasshelar.lumenx.feature.timer.CountdownState
import com.vikasshelar.lumenx.feature.timer.TimerStateMachine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FlashlightViewModel @Inject constructor(
    private val flashlightController: FlashlightController,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val timerStateMachine = TimerStateMachine()
    private val countdownState = MutableStateFlow(CountdownState())
    private val modeEngine = FlashModeEngine(
        flashlightController = flashlightController,
        scope = viewModelScope,
    )
    private var timerJob: Job? = null

    private val mutableFeedback = MutableSharedFlow<FeedbackCue>(extraBufferCapacity = 8)
    val feedback = mutableFeedback.asSharedFlow()

    val uiState: StateFlow<FlashlightUiState> = combine(
        flashlightController.observeTorchState(),
        settingsRepository.settings,
        countdownState,
        modeEngine.state,
    ) { torchSnapshot, settings, timer, modeState ->
        torchSnapshot.toUiState(settings, timer, modeState)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FlashlightUiState(),
    )

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            val capability = flashlightController.getCapability()
            if (settings.timerSeconds > 0) {
                startTimer(settings.timerSeconds)
            }
            if (settings.lastTorchEnabled && capability.isAvailable) {
                val result = modeEngine.startMode(
                    mode = settings.selectedMode,
                    brightnessLevel = settings.brightnessLevel,
                    useHardwareBrightness = capability.supportsStrength,
                )
                if (result is FlashModeStartResult.Failed) {
                    settingsRepository.setLastTorchEnabled(false)
                }
            }
        }
    }

    fun toggleTorch() {
        viewModelScope.launch {
            val state = uiState.value
            if (!state.isTorchAvailable) {
                emitFeedback(SoundCue.Error, HapticCue.Error)
                return@launch
            }

            val enableTorch = !state.isPowerEngaged
            val result = if (enableTorch) {
                modeEngine.startMode(
                    mode = state.selectedMode,
                    brightnessLevel = state.brightnessLevel,
                    useHardwareBrightness = state.brightnessSupported,
                )
            } else {
                modeEngine.stopActiveMode()
                flashlightController.setTorchEnabled(false)
            }

            if (result is TorchCommandResult.Success || result is FlashModeStartResult.Started) {
                settingsRepository.setLastTorchEnabled(enableTorch)
                emitFeedback(
                    sound = if (enableTorch) SoundCue.PowerOn else SoundCue.PowerOff,
                    haptic = if (enableTorch) HapticCue.PowerOn else HapticCue.PowerOff,
                )
            } else {
                emitFeedback(SoundCue.Error, HapticCue.Error)
            }
        }
    }

    fun setBrightnessLevel(level: Int) {
        viewModelScope.launch {
            val state = uiState.value
            val coercedLevel = level.coerceIn(1, state.maxBrightnessLevel.coerceAtLeast(1))
            if (!state.brightnessSupported) {
                emitFeedback(SoundCue.Error, HapticCue.Error)
                return@launch
            }

            if (state.isModeActive) {
                val result = modeEngine.restartActiveMode(
                    fallbackMode = state.selectedMode,
                    brightnessLevel = coercedLevel,
                    useHardwareBrightness = true,
                )
                if (result is FlashModeStartResult.Failed) {
                    emitFeedback(SoundCue.Error, HapticCue.Error)
                    return@launch
                }
            } else if (state.isTorchOn) {
                val result = flashlightController.setBrightnessLevel(coercedLevel)
                if (result is TorchCommandResult.Failure) {
                    emitFeedback(SoundCue.Error, HapticCue.Error)
                    return@launch
                }
            }

            settingsRepository.setBrightnessLevel(coercedLevel)
            emitFeedback(SoundCue.Selection, HapticCue.Selection)
        }
    }

    fun selectMode(mode: FlashlightMode) {
        viewModelScope.launch {
            val state = uiState.value
            settingsRepository.setSelectedMode(mode)
            if (state.isPowerEngaged) {
                val result = modeEngine.startMode(
                    mode = mode,
                    brightnessLevel = state.brightnessLevel,
                    useHardwareBrightness = state.brightnessSupported,
                )
                if (result is FlashModeStartResult.Failed) {
                    settingsRepository.setLastTorchEnabled(false)
                    emitFeedback(SoundCue.Error, HapticCue.Error)
                    return@launch
                }
                settingsRepository.setLastTorchEnabled(true)
            }
            emitFeedback(SoundCue.Selection, HapticCue.Selection)
        }
    }

    fun selectTimer(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.setTimerSeconds(seconds)
            if (seconds <= 0) {
                timerJob?.cancel()
                countdownState.value = timerStateMachine.cancel()
                emitFeedback(SoundCue.Selection, HapticCue.Selection)
            } else {
                startTimer(seconds)
                emitFeedback(SoundCue.TimerArmed, HapticCue.Selection)
            }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        modeEngine.cancelActiveMode()
        super.onCleared()
    }

    fun onAppBackgrounded() {
        viewModelScope.launch {
            modeEngine.stopActiveMode()
            flashlightController.setTorchEnabled(false)
            settingsRepository.setLastTorchEnabled(false)
            settingsRepository.setTimerSeconds(0)
            timerJob?.cancel()
            countdownState.value = timerStateMachine.cancel()
        }
    }

    private fun startTimer(seconds: Int) {
        timerJob?.cancel()
        countdownState.value = timerStateMachine.arm(seconds)
        timerJob = viewModelScope.launch {
            while (countdownState.value.isRunning) {
                delay(1_000)
                countdownState.value = timerStateMachine.tick(countdownState.value)
            }

            if (countdownState.value.durationSeconds > 0 && countdownState.value.remainingSeconds == 0) {
                modeEngine.stopActiveMode()
                flashlightController.setTorchEnabled(false)
                settingsRepository.setLastTorchEnabled(false)
                settingsRepository.setTimerSeconds(0)
                countdownState.value = timerStateMachine.cancel()
                emitFeedback(SoundCue.PowerOff, HapticCue.PowerOff)
            }
        }
    }

    private fun TorchSnapshot.toUiState(
        settings: UserSettings,
        timer: CountdownState,
        modeState: FlashModeEngineState,
    ): FlashlightUiState {
        val available = capability.isAvailable
        val maxLevel = capability.safeMaxStrengthLevel
        val brightness = if (isEnabled && capability.supportsStrength) {
            strengthLevel
        } else {
            settings.brightnessLevel
        }.coerceIn(1, maxLevel)

        return FlashlightUiState(
            hardwareStatus = when {
                error is TorchError.NoFlashUnit || !available -> FlashlightHardwareStatus.Unavailable
                error != null -> FlashlightHardwareStatus.Error
                else -> FlashlightHardwareStatus.Available
            },
            isTorchAvailable = available,
            isTorchOn = isEnabled,
            brightnessSupported = capability.supportsStrength,
            brightnessLevel = brightness,
            maxBrightnessLevel = maxLevel,
            selectedMode = settings.selectedMode,
            activeMode = modeState.activeMode,
            countdownState = timer,
            errorMessage = error?.userMessage,
            hapticsEnabled = settings.hapticsEnabled,
            soundEnabled = settings.soundEnabled,
        )
    }

    private fun emitFeedback(
        sound: SoundCue,
        haptic: HapticCue,
    ) {
        mutableFeedback.tryEmit(FeedbackCue(sound = sound, haptic = haptic))
    }
}
