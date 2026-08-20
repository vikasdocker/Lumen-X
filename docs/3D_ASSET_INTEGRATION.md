# 3D Asset Integration

Lumen X is prepared for real 3D flashlight assets without binding flashlight business logic to a specific renderer or model format.

## Current Decision

No large 3D rendering dependency has been added yet.

The current Android stack is Jetpack Compose with a high-quality procedural `Canvas` flashlight. Because no legally verified real 3D model is included yet, the safest maintained approach is to keep the existing Compose renderer as the default implementation behind a renderer-neutral visual contract. This avoids APK size, native dependency, ABI, and lifecycle cost until an actual GLB/glTF asset and target rendering requirements are known.

When a real model is selected, the next renderer should be chosen through a small spike against this interface. The likely asset format should be GLB or glTF because it preserves model hierarchy, materials, textures, and animation data better than a flat mesh-only format.

## Architecture

The visual contract lives in `feature/flashlight/.../visual`:

- `FlashlightVisual` renders a model from a visual state.
- `FlashlightModel` records renderer-independent model metadata and asset source information.
- `FlashlightVisualState` carries power, brightness, mode, interaction, lighting, glow, camera, and environment state.
- `ProceduralFlashlightVisual` is the current placeholder renderer and can be replaced without changing `FlashlightViewModel` or the hardware controller.

The hardware layer remains isolated in `core:hardware`. UI visuals receive only state derived from `FlashlightUiState`; they do not call Android `CameraManager` or mutate real torch state.

## Future Renderer Requirements

A real 3D renderer implementation should:

- Implement `FlashlightVisual`.
- Consume `FlashlightVisualState` for power, brightness, mode, touch, drag, rotation/parallax, lighting, glow, camera movement, and environment scenes.
- Resolve model and texture paths from `FlashlightModel.source`.
- Keep live flashlight behavior inside the existing hardware controller.
- Respect reduced-motion settings by preserving the existing animation-scale path.
- Fail back to `ProceduralFlashlightVisual` if a model fails to load.

## Asset Folders

```text
assets/
  3d/        GLB, glTF, OBJ, or other model files after license review
  textures/  Texture maps after license review
  audio/     Sound assets after license review
  licenses/  License text, attribution notes, or source snapshots
```

All imported external assets must be recorded in `ASSET_LICENSES.md` before use.
