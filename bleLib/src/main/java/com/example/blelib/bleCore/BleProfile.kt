package com.example.blelib.bleCore

/**
 * Describes a logical BLE capability (e.g. Heart Rate, Battery, NPH Sensor).
 */
data class BleProfile(
    val id: String,                 // Stable identifier
    val displayName: String,
    val services: List<BleServiceDef>
)

/**
 * Describes a GATT service within a BLE profile.
 */
data class BleServiceDef(
    val uuid: String,
    val required: Boolean = true,
    val characteristics: List<BleCharacteristicDef>
)

/**
 * Describes a GATT characteristic within a service.
 */
data class BleCharacteristicDef(
    val uuid: String,
    val properties: Set<CharacteristicProperty>,
    val required: Boolean = true,
    val enableOnConnect: Boolean = false,
    val parser: CharacteristicParser<*>? = null
)
