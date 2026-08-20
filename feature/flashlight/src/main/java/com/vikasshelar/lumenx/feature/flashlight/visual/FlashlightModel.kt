package com.vikasshelar.lumenx.feature.flashlight.visual

data class FlashlightModel(
    val id: String,
    val displayName: String,
    val source: FlashlightModelSource,
    val scaleHint: Float = 1f,
)

data class FlashlightModelSource(
    val localAssetPath: String?,
    val format: FlashlightModelFormat,
    val licenseRecordId: String?,
)

enum class FlashlightModelFormat {
    ProceduralCompose,
    Glb,
    Gltf,
    Obj,
}

object FlashlightModels {
    val ProceduralPlaceholder = FlashlightModel(
        id = "procedural-lumen-x-placeholder",
        displayName = "Procedural Lumen X placeholder",
        source = FlashlightModelSource(
            localAssetPath = null,
            format = FlashlightModelFormat.ProceduralCompose,
            licenseRecordId = null,
        ),
    )
}
