package com.vikasshelar.lumenx.core.ui.feedback

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

interface SoundFeedbackPlayer {
    fun play(cue: SoundCue)
}

class AndroidSoundFeedbackPlayer : SoundFeedbackPlayer {
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, 34)

    override fun play(cue: SoundCue) {
        val tone = when (cue) {
            SoundCue.PowerOn -> ToneGenerator.TONE_PROP_ACK
            SoundCue.PowerOff -> ToneGenerator.TONE_PROP_BEEP
            SoundCue.Selection -> ToneGenerator.TONE_PROP_PROMPT
            SoundCue.TimerArmed -> ToneGenerator.TONE_PROP_ACK
            SoundCue.Error -> ToneGenerator.TONE_PROP_NACK
        }
        val durationMs = if (cue == SoundCue.Error) 120 else 70
        toneGenerator.startTone(tone, durationMs)
    }

    fun release() {
        toneGenerator.release()
    }
}

@Composable
fun rememberSoundFeedbackPlayer(): SoundFeedbackPlayer {
    val player = remember { AndroidSoundFeedbackPlayer() }
    DisposableEffect(player) {
        onDispose { player.release() }
    }
    return player
}
