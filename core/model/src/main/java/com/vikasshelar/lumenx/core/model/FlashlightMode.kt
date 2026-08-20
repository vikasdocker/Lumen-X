package com.vikasshelar.lumenx.core.model

enum class FlashlightMode(
    val label: String,
    val shortCode: String,
    val description: String,
    val hardwareLabel: String,
    val isHardwareImplemented: Boolean,
) {
    Beam(
        label = "BEAM",
        shortCode = "BEAM",
        description = "Continuous real hardware torch.",
        hardwareLabel = "Live hardware",
        isHardwareImplemented = true,
    ),
    Strobe(
        label = "STROBE",
        shortCode = "STRB",
        description = "Visual preview for a future hardware-safe strobe pattern.",
        hardwareLabel = "Preview only",
        isHardwareImplemented = false,
    ),
    Sos(
        label = "SOS",
        shortCode = "SOS",
        description = "Visual preview for a future emergency SOS pattern.",
        hardwareLabel = "Preview only",
        isHardwareImplemented = false,
    ),
    Beacon(
        label = "BEACON",
        shortCode = "BCN",
        description = "Visual preview for a future long-interval signal pattern.",
        hardwareLabel = "Preview only",
        isHardwareImplemented = false,
    ),
}
