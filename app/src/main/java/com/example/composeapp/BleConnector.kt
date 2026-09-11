package com.example.composeapp

import com.example.composeapp.bledeviceui.model.BleParsedData
import kotlinx.coroutines.flow.SharedFlow

interface BleConnector {

    /**
     * Stream of parsed values received from the connected peripheral
     * (Heart Rate, Temperature, …). Emissions start after [connect] succeeds
     * and notifications have been enabled.
     */
    val dataUpdates: SharedFlow<BleParsedData>

    /**
     * Connects to [deviceId], discovers services and enables notifications /
     * indications for every characteristic marked `enableOnConnect` in the
     * active BLE profiles.
     *
     * Returns success only once service discovery has completed.
     */
    suspend fun connect(deviceId: String): Result<Unit>

    fun disconnect()
}