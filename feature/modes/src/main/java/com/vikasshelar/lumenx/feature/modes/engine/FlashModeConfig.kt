package com.vikasshelar.lumenx.feature.modes.engine

data class FlashModeConfig(
    val strobeIntervalMillis: Long = FlashModeSafetyLimits.DefaultStrobeIntervalMillis,
    val beaconOnMillis: Long = FlashModeSafetyLimits.DefaultBeaconOnMillis,
    val beaconOffMillis: Long = FlashModeSafetyLimits.DefaultBeaconOffMillis,
    val morseText: String = "SOS",
    val morseUnitMillis: Long = FlashModeSafetyLimits.DefaultMorseUnitMillis,
    val signalPattern: FlashModePattern? = null,
    val customPattern: FlashModePattern? = null,
)

object FlashModeSafetyLimits {
    const val MinPulseMillis: Long = 150L
    const val MaxPulseMillis: Long = 10_000L

    const val MinStrobeIntervalMillis: Long = 250L
    const val MaxStrobeIntervalMillis: Long = 3_000L
    const val DefaultStrobeIntervalMillis: Long = 500L

    const val MinBeaconOnMillis: Long = 200L
    const val MaxBeaconOnMillis: Long = 5_000L
    const val MinBeaconOffMillis: Long = 1_000L
    const val MaxBeaconOffMillis: Long = 30_000L
    const val DefaultBeaconOnMillis: Long = 450L
    const val DefaultBeaconOffMillis: Long = 4_000L

    const val MinMorseUnitMillis: Long = 150L
    const val MaxMorseUnitMillis: Long = 800L
    const val DefaultMorseUnitMillis: Long = 200L

    const val MaxMorseCharacters: Int = 48
    const val MaxCustomPatternSteps: Int = 64
}
