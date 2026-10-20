package com.example.composeapp

import com.example.composeapp.bledeviceui.model.BleParsedData
import kotlinx.coroutines.flow.SharedFlow

interface BleConnector {

    /**
     * Stream of parsed values received from the connected peripheral
     * (Heart Rate, Body Sensor Location, Temperature, …). Emissions start after [connect] succeeds
     * and notifications/reads have been initiated.
     */
    val dataUpdates: SharedFlow<BleParsedData>

    /**
     * Emits once every time an **established** connection is lost unexpectedly
     * (peripheral out of range, powered off, link supervision timeout).
     *
     * Intentional calls to [disconnect] do not emit.
     */
    val disconnections: SharedFlow<Unit>

    /**
     * Connects to [deviceId], discovers services and enables notifications /
     * indications / reads for every characteristic marked `enableOnConnect` in the
     * active BLE profiles.
     *
     * Returns success only once service discovery has completed.
     *
     * @param autoConnect when true the Android stack keeps a low-duty-cycle
     *   background connection request pending and links up by itself the moment
     *   the peripheral advertises again. Slower to establish but far more
     *   power-efficient, which makes it the right choice while reconnecting to a
     *   device that has gone out of range. Use `false` for the first, user-driven
     *   connection where latency matters.
     */
    suspend fun connect(deviceId: String, autoConnect: Boolean = false): Result<Unit>

    fun disconnect()

    /** Reads a specific GATT characteristic asynchronously. */
    suspend fun readCharacteristic(serviceUuid: String, characteristicUuid: String): Boolean

    /** Writes a value to a specific GATT characteristic. */
    suspend fun writeCharacteristic(serviceUuid: String, characteristicUuid: String, value: ByteArray): Boolean

    /** Writes to Heart Rate Control Point (e.g. 0x01 to reset energy expended). */
    suspend fun writeHeartRateControlPoint(controlValue: Byte): Boolean
}
