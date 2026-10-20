package com.example.composeapp.bledeviceui.model

/**
 * A single value notified by a connected peripheral, already converted from raw
 * bytes to a domain model by the profile's `CharacteristicParser`.
 *
 * [value] is intentionally untyped: every profile contributes its own parsed
 * type (e.g. `HeartRateData`, `TemperatureData`), so consumers branch on it and
 * adding a new profile never changes this contract.
 */
data class BleParsedData(
    /** Source characteristic UUID, uppercase. */
    val characteristicUuid: String,
    val value: Any
)

