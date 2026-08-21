package com.vikasshelar.lumenx.feature.modes.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimedFlashModePatternTest {
    @Test
    fun strobeUsesSafeDefaultInterval() {
        val pattern = StrobeFlashMode.pattern()

        assertTrue(pattern.repeats)
        assertEquals(
            listOf(
                true to FlashModeSafetyLimits.DefaultStrobeIntervalMillis,
                false to FlashModeSafetyLimits.DefaultStrobeIntervalMillis,
            ),
            pattern.steps.map { it.enabled to it.durationMillis },
        )
    }

    @Test
    fun strobeIntervalIsClampedToSafeLimit() {
        val pattern = StrobeFlashMode.pattern(
            FlashModeConfig(strobeIntervalMillis = 40L),
        )

        assertEquals(
            listOf(
                true to FlashModeSafetyLimits.MinStrobeIntervalMillis,
                false to FlashModeSafetyLimits.MinStrobeIntervalMillis,
            ),
            pattern.steps.map { it.enabled to it.durationMillis },
        )
    }

    @Test
    fun beaconOnOffIntervalsAreClampedIndependently() {
        val pattern = BeaconFlashMode.pattern(
            FlashModeConfig(
                beaconOnMillis = 40L,
                beaconOffMillis = 80L,
            ),
        )

        assertTrue(pattern.repeats)
        assertEquals(
            listOf(
                true to FlashModeSafetyLimits.MinBeaconOnMillis,
                false to FlashModeSafetyLimits.MinBeaconOffMillis,
            ),
            pattern.steps.map { it.enabled to it.durationMillis },
        )
    }
}
