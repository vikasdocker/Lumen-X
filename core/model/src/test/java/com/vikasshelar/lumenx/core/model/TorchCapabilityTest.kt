package com.vikasshelar.lumenx.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TorchCapabilityTest {
    @Test
    fun strengthCapabilityExposesSafeBounds() {
        val capability = TorchCapability(
            isAvailable = true,
            supportsStrength = true,
            maxStrengthLevel = 8,
            defaultStrengthLevel = 3,
            cameraId = "0",
        )

        assertTrue(capability.supportsStrength)
        assertEquals(8, capability.safeMaxStrengthLevel)
        assertEquals(3, capability.safeDefaultStrengthLevel)
    }

    @Test
    fun invalidStrengthValuesAreClampedForUiSafety() {
        val capability = TorchCapability(
            isAvailable = true,
            supportsStrength = true,
            maxStrengthLevel = 0,
            defaultStrengthLevel = 99,
        )

        assertEquals(1, capability.safeMaxStrengthLevel)
        assertEquals(1, capability.safeDefaultStrengthLevel)
    }

    @Test
    fun unavailableSnapshotIsNotUsable() {
        val snapshot = TorchSnapshot(
            capability = TorchCapability(isAvailable = false, supportsStrength = false),
            error = TorchError.NoFlashUnit,
        )

        assertFalse(snapshot.isUsable)
    }
}
