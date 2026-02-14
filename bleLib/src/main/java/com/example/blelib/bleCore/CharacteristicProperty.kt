package com.example.blelib.bleCore

/**
 * Defines how a BLE characteristic can be used.
 */
enum class CharacteristicProperty {
    READ,
    WRITE,
    WRITE_NO_RESPONSE,
    NOTIFY,
    INDICATE
}
