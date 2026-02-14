package com.example.composeapp.bledeviceui.scanner

import com.example.composeapp.bledeviceui.model.ScanResultWrapper
import kotlinx.coroutines.flow.Flow

interface BleScanner {
    fun scan(): Flow<ScanResultWrapper>
    fun stop()
}