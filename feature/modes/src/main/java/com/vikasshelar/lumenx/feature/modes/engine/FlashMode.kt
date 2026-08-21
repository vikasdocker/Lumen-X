package com.vikasshelar.lumenx.feature.modes.engine

import com.vikasshelar.lumenx.core.model.FlashlightMode

interface FlashMode {
    val mode: FlashlightMode
    val id: String
    val name: String
    val icon: String
    val description: String
    val requiresTiming: Boolean

    fun pattern(config: FlashModeConfig = FlashModeConfig()): FlashModePattern

    suspend fun start(
        session: FlashModeSession,
        config: FlashModeConfig = FlashModeConfig(),
    )

    suspend fun stop(session: FlashModeSession) {
        session.setTorchEnabled(false)
    }
}

interface FlashModeSession {
    suspend fun setTorchEnabled(enabled: Boolean): FlashModeCommandResult
    suspend fun wait(durationMillis: Long)
}

sealed interface FlashModeCommandResult {
    data object Success : FlashModeCommandResult
    data class Failure(val reason: String) : FlashModeCommandResult
}

class FlashModeExecutionException(message: String) : RuntimeException(message)

suspend fun FlashModePattern.runOn(session: FlashModeSession) {
    if (steps.isEmpty()) {
        session.setTorchEnabled(false).requireSuccess()
        return
    }

    do {
        steps.forEach { step ->
            session.setTorchEnabled(step.enabled).requireSuccess()
            session.wait(step.durationMillis)
        }
    } while (repeats)

    session.setTorchEnabled(false).requireSuccess()
}

internal fun FlashModeCommandResult.requireSuccess() {
    if (this is FlashModeCommandResult.Failure) {
        throw FlashModeExecutionException(reason)
    }
}
