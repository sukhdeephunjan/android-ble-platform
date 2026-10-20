package com.example.composeapp.bledeviceui.di

import com.example.blelib.bleCore.BleProfileProvider
import com.example.blelib.bleCore.StandardBleProfiles
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BleModule {

    @Provides
    @Singleton
    fun provideBleProfileProvider(): BleProfileProvider = StandardBleProfiles
}

