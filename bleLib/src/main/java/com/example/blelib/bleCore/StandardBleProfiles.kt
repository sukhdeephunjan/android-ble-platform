package com.example.blelib.bleCore


/**
 * Bluetooth SIG standard profiles supported by the platform.
 *
 * Active profiles (data received from peripherals):
 *   - Heart Rate  — HEART_RATE_SERVICE / HEART_RATE_MEASUREMENT (0x2A37)
 *   - Temperature — ADAFRUIT_TEMPERATURE_SERVICE / TEMPERATURE_MEASUREMENT (0x2A1C)
 *
 * Kept for future use:
 *   - Battery Level
 *   - Device Information
 */
object StandardBleProfiles : BleProfileProvider {

    override fun provideProfiles(): List<BleProfile> = listOf(
        heartRateProfile(),
        temperatureProfile(),
        // batteryProfile()    — add back when Battery pipeline is ready
        // deviceInfoProfile() — add back when Device Information pipeline is ready
    )

    // ── Active profiles ───────────────────────────────────────────────────────

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
                    ),
                    BleCharacteristicDef(
                        uuid = Uuids.BODY_SENSOR_LOCATION,
                        properties = setOf(CharacteristicProperty.READ),
                        enableOnConnect = true,
                        parser = BodySensorLocationParser
                    ),
                    BleCharacteristicDef(
                        uuid = Uuids.HEART_RATE_CONTROL_POINT,
                        properties = setOf(CharacteristicProperty.WRITE)
                    )
                )
            )
        )
    )

    /**
     * Adafruit-compatible temperature profile using the standard Health Thermometer
     * Measurement characteristic (0x2A1C / IEEE-11073 FLOAT).
     * The peripheral may use INDICATE or NOTIFY depending on firmware.
     */
    private fun temperatureProfile() = BleProfile(
        id = "temperature",
        displayName = "Temperature",
        services = listOf(
            BleServiceDef(
                uuid = Uuids.ADAFRUIT_TEMPERATURE_SERVICE,
                characteristics = listOf(
                    BleCharacteristicDef(
                        uuid = Uuids.TEMPERATURE_MEASUREMENT,
                        properties = setOf(
                            CharacteristicProperty.INDICATE,
                            CharacteristicProperty.NOTIFY
                        ),
                        enableOnConnect = true,
                        parser = TemperatureParser
                    )
                )
            )
        )
    )

    // ── Future profiles (kept, not yet wired to data pipeline) ────────────────

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
