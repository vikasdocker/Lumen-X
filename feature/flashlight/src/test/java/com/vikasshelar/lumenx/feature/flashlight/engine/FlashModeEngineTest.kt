package com.vikasshelar.lumenx.feature.flashlight.engine

import com.vikasshelar.lumenx.core.hardware.FlashlightController
import com.vikasshelar.lumenx.core.hardware.TorchCommandResult
import com.vikasshelar.lumenx.core.model.FlashlightMode
import com.vikasshelar.lumenx.core.model.TorchCapability
import com.vikasshelar.lumenx.core.model.TorchError
import com.vikasshelar.lumenx.core.model.TorchSnapshot
import com.vikasshelar.lumenx.feature.modes.engine.FlashModeConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlashModeEngineTest {
    @Test
    fun startingAnotherModeStopsThePreviousMode() = runTest {
        val controller = FakeFlashlightController()
        val engine = FlashModeEngine(
            flashlightController = controller,
            scope = backgroundScope,
            configProvider = {
                FlashModeConfig(
                    strobeIntervalMillis = 250L,
                    beaconOnMillis = 250L,
                    beaconOffMillis = 1_200L,
                )
            },
        )

        val strobeResult = engine.startMode(
            mode = FlashlightMode.Strobe,
            brightnessLevel = 1,
            useHardwareBrightness = false,
        )
        runCurrent()

        assertEquals(FlashModeStartResult.Started, strobeResult)
        assertEquals(FlashlightMode.Strobe, engine.state.value.activeMode)
        assertTrue(controller.enabledCalls.contains(true))

        val beaconResult = engine.startMode(
            mode = FlashlightMode.Beacon,
            brightnessLevel = 1,
            useHardwareBrightness = false,
        )
        runCurrent()

        assertEquals(FlashModeStartResult.Started, beaconResult)
        assertEquals(FlashlightMode.Beacon, engine.state.value.activeMode)
        assertEquals(listOf(true, false, true), controller.enabledCalls.takeLast(3))

        engine.stopActiveMode()
        runCurrent()

        assertEquals(null, engine.state.value.activeMode)
        assertEquals(false, controller.enabledCalls.last())
    }

    private class FakeFlashlightController : FlashlightController {
        private val snapshot = MutableStateFlow(
            TorchSnapshot(
                capability = TorchCapability(
                    isAvailable = true,
                    supportsStrength = false,
                    cameraId = "0",
                ),
            ),
        )
        val enabledCalls = mutableListOf<Boolean>()

        override suspend fun getCapability(): TorchCapability = snapshot.value.capability

        override suspend fun isTorchAvailable(): Boolean = snapshot.value.capability.isAvailable

        override suspend fun isTorchEnabled(): Boolean = snapshot.value.isEnabled

        override suspend fun setTorchEnabled(enabled: Boolean): TorchCommandResult {
            enabledCalls += enabled
            snapshot.value = snapshot.value.copy(isEnabled = enabled)
            return TorchCommandResult.Success
        }

        override suspend fun supportsBrightness(): Boolean = false

        override suspend fun getMaxBrightnessLevel(): Int = 1

        override suspend fun setBrightnessLevel(level: Int): TorchCommandResult {
            return TorchCommandResult.Failure(TorchError.UnsupportedStrength)
        }

        override fun observeTorchState(): Flow<TorchSnapshot> = snapshot
    }
}
