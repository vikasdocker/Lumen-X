package com.vikasshelar.lumenx.core.model

sealed interface TorchError {
    val userMessage: String

    data object NoFlashUnit : TorchError {
        override val userMessage: String = "This device does not expose a hardware flash unit."
    }

    data object UnsupportedStrength : TorchError {
        override val userMessage: String =
            "This device does not expose LED intensity levels. Power control still uses the real torch."
    }

    data object SecurityDenied : TorchError {
        override val userMessage: String = "Android denied flashlight hardware access."
    }

    data class CameraUnavailable(
        val reason: String,
    ) : TorchError {
        override val userMessage: String = "The flashlight is unavailable: $reason"
    }

    data class SystemError(
        override val userMessage: String,
    ) : TorchError
}
