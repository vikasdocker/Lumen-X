package com.vikasshelar.lumenx.feature.timer

data class CountdownState(
    val durationSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val isRunning: Boolean = false,
) {
    val isArmed: Boolean = durationSeconds > 0
    val progress: Float = if (durationSeconds <= 0) {
        0f
    } else {
        remainingSeconds / durationSeconds.toFloat()
    }.coerceIn(0f, 1f)
}
