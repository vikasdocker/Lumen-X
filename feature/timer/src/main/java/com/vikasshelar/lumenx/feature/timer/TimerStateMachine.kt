package com.vikasshelar.lumenx.feature.timer

class TimerStateMachine {
    fun arm(seconds: Int): CountdownState {
        val safeSeconds = seconds.coerceAtLeast(0)
        return CountdownState(
            durationSeconds = safeSeconds,
            remainingSeconds = safeSeconds,
            isRunning = safeSeconds > 0,
        )
    }

    fun tick(state: CountdownState): CountdownState {
        if (!state.isRunning || state.remainingSeconds <= 0) {
            return state.copy(isRunning = false, remainingSeconds = 0)
        }

        val nextRemaining = (state.remainingSeconds - 1).coerceAtLeast(0)
        return state.copy(
            remainingSeconds = nextRemaining,
            isRunning = nextRemaining > 0,
        )
    }

    fun cancel(): CountdownState = CountdownState()
}
