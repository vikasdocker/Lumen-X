package com.vikasshelar.lumenx.feature.modes.engine

data class FlashModePattern(
    val steps: List<FlashPatternStep>,
    val repeats: Boolean,
) {
    fun sanitized(): FlashModePattern {
        return copy(
            steps = steps
                .take(FlashModeSafetyLimits.MaxCustomPatternSteps)
                .map { it.sanitized() },
        )
    }

    companion object {
        fun continuousOn(): FlashModePattern = FlashModePattern(
            steps = listOf(FlashPatternStep.on(FlashModeSafetyLimits.MaxPulseMillis)),
            repeats = true,
        )

        fun empty(): FlashModePattern = FlashModePattern(
            steps = emptyList(),
            repeats = false,
        )
    }
}

data class FlashPatternStep(
    val enabled: Boolean,
    val durationMillis: Long,
) {
    fun sanitized(): FlashPatternStep = copy(
        durationMillis = durationMillis.coerceIn(
            FlashModeSafetyLimits.MinPulseMillis,
            FlashModeSafetyLimits.MaxPulseMillis,
        ),
    )

    companion object {
        fun on(durationMillis: Long): FlashPatternStep = FlashPatternStep(
            enabled = true,
            durationMillis = durationMillis.coerceIn(
                FlashModeSafetyLimits.MinPulseMillis,
                FlashModeSafetyLimits.MaxPulseMillis,
            ),
        )

        fun off(durationMillis: Long): FlashPatternStep = FlashPatternStep(
            enabled = false,
            durationMillis = durationMillis.coerceIn(
                FlashModeSafetyLimits.MinPulseMillis,
                FlashModeSafetyLimits.MaxPulseMillis,
            ),
        )

        internal fun exact(
            enabled: Boolean,
            durationMillis: Long,
        ): FlashPatternStep = FlashPatternStep(
            enabled = enabled,
            durationMillis = durationMillis,
        )
    }
}
