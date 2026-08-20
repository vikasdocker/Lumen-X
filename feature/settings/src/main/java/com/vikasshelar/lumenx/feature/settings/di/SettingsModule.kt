package com.vikasshelar.lumenx.feature.settings.di

import com.vikasshelar.lumenx.feature.settings.DataStoreSettingsRepository
import com.vikasshelar.lumenx.feature.settings.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        repository: DataStoreSettingsRepository,
    ): SettingsRepository
}
