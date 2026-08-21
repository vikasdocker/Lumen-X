# Lumen X

Lumen X is a premium Android flashlight foundation built with Kotlin, Jetpack Compose, MVVM, Hilt, Navigation Compose, DataStore, Coroutines/Flow, and Android Camera2 flashlight APIs. The first version focuses on a cinematic single-device experience rather than a generic utility dashboard.

## Current Functionality

- Real hardware torch ON/OFF through `CameraManager`.
- Hardware flash availability detection with graceful unavailable/error states.
- Real LED torch strength support on Android 13+ devices that expose torch strength levels.
- Disabled intensity control with clear copy when the device does not expose LED strength levels.
- Persisted torch preference, intensity level, mode, timer, haptics, sound, and theme settings.
- Central Compose-rendered flagship-style flashlight placeholder with body, lens, LED cluster, switch rail, grip details, reflections, shadowing, and ambient glow.
- Drag-reactive physical device stage with press/tilt microinteractions and adaptive portrait/landscape layouts.
- Renderer-neutral flashlight visual architecture prepared for real 3D model integration.
- Premium radial intensity control with LOW/MEDIUM/HIGH labels and current intensity percentage.
- BEAM/STROBE/SOS/BEACON/SIGNAL/MORSE/CUSTOM mode selector backed by a scalable mode engine.
- Physical-feeling animated power actuator, glow transition, brightness transition, mode selector, timer, battery indicator, haptic cues, and tone-based sound feedback architecture.
- Timer shutdown logic that turns off the real torch when the countdown completes.

## Architecture

The app is intentionally split so UI code never talks directly to Android camera APIs.

```text
app/
  Android app shell, Hilt application, navigation host
core/
  common/        Coroutine dispatcher abstraction
  designsystem/  Lumen X colors, theme, glass surfaces, cinematic background
  hardware/      FlashlightController interface and CameraManager implementation
  model/         Immutable torch, capability, error, and mode models
  ui/            Battery state and feedback utilities
feature/
  flashlight/    Main screen, ViewModel, UI state, visual abstraction, mode engine orchestration
  modes/         Mode metadata, timing patterns, Morse conversion, reducer, and selector UI
  settings/      DataStore repository and settings screen
  timer/         Timer state machine and timer UI
assets/
  3d/            Future licensed model files
  textures/      Future licensed texture files
  audio/         Future licensed audio files
  licenses/      License text, attribution notes, or source snapshots
```

The flashlight visual layer is isolated behind `FlashlightVisual`, `FlashlightModel`, and `FlashlightVisualState`. The current procedural Compose renderer is the default implementation, and a future GLB/glTF renderer can replace it without changing hardware control or flashlight business logic.

## Hardware Notes

Lumen X never fakes flashlight intensity. Screen brightness is not used as a substitute for LED brightness.

Brightness control is enabled only when Android reports `FLASH_INFO_STRENGTH_MAXIMUM_LEVEL > 1` and the device supports `CameraManager.turnOnTorchWithStrengthLevel`. Devices without strength support still get real torch ON/OFF through `CameraManager.setTorchMode`, with the radial intensity control disabled.

The mode engine keeps timing logic out of the UI. BEAM uses continuous hardware torch output. STROBE, SOS, BEACON, SIGNAL, MORSE, and CUSTOM run through a common `FlashMode` contract with lifecycle start/stop behavior and safety-clamped timing defaults.

Active modes are stopped when the user turns power off, another mode starts, a timer expires, the app leaves the foreground, or the flashlight becomes unavailable. The engine uses structured coroutine cancellation so timing jobs do not leak.

Handled hardware cases include no flash unit, camera service errors, camera in use, camera policy disabled, security denial, unsupported strength, and torch unavailable callbacks.

Android reference: [CameraManager](https://developer.android.com/reference/android/hardware/camera2/CameraManager).

## Asset Licensing

No external art, 3D, texture, or audio assets are currently included. Future assets must be recorded in `ASSET_LICENSES.md` before use, including source URL, creator, license, attribution requirement, commercial-use permission, and modification permission. Prefer CC0 or similarly permissive sources.

3D integration notes are in `docs/3D_ASSET_INTEGRATION.md`.

## Toolchain

- Java 17
- Gradle wrapper 8.14.3
- Android Gradle Plugin 8.13.2
- Kotlin 2.2.21
- Compile SDK 36
- Target SDK 36
- Min SDK 26

The local machine currently has Android SDK platforms 34, 35, and 36 installed, so the project targets SDK 36 rather than SDK 37-era libraries.

## Setup

```powershell
.\gradlew.bat test assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Instrumentation Compose UI tests are under `feature/flashlight/src/androidTest`. They require an online connected Android device or emulator:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

## Tests

Unit coverage currently includes:

- Torch capability bounds and unavailable state.
- Flashlight ViewModel ON/OFF persistence and unsupported brightness behavior.
- Flash mode engine transitions.
- SOS, Morse, strobe, and beacon timing behavior.
- Mode reducer state transitions.
- Timer arm/tick/cancel behavior.

Compose UI test coverage currently includes:

- Power button interaction.
- Disabled intensity control on unsupported hardware.
- Mode selector interaction.
- Timer selector interaction.

## Roadmap

- Replace the Compose placeholder with a real 3D flashlight asset.
- Add UI editors for MORSE text, SIGNAL programs, and CUSTOM user-created flash patterns.
- Add emergency affordances with explicit user confirmation and device capability checks.
- Add deeper lifecycle policies for background torch behavior.
- Add visual polish passes for tablets, foldables, landscape, and dynamic color opt-in.
- Add connected-device test automation once an emulator/device is available in CI.
