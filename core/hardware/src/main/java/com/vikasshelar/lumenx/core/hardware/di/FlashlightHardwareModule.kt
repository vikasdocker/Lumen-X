package com.vikasshelar.lumenx.core.hardware.di

import com.vikasshelar.lumenx.core.hardware.AndroidFlashlightController
import com.vikasshelar.lumenx.core.hardware.FlashlightController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FlashlightHardwareModule {
    @Binds
    @Singleton
    abstract fun bindFlashlightController(
        controller: AndroidFlashlightController,
    ): FlashlightController
}
