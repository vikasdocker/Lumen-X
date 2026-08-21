package com.vikasshelar.lumenx.feature.flashlight.engine

import com.vikasshelar.lumenx.core.hardware.FlashlightController
import com.vikasshelar.lumenx.core.hardware.TorchCommandResult
import com.vikasshelar.lumenx.core.model.FlashlightMode
import com.vikasshelar.lumenx.core.model.TorchError
import com.vikasshelar.lumenx.feature.modes.engine.FlashModeCommandResult
import com.vikasshelar.lumenx.feature.modes.engine.FlashModeConfig
import com.vikasshelar.lumenx.feature.modes.engine.FlashModeExecutionException
import com.vikasshelar.lumenx.feature.modes.engine.FlashModeRegistry
import com.vikasshelar.lumenx.feature.modes.engine.FlashModeSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FlashModeEngineState(
    val activeMode: FlashlightMode? = null,
) {
    val isActive: Boolean = activeMode != null
}

sealed interface FlashModeStartResult {
    data object Started : FlashModeStartResult
    data class Failed(val reason: String) : FlashModeStartResult
}

sealed interface FlashModeEngineEvent {
    data class Failed(
        val mode: FlashlightMode,
        val reason: String,
    ) : FlashModeEngineEvent
}

class FlashModeEngine(
    private val flashlightController: FlashlightController,
    private val scope: CoroutineScope,
    private val registry: FlashModeRegistry = FlashModeRegistry(),
    private val configProvider: () -> FlashModeConfig = { FlashModeConfig() },
) {
    private val mutableState = MutableStateFlow(FlashModeEngineState())
    val state: StateFlow<FlashModeEngineState> = mutableState.asStateFlow()

    private val mutableEvents = MutableSharedFlow<FlashModeEngineEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<FlashModeEngineEvent> = mutableEvents.asSharedFlow()

    private var activeJob: Job? = null

    suspend fun startMode(
        mode: FlashlightMode,
        brightnessLevel: Int,
        useHardwareBrightness: Boolean,
    ): FlashModeStartResult {
        stopActiveMode()

        val capability = flashlightController.getCapability()
        if (!capability.isAvailable) {
            return FlashModeStartResult.Failed("Flashlight hardware is unavailable.")
        }

        val flashMode = registry.modeFor(mode)
            ?: return FlashModeStartResult.Failed("Mode ${mode.label} is not registered.")
        val session = HardwareFlashModeSession(
            flashlightController = flashlightController,
            brightnessLevel = brightnessLevel.coerceIn(1, capability.safeMaxStrengthLevel),
            useHardwareBrightness = useHardwareBrightness && capability.supportsStrength,
        )

        mutableState.value = FlashModeEngineState(activeMode = mode)
        activeJob = scope.launch {
            val runningJob = coroutineContext[Job]
            val availabilityJob = launch {
                flashlightController.observeTorchState().collect { snapshot ->
                    val unavailable = !snapshot.capability.isAvailable ||
                        snapshot.error is TorchError.NoFlashUnit ||
                        snapshot.error is TorchError.CameraUnavailable
                    if (unavailable) {
                        runningJob?.cancel(CancellationException("Flashlight hardware became unavailable."))
                    }
                }
            }
            try {
                flashMode.start(session, configProvider())
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: FlashModeExecutionException) {
                mutableEvents.tryEmit(
                    FlashModeEngineEvent.Failed(
                        mode = mode,
                        reason = exception.message ?: "Mode execution failed.",
                    ),
                )
            } finally {
                withContext(NonCancellable) {
                    availabilityJob.cancelAndJoin()
                    flashMode.stop(session)
                }
                if (mutableState.value.activeMode == mode) {
                    mutableState.value = FlashModeEngineState()
                }
            }
        }

        return FlashModeStartResult.Started
    }

    suspend fun restartActiveMode(
        fallbackMode: FlashlightMode,
        brightnessLevel: Int,
        useHardwareBrightness: Boolean,
    ): FlashModeStartResult {
        val mode = mutableState.value.activeMode ?: fallbackMode
        return startMode(
            mode = mode,
            brightnessLevel = brightnessLevel,
            useHardwareBrightness = useHardwareBrightness,
        )
    }

    suspend fun stopActiveMode() {
        val job = activeJob
        activeJob = null
        job?.cancelAndJoin()
        mutableState.value = FlashModeEngineState()
    }

    fun cancelActiveMode() {
        activeJob?.cancel()
        activeJob = null
        mutableState.value = FlashModeEngineState()
    }

    private class HardwareFlashModeSession(
        private val flashlightController: FlashlightController,
        private val brightnessLevel: Int,
        private val useHardwareBrightness: Boolean,
    ) : FlashModeSession {
        override suspend fun setTorchEnabled(enabled: Boolean): FlashModeCommandResult {
            val result = if (enabled && useHardwareBrightness) {
                flashlightController.setBrightnessLevel(brightnessLevel)
            } else {
                flashlightController.setTorchEnabled(enabled)
            }
            return when (result) {
                TorchCommandResult.Success -> FlashModeCommandResult.Success
                is TorchCommandResult.Failure -> FlashModeCommandResult.Failure(result.error.userMessage)
            }
        }

        override suspend fun wait(durationMillis: Long) {
            delay(durationMillis)
        }
    }
}
