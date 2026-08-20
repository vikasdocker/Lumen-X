package com.vikasshelar.lumenx.core.model

data class TorchCapability(
    val isAvailable: Boolean,
    val supportsStrength: Boolean,
    val maxStrengthLevel: Int = 1,
    val defaultStrengthLevel: Int = 1,
    val cameraId: String? = null,
) {
    val safeMaxStrengthLevel: Int = maxStrengthLevel.coerceAtLeast(1)
    val safeDefaultStrengthLevel: Int = defaultStrengthLevel.coerceIn(1, safeMaxStrengthLevel)
}
