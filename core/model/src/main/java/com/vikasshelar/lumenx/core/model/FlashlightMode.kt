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
        description = "Safe configurable repeating flash interval.",
        hardwareLabel = "Timed hardware",
        isHardwareImplemented = true,
    ),
    Sos(
        label = "SOS",
        shortCode = "SOS",
        description = "Standard Morse SOS cadence with safe timing.",
        hardwareLabel = "Timed hardware",
        isHardwareImplemented = true,
    ),
    Beacon(
        label = "BEACON",
        shortCode = "BCN",
        description = "Low-duty locator pulse with configurable on/off timing.",
        hardwareLabel = "Timed hardware",
        isHardwareImplemented = true,
    ),
    Signal(
        label = "SIGNAL",
        shortCode = "SIG",
        description = "Programmable signal pattern hook with a safe default.",
        hardwareLabel = "Pattern ready",
        isHardwareImplemented = true,
    ),
    Morse(
        label = "MORSE",
        shortCode = "MRS",
        description = "Text-to-Morse timing engine.",
        hardwareLabel = "Text timing",
        isHardwareImplemented = true,
    ),
    Custom(
        label = "CUSTOM",
        shortCode = "CSTM",
        description = "User-created pattern architecture.",
        hardwareLabel = "User pattern",
        isHardwareImplemented = true,
    ),
}
