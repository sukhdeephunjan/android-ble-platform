package com.example.blelib.bleCore

import kotlin.math.pow

/**
 * Parses the Bluetooth SIG **Temperature Measurement** characteristic (0x2A1C).
 *
 * Layout:
 * ```
 * byte 0      flags   — bit 0: 0 = Celsius, 1 = Fahrenheit
 * bytes 1..4  value   — IEEE-11073 32-bit FLOAT
 *                       bytes 1..3 = 24-bit signed mantissa (little-endian)
 *                       byte  4    = 8-bit signed exponent
 * ```
 * The measurement is `mantissa × 10^exponent`.
 */
object TemperatureParser : CharacteristicParser<TemperatureData> {

    override fun parse(bytes: ByteArray): TemperatureData {
        require(bytes.size >= 5) { "Temperature payload too short: ${bytes.size} bytes" }

        val flags = bytes[0].toInt() and 0xFF
        val isFahrenheit = flags and 0x01 != 0

        // 24-bit signed mantissa, little-endian
        var mantissa = (bytes[1].toInt() and 0xFF) or
                ((bytes[2].toInt() and 0xFF) shl 8) or
                ((bytes[3].toInt() and 0xFF) shl 16)
        if (mantissa and 0x800000 != 0) {
            // Sign-extend from 24 to 32 bits
            mantissa = mantissa or 0xFF000000.toInt()
        }

        val exponent = bytes[4].toInt()   // already signed
        val value = (mantissa * 10.0.pow(exponent)).toFloat()

        return if (isFahrenheit) {
            TemperatureData(
                celsius = (value - 32f) * 5f / 9f,
                fahrenheit = value,
                unit = TemperatureUnit.FAHRENHEIT
            )
        } else {
            TemperatureData(celsius = value, unit = TemperatureUnit.CELSIUS)
        }
    }
}

