package com.vikasshelar.lumenx.feature.modes.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SosFlashModeTest {
    @Test
    fun sosPatternUsesStandardMorseTiming() {
        val pattern = SosFlashMode.pattern(
            FlashModeConfig(morseUnitMillis = 200L),
        )

        val actual = pattern.steps.map { it.enabled to it.durationMillis }

        assertTrue(pattern.repeats)
        assertEquals(
            listOf(
                true to 200L,
                false to 200L,
                true to 200L,
                false to 200L,
                true to 200L,
                false to 600L,
                true to 600L,
                false to 200L,
                true to 600L,
                false to 200L,
                true to 600L,
                false to 600L,
                true to 200L,
                false to 200L,
                true to 200L,
                false to 200L,
                true to 200L,
                false to 1_400L,
            ),
            actual,
        )
    }
}
