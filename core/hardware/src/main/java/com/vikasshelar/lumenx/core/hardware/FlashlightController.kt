package com.vikasshelar.lumenx.core.hardware

import com.vikasshelar.lumenx.core.model.TorchCapability
import com.vikasshelar.lumenx.core.model.TorchError
import com.vikasshelar.lumenx.core.model.TorchSnapshot
import kotlinx.coroutines.flow.Flow

interface FlashlightController {
    suspend fun getCapability(): TorchCapability
    suspend fun isTorchAvailable(): Boolean
    suspend fun isTorchEnabled(): Boolean
    suspend fun setTorchEnabled(enabled: Boolean): TorchCommandResult
    suspend fun supportsBrightness(): Boolean
    suspend fun getMaxBrightnessLevel(): Int
    suspend fun setBrightnessLevel(level: Int): TorchCommandResult
    fun observeTorchState(): Flow<TorchSnapshot>
}

sealed interface TorchCommandResult {
    data object Success : TorchCommandResult
    data class Failure(val error: TorchError) : TorchCommandResult
}
