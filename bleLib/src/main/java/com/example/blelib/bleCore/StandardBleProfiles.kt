package com.example.blelib.bleCore

import HeartRateParser


/**
 * Bluetooth SIG standard profiles supported by the platform.
 */
object StandardBleProfiles : BleProfileProvider {

    override fun provideProfiles(): List<BleProfile> = listOf(
        heartRateProfile(),
        batteryProfile(),
        deviceInfoProfile()
    )

    private fun heartRateProfile() = BleProfile(
        id = "heart_rate",
        displayName = "Heart Rate",
        services = listOf(
            BleServiceDef(
                uuid = Uuids.HEART_RATE_SERVICE,
                characteristics = listOf(
                    BleCharacteristicDef(
                        uuid = Uuids.HEART_RATE_MEASUREMENT,
                        properties = setOf(CharacteristicProperty.NOTIFY),
                        enableOnConnect = true,
                        parser = HeartRateParser
                    )
                )
            )
        )
    )

    private fun batteryProfile() = BleProfile(
        id = "battery",
        displayName = "Battery Level",
        services = listOf(
            BleServiceDef(
                uuid = Uuids.BATTERY_SERVICE,
                characteristics = listOf(
                    BleCharacteristicDef(
                        uuid = Uuids.BATTERY_LEVEL,
                        properties = setOf(CharacteristicProperty.READ, CharacteristicProperty.NOTIFY)
                    )
                )
            )
        )
    )

    private fun deviceInfoProfile() = BleProfile(
        id = "device_info",
        displayName = "Device Information",
        services = listOf(
            BleServiceDef(
                uuid = Uuids.DEVICE_INFORMATION,
                characteristics = listOf(
                    BleCharacteristicDef(
                        uuid = Uuids.MANUFACTURER_NAME,
                        properties = setOf(CharacteristicProperty.READ)
                    )
                )
            )
        )
    )
}
