package com.vikasshelar.lumenx.core.hardware

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import com.vikasshelar.lumenx.core.common.AppDispatchers
import com.vikasshelar.lumenx.core.model.TorchCapability
import com.vikasshelar.lumenx.core.model.TorchError
import com.vikasshelar.lumenx.core.model.TorchSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

@Singleton
class AndroidFlashlightController @Inject constructor(
    @ApplicationContext context: Context,
    private val dispatchers: AppDispatchers,
) : FlashlightController {
    private val appContext = context.applicationContext
    private val cameraManager = appContext.getSystemService(CameraManager::class.java)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val initialSnapshot = createInitialSnapshot()
    private val torchState = MutableStateFlow(initialSnapshot)

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId == torchState.value.capability.cameraId) {
                torchState.update { snapshot ->
                    snapshot.copy(isEnabled = enabled, error = null)
                }
            }
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            if (cameraId == torchState.value.capability.cameraId) {
                torchState.update { snapshot ->
                    snapshot.copy(
                        isEnabled = false,
                        error = TorchError.CameraUnavailable("camera service reported the torch as unavailable"),
                    )
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onTorchStrengthLevelChanged(cameraId: String, newStrengthLevel: Int) {
            if (cameraId == torchState.value.capability.cameraId) {
                torchState.update { snapshot ->
                    snapshot.copy(strengthLevel = newStrengthLevel.coerceIn(snapshot.strengthRange()))
                }
            }
        }
    }

    init {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                cameraManager.registerTorchCallback(appContext.mainExecutor, torchCallback)
            } else {
                @Suppress("DEPRECATION")
                cameraManager.registerTorchCallback(torchCallback, mainHandler)
            }
        }.onFailure { throwable ->
            torchState.update { snapshot ->
                snapshot.copy(error = throwable.toTorchError())
            }
        }
    }

    override suspend fun getCapability(): TorchCapability = withContext(dispatchers.io) {
        torchState.value.capability
    }

    override suspend fun isTorchAvailable(): Boolean = getCapability().isAvailable

    override suspend fun isTorchEnabled(): Boolean = withContext(dispatchers.io) {
        torchState.value.isEnabled
    }

    override suspend fun supportsBrightness(): Boolean = getCapability().supportsStrength

    override suspend fun getMaxBrightnessLevel(): Int = getCapability().safeMaxStrengthLevel

    @SuppressLint("MissingPermission")
    override suspend fun setTorchEnabled(enabled: Boolean): TorchCommandResult = withContext(dispatchers.io) {
        val snapshot = torchState.value
        val cameraId = snapshot.capability.cameraId
        if (!snapshot.capability.isAvailable || cameraId == null) {
            return@withContext fail(TorchError.NoFlashUnit)
        }

        runCatching {
            if (enabled && snapshot.capability.supportsStrength && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                cameraManager.turnOnTorchWithStrengthLevel(
                    cameraId,
                    snapshot.strengthLevel.coerceIn(snapshot.strengthRange()),
                )
            } else {
                cameraManager.setTorchMode(cameraId, enabled)
            }
        }.fold(
            onSuccess = {
                torchState.update { current ->
                    current.copy(isEnabled = enabled, error = null)
                }
                TorchCommandResult.Success
            },
            onFailure = { throwable -> fail(throwable.toTorchError()) },
        )
    }

    @SuppressLint("MissingPermission")
    override suspend fun setBrightnessLevel(level: Int): TorchCommandResult = withContext(dispatchers.io) {
        val snapshot = torchState.value
        val cameraId = snapshot.capability.cameraId
        if (!snapshot.capability.isAvailable || cameraId == null) {
            return@withContext fail(TorchError.NoFlashUnit)
        }
        if (!snapshot.capability.supportsStrength || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return@withContext fail(TorchError.UnsupportedStrength)
        }

        val coercedLevel = level.coerceIn(snapshot.strengthRange())
        runCatching {
            cameraManager.turnOnTorchWithStrengthLevel(cameraId, coercedLevel)
        }.fold(
            onSuccess = {
                torchState.update { current ->
                    current.copy(
                        isEnabled = true,
                        strengthLevel = coercedLevel,
                        error = null,
                    )
                }
                TorchCommandResult.Success
            },
            onFailure = { throwable -> fail(throwable.toTorchError()) },
        )
    }

    override fun observeTorchState(): Flow<TorchSnapshot> = torchState.asStateFlow()

    private fun createInitialSnapshot(): TorchSnapshot {
        return runCatching {
            discoverCapability()
        }.fold(
            onSuccess = { capability ->
                val error = if (capability.isAvailable) null else TorchError.NoFlashUnit
                TorchSnapshot(
                    capability = capability,
                    isEnabled = false,
                    strengthLevel = capability.safeDefaultStrengthLevel,
                    error = error,
                )
            },
            onFailure = { throwable ->
                TorchSnapshot(
                    capability = TorchCapability(isAvailable = false, supportsStrength = false),
                    error = throwable.toTorchError(),
                )
            },
        )
    }

    private fun discoverCapability(): TorchCapability {
        val flashCameraIds = cameraManager.cameraIdList
            .mapNotNull { cameraId ->
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                if (!hasFlash) return@mapNotNull null

                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                FlashCameraCandidate(
                    cameraId = cameraId,
                    lensFacing = facing,
                    maxStrength = characteristics.maxTorchStrength(),
                    defaultStrength = characteristics.defaultTorchStrength(),
                )
            }

        val selectedCamera = flashCameraIds.firstOrNull {
            it.lensFacing == CameraCharacteristics.LENS_FACING_BACK
        } ?: flashCameraIds.firstOrNull()

        return if (selectedCamera == null) {
            TorchCapability(isAvailable = false, supportsStrength = false)
        } else {
            TorchCapability(
                isAvailable = true,
                supportsStrength = selectedCamera.maxStrength > 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
                maxStrengthLevel = selectedCamera.maxStrength,
                defaultStrengthLevel = selectedCamera.defaultStrength,
                cameraId = selectedCamera.cameraId,
            )
        }
    }

    private fun fail(error: TorchError): TorchCommandResult.Failure {
        torchState.update { snapshot ->
            snapshot.copy(error = error, isEnabled = if (error is TorchError.NoFlashUnit) false else snapshot.isEnabled)
        }
        return TorchCommandResult.Failure(error)
    }

    private fun Throwable.toTorchError(): TorchError {
        return when (this) {
            is CameraAccessException -> toTorchError()
            is SecurityException -> TorchError.SecurityDenied
            is IllegalArgumentException -> TorchError.SystemError(message ?: "Invalid flashlight hardware request.")
            else -> TorchError.SystemError(message ?: "Unexpected flashlight hardware failure.")
        }
    }

    private fun CameraAccessException.toTorchError(): TorchError {
        val reasonText = when (reason) {
            CameraAccessException.CAMERA_DISABLED -> "camera access is disabled by policy"
            CameraAccessException.CAMERA_DISCONNECTED -> "camera service disconnected"
            CameraAccessException.CAMERA_ERROR -> "camera service error"
            CameraAccessException.CAMERA_IN_USE -> "camera is already in use"
            CameraAccessException.MAX_CAMERAS_IN_USE -> "camera resources are exhausted"
            else -> message ?: "camera access failed"
        }
        return TorchError.CameraUnavailable(reasonText)
    }

    private fun TorchSnapshot.strengthRange(): IntRange = 1..capability.safeMaxStrengthLevel

    private fun CameraCharacteristics.maxTorchStrength(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
        } else {
            1
        }
    }

    private fun CameraCharacteristics.defaultTorchStrength(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            get(CameraCharacteristics.FLASH_INFO_STRENGTH_DEFAULT_LEVEL) ?: 1
        } else {
            1
        }
    }

    private data class FlashCameraCandidate(
        val cameraId: String,
        val lensFacing: Int?,
        val maxStrength: Int,
        val defaultStrength: Int,
    )
}
