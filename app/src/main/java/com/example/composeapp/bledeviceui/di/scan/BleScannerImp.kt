package com.example.composeapp.bledeviceui.di.scan

import android.content.Context
import com.example.composeapp.bledeviceui.scanner.BleScanner
import com.example.composeapp.bledeviceui.model.ScanResultWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleScannerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BleScanner {

    override fun scan(): Flow<ScanResultWrapper> {
        // actual BLE scan logic

        return TODO("Provide the return value")
    }

    override fun stop() {
        // stop scanning
    }
}
