package com.example.composeapp.bledeviceui.di.scan

import com.example.composeapp.bledeviceui.scanner.BleScanner
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BleScannerModule {

    @Binds
    @Singleton
    abstract fun bindBleScanner(
        impl: BleScannerImpl
    ): BleScanner
}