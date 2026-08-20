package com.vikasshelar.lumenx.feature.timer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerStateMachineTest {
    private val machine = TimerStateMachine()

    @Test
    fun armStartsCountdownWithRequestedDuration() {
        val state = machine.arm(60)

        assertEquals(60, state.durationSeconds)
        assertEquals(60, state.remainingSeconds)
        assertTrue(state.isRunning)
    }

    @Test
    fun tickStopsAtZero() {
        val armed = machine.arm(1)

        val finished = machine.tick(armed)

        assertEquals(0, finished.remainingSeconds)
        assertFalse(finished.isRunning)
    }

    @Test
    fun cancelClearsTimerState() {
        val cancelled = machine.cancel()

        assertEquals(0, cancelled.durationSeconds)
        assertEquals(0, cancelled.remainingSeconds)
        assertFalse(cancelled.isRunning)
    }
}
