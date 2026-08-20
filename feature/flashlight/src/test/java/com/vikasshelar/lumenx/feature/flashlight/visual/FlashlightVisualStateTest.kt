package com.vikasshelar.lumenx.feature.flashlight.visual

import com.vikasshelar.lumenx.core.model.FlashlightMode
import org.junit.Assert.assertEquals
import org.junit.Test

class FlashlightVisualStateTest {
    @Test
    fun fromClampsRendererInputs() {
        val state = FlashlightVisualState.from(
            isPowered = true,
            powerFraction = 2f,
            brightnessFraction = -1f,
            mode = FlashlightMode.Beam,
            interaction = FlashlightInteractionState(
                isTouching = true,
                rotationXDegrees = -7f,
                rotationYDegrees = 9f,
                parallaxX = 0.4f,
                parallaxY = -0.2f,
            ),
        )

        assertEquals(1f, state.powerFraction)
        assertEquals(0f, state.brightnessFraction)
        assertEquals(0.24f, state.glow.lensGlow, 0.0001f)
        assertEquals(9f, state.camera.orbitYawDegrees)
        assertEquals(-7f, state.camera.orbitPitchDegrees)
        assertEquals(-0.04f, state.camera.distanceOffset)
    }
}
