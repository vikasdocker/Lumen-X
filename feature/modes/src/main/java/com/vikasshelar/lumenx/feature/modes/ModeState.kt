package com.vikasshelar.lumenx.feature.modes

import com.vikasshelar.lumenx.core.model.FlashlightMode

data class ModeState(
    val selectedMode: FlashlightMode = FlashlightMode.Beam,
    val availableModes: List<FlashlightMode> = FlashlightMode.entries,
)

class ModeStateReducer {
    fun selectMode(
        state: ModeState,
        mode: FlashlightMode,
    ): ModeState {
        return if (mode in state.availableModes) {
            state.copy(selectedMode = mode)
        } else {
            state
        }
    }
}
