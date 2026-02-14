package com.example.composeapp

interface BleConnector {
    suspend fun connect(deviceId: String): Result<Unit>
    fun disconnect()
}