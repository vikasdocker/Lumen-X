package com.vikasshelar.lumenx.feature.flashlight.visual

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface FlashlightVisual {
    @Composable
    fun Render(
        model: FlashlightModel,
        state: FlashlightVisualState,
        modifier: Modifier,
    )
}

@Composable
fun FlashlightVisualHost(
    state: FlashlightVisualState,
    modifier: Modifier = Modifier,
    model: FlashlightModel = FlashlightModels.ProceduralPlaceholder,
    visual: FlashlightVisual = ProceduralFlashlightVisual,
) {
    visual.Render(
        model = model,
        state = state,
        modifier = modifier,
    )
}
