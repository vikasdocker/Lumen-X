package com.vikasshelar.lumenx.feature.flashlight.visual

import com.vikasshelar.lumenx.core.model.FlashlightMode

data class FlashlightVisualState(
    val isPowered: Boolean,
    val powerFraction: Float,
    val hardwarePowerFraction: Float = powerFraction,
    val brightnessFraction: Float,
    val mode: FlashlightMode,
    val interaction: FlashlightInteractionState = FlashlightInteractionState(),
    val lighting: FlashlightLightingState = FlashlightLightingState(),
    val glow: FlashlightGlowState = FlashlightGlowState(),
    val camera: FlashlightCameraState = FlashlightCameraState(),
    val environment: FlashlightEnvironmentState = FlashlightEnvironmentState(),
) {
    companion object {
        fun from(
            isPowered: Boolean,
            powerFraction: Float,
            hardwarePowerFraction: Float = powerFraction,
            brightnessFraction: Float,
            mode: FlashlightMode,
            interaction: FlashlightInteractionState = FlashlightInteractionState(),
        ): FlashlightVisualState {
            val normalizedPower = powerFraction.unit()
            val normalizedHardwarePower = hardwarePowerFraction.unit()
            val normalizedBrightness = brightnessFraction.unit()
            val liveGlow = normalizedHardwarePower * normalizedBrightness.coerceAtLeast(0.24f)

            return FlashlightVisualState(
                isPowered = isPowered,
                powerFraction = normalizedPower,
                hardwarePowerFraction = normalizedHardwarePower,
                brightnessFraction = normalizedBrightness,
                mode = mode,
                interaction = interaction,
                lighting = FlashlightLightingState(
                    keyLightIntensity = 0.42f + liveGlow * 0.5f,
                    fillLightIntensity = 0.18f + normalizedPower * 0.16f,
                    environmentIllumination = liveGlow,
                ),
                glow = FlashlightGlowState(
                    lensGlow = liveGlow,
                    ambientGlow = normalizedPower * 0.72f,
                    beamGlow = liveGlow * 0.82f,
                ),
                camera = FlashlightCameraState(
                    orbitYawDegrees = interaction.rotationYDegrees,
                    orbitPitchDegrees = interaction.rotationXDegrees,
                    parallaxX = interaction.parallaxX,
                    parallaxY = interaction.parallaxY,
                    distanceOffset = if (interaction.isTouching) -0.04f else 0f,
                ),
                environment = FlashlightEnvironmentState(
                    sceneId = FlashlightEnvironmentState.DefaultSceneId,
                    reflectionStrength = 0.22f + liveGlow * 0.18f,
                    floorShadowStrength = 0.58f - normalizedPower * 0.14f,
                    atmosphericGlow = liveGlow * 0.28f,
                ),
            )
        }
    }
}

data class FlashlightInteractionState(
    val isTouching: Boolean = false,
    val pressFraction: Float = 0f,
    val dragX: Float = 0f,
    val dragY: Float = 0f,
    val rotationXDegrees: Float = 0f,
    val rotationYDegrees: Float = 0f,
    val parallaxX: Float = 0f,
    val parallaxY: Float = 0f,
)

data class FlashlightLightingState(
    val keyLightIntensity: Float = 0.42f,
    val fillLightIntensity: Float = 0.18f,
    val environmentIllumination: Float = 0f,
)

data class FlashlightGlowState(
    val lensGlow: Float = 0f,
    val ambientGlow: Float = 0f,
    val beamGlow: Float = 0f,
)

data class FlashlightCameraState(
    val orbitYawDegrees: Float = 0f,
    val orbitPitchDegrees: Float = 0f,
    val parallaxX: Float = 0f,
    val parallaxY: Float = 0f,
    val distanceOffset: Float = 0f,
    val focalLengthMm: Float = 35f,
)

data class FlashlightEnvironmentState(
    val sceneId: String = DefaultSceneId,
    val reflectionStrength: Float = 0.22f,
    val floorShadowStrength: Float = 0.58f,
    val atmosphericGlow: Float = 0f,
) {
    companion object {
        const val DefaultSceneId: String = "cinematic-dark-stage"
    }
}

private fun Float.unit(): Float = coerceIn(0f, 1f)
