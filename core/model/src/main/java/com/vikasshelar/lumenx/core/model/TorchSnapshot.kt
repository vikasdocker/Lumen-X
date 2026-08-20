package com.vikasshelar.lumenx.core.model

data class TorchSnapshot(
    val capability: TorchCapability = TorchCapability(isAvailable = false, supportsStrength = false),
    val isEnabled: Boolean = false,
    val strengthLevel: Int = capability.safeDefaultStrengthLevel,
    val error: TorchError? = null,
) {
    val isUsable: Boolean = capability.isAvailable && error !is TorchError.NoFlashUnit
}
