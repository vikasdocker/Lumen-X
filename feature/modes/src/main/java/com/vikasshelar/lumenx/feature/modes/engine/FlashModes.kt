package com.vikasshelar.lumenx.feature.modes.engine

import com.vikasshelar.lumenx.core.model.FlashlightMode

object BeamFlashMode : FlashMode {
    override val mode: FlashlightMode = FlashlightMode.Beam
    override val id: String = "beam"
    override val name: String = "BEAM"
    override val icon: String = "beam"
    override val description: String = "Continuous hardware flashlight."
    override val requiresTiming: Boolean = false

    override fun pattern(config: FlashModeConfig): FlashModePattern = FlashModePattern.continuousOn()

    override suspend fun start(
        session: FlashModeSession,
        config: FlashModeConfig,
    ) {
        session.setTorchEnabled(true).requireSuccess()
        while (true) {
            session.wait(FlashModeSafetyLimits.MaxPulseMillis)
        }
    }
}

object StrobeFlashMode : FlashMode {
    override val mode: FlashlightMode = FlashlightMode.Strobe
    override val id: String = "strobe"
    override val name: String = "STROBE"
    override val icon: String = "strobe"
    override val description: String = "Safe repeating flash interval."
    override val requiresTiming: Boolean = true

    override fun pattern(config: FlashModeConfig): FlashModePattern {
        val interval = config.strobeIntervalMillis.coerceIn(
            FlashModeSafetyLimits.MinStrobeIntervalMillis,
            FlashModeSafetyLimits.MaxStrobeIntervalMillis,
        )
        return FlashModePattern(
            steps = listOf(
                FlashPatternStep.exact(enabled = true, durationMillis = interval),
                FlashPatternStep.exact(enabled = false, durationMillis = interval),
            ),
            repeats = true,
        )
    }

    override suspend fun start(
        session: FlashModeSession,
        config: FlashModeConfig,
    ) {
        pattern(config).runOn(session)
    }
}

object SosFlashMode : FlashMode {
    override val mode: FlashlightMode = FlashlightMode.Sos
    override val id: String = "sos"
    override val name: String = "SOS"
    override val icon: String = "sos"
    override val description: String = "Standard Morse SOS distress cadence."
    override val requiresTiming: Boolean = true

    override fun pattern(config: FlashModeConfig): FlashModePattern {
        return MorseCode.patternFor(
            text = "SOS",
            unitMillis = config.morseUnitMillis,
            repeats = true,
            includeCycleGap = true,
        )
    }

    override suspend fun start(
        session: FlashModeSession,
        config: FlashModeConfig,
    ) {
        pattern(config).runOn(session)
    }
}

object BeaconFlashMode : FlashMode {
    override val mode: FlashlightMode = FlashlightMode.Beacon
    override val id: String = "beacon"
    override val name: String = "BEACON"
    override val icon: String = "beacon"
    override val description: String = "Low-duty locator pulse."
    override val requiresTiming: Boolean = true

    override fun pattern(config: FlashModeConfig): FlashModePattern {
        val onMillis = config.beaconOnMillis.coerceIn(
            FlashModeSafetyLimits.MinBeaconOnMillis,
            FlashModeSafetyLimits.MaxBeaconOnMillis,
        )
        val offMillis = config.beaconOffMillis.coerceIn(
            FlashModeSafetyLimits.MinBeaconOffMillis,
            FlashModeSafetyLimits.MaxBeaconOffMillis,
        )
        return FlashModePattern(
            steps = listOf(
                FlashPatternStep.exact(enabled = true, durationMillis = onMillis),
                FlashPatternStep.exact(enabled = false, durationMillis = offMillis),
            ),
            repeats = true,
        )
    }

    override suspend fun start(
        session: FlashModeSession,
        config: FlashModeConfig,
    ) {
        pattern(config).runOn(session)
    }
}

object SignalFlashMode : FlashMode {
    override val mode: FlashlightMode = FlashlightMode.Signal
    override val id: String = "signal"
    override val name: String = "SIGNAL"
    override val icon: String = "signal"
    override val description: String = "Programmable pattern hook with a safe default signal."
    override val requiresTiming: Boolean = true

    override fun pattern(config: FlashModeConfig): FlashModePattern {
        return config.signalPattern?.sanitized() ?: FlashModePattern(
            steps = listOf(
                FlashPatternStep.on(300L),
                FlashPatternStep.off(300L),
                FlashPatternStep.on(300L),
                FlashPatternStep.off(2_400L),
            ),
            repeats = true,
        )
    }

    override suspend fun start(
        session: FlashModeSession,
        config: FlashModeConfig,
    ) {
        pattern(config).runOn(session)
    }
}

object MorseFlashMode : FlashMode {
    override val mode: FlashlightMode = FlashlightMode.Morse
    override val id: String = "morse"
    override val name: String = "MORSE"
    override val icon: String = "morse"
    override val description: String = "Text-to-Morse hardware flash timing."
    override val requiresTiming: Boolean = true

    override fun pattern(config: FlashModeConfig): FlashModePattern {
        return MorseCode.patternFor(
            text = config.morseText.ifBlank { "SOS" },
            unitMillis = config.morseUnitMillis,
            repeats = true,
            includeCycleGap = true,
        )
    }

    override suspend fun start(
        session: FlashModeSession,
        config: FlashModeConfig,
    ) {
        pattern(config).runOn(session)
    }
}

object CustomFlashMode : FlashMode {
    override val mode: FlashlightMode = FlashlightMode.Custom
    override val id: String = "custom"
    override val name: String = "CUSTOM"
    override val icon: String = "custom"
    override val description: String = "User-created pattern execution point."
    override val requiresTiming: Boolean = true

    override fun pattern(config: FlashModeConfig): FlashModePattern {
        return config.customPattern?.sanitized() ?: FlashModePattern.empty()
    }

    override suspend fun start(
        session: FlashModeSession,
        config: FlashModeConfig,
    ) {
        pattern(config).runOn(session)
    }
}

class FlashModeRegistry(
    modes: List<FlashMode> = defaultModes,
) {
    private val modesById = modes.associateBy { it.mode }

    val availableModes: List<FlashMode> = modes

    fun modeFor(mode: FlashlightMode): FlashMode? = modesById[mode]

    companion object {
        val defaultModes: List<FlashMode> = listOf(
            BeamFlashMode,
            StrobeFlashMode,
            SosFlashMode,
            BeaconFlashMode,
            SignalFlashMode,
            MorseFlashMode,
            CustomFlashMode,
        )
    }
}
