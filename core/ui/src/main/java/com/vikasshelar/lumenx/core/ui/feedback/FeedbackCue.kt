package com.vikasshelar.lumenx.core.ui.feedback

enum class SoundCue {
    PowerOn,
    PowerOff,
    Selection,
    TimerArmed,
    Error,
}

enum class HapticCue {
    PowerOn,
    PowerOff,
    Selection,
    Error,
}

data class FeedbackCue(
    val sound: SoundCue,
    val haptic: HapticCue,
)
