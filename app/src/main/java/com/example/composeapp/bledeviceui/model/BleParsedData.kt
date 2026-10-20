package com.example.composeapp.bledeviceui.model

/**
 * A single parsed notification/indication coming from a connected peripheral.
 *
 * @param characteristicUuid The GATT characteristic that produced the value (uppercase).
 * @param value The parsed domain object produced by the profile's
 *              [com.example.blelib.bleCore.CharacteristicParser]
 *              (e.g. `HeartRateData`, `TemperatureData`).
 */
data class BleParsedData(
    val characteristicUuid: String,
    val value: Any
)
