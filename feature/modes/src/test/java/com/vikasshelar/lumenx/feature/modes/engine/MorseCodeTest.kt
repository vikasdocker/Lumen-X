package com.vikasshelar.lumenx.feature.modes.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MorseCodeTest {
    @Test
    fun encodeConvertsTextIntoMorseTokens() {
        assertEquals(
            listOf(".-", "/", "-..."),
            MorseCode.encode("A B"),
        )
    }

    @Test
    fun patternForTextUsesDotDashAndGapTiming() {
        val pattern = MorseCode.patternFor(
            text = "A",
            unitMillis = 200L,
            repeats = false,
            includeCycleGap = false,
        )

        assertFalse(pattern.repeats)
        assertEquals(
            listOf(
                true to 200L,
                false to 200L,
                true to 600L,
            ),
            pattern.steps.map { it.enabled to it.durationMillis },
        )
    }

    @Test
    fun morseUnitIsClampedToSafeMinimum() {
        val pattern = MorseCode.patternFor(
            text = "E",
            unitMillis = 40L,
            repeats = false,
            includeCycleGap = false,
        )

        assertEquals(
            listOf(true to FlashModeSafetyLimits.MinMorseUnitMillis),
            pattern.steps.map { it.enabled to it.durationMillis },
        )
    }
}
