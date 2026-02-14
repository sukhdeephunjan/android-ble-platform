package com.example.composeapp.bledeviceui.di.connect

import com.example.composeapp.BleConnector
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BleConnectModule {
    @Binds
    abstract fun bindBleConnector(impl: BleConnectorImpl): BleConnector

}