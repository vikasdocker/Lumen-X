package com.vikasshelar.lumenx.feature.flashlight

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.vikasshelar.lumenx.core.designsystem.LumenXTheme
import com.vikasshelar.lumenx.core.model.FlashlightMode
import com.vikasshelar.lumenx.core.ui.battery.BatteryUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FlashlightScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun powerButtonCallsToggle() {
        var toggled = false
        setContent(onToggleTorch = { toggled = true })

        composeRule.onNodeWithTag("power_button").performClick()

        assertTrue(toggled)
    }

    @Test
    fun unsupportedIntensityControlIsDisabled() {
        setContent(
            state = defaultState.copy(brightnessSupported = false),
        )

        composeRule.onNodeWithTag("intensity_slider").assertIsNotEnabled()
    }

    @Test
    fun modeSelectorInvokesSelection() {
        var selectedMode: FlashlightMode? = null
        setContent(onModeSelected = { selectedMode = it })

        composeRule.onNodeWithTag("mode_${FlashlightMode.Strobe.name}").performClick()

        assertEquals(FlashlightMode.Strobe, selectedMode)
    }

    @Test
    fun timerControlInvokesSelection() {
        var selectedSeconds = -1
        setContent(onTimerSelected = { selectedSeconds = it })

        composeRule.onNodeWithTag("timer_300").performClick()

        assertEquals(300, selectedSeconds)
    }

    private fun setContent(
        state: FlashlightUiState = defaultState,
        onToggleTorch: () -> Unit = {},
        onBrightnessChange: (Int) -> Unit = {},
        onModeSelected: (FlashlightMode) -> Unit = {},
        onTimerSelected: (Int) -> Unit = {},
    ) {
        composeRule.setContent {
            LumenXTheme {
                FlashlightScreen(
                    state = state,
                    batteryState = BatteryUiState(levelPercent = 82),
                    onToggleTorch = onToggleTorch,
                    onBrightnessChange = onBrightnessChange,
                    onModeSelected = onModeSelected,
                    onTimerSelected = onTimerSelected,
                    onSettingsClick = {},
                )
            }
        }
    }

    private val defaultState = FlashlightUiState(
        hardwareStatus = FlashlightHardwareStatus.Available,
        isTorchAvailable = true,
        isTorchOn = false,
        brightnessSupported = true,
        brightnessLevel = 3,
        maxBrightnessLevel = 5,
    )
}
