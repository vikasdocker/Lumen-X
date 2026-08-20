package com.vikasshelar.lumenx.feature.modes

import com.vikasshelar.lumenx.core.model.FlashlightMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ModeStateReducerTest {
    private val reducer = ModeStateReducer()

    @Test
    fun selectModeUpdatesWhenModeIsAvailable() {
        val state = ModeState(selectedMode = FlashlightMode.Beam)

        val updated = reducer.selectMode(state, FlashlightMode.Strobe)

        assertEquals(FlashlightMode.Strobe, updated.selectedMode)
    }

    @Test
    fun selectModeIgnoresUnavailableMode() {
        val state = ModeState(
            selectedMode = FlashlightMode.Beam,
            availableModes = listOf(FlashlightMode.Beam),
        )

        val updated = reducer.selectMode(state, FlashlightMode.Sos)

        assertEquals(FlashlightMode.Beam, updated.selectedMode)
    }
}
