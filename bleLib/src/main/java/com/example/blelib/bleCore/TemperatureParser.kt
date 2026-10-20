package com.example.blelib.bleCore

/**
 * Parses the Health Thermometer Measurement characteristic (UUID 0x2A1C).
 *
 * Byte layout (Bluetooth SIG spec):
 *  - Byte 0       : Flags
 *      bit 0 → Temperature Unit (0 = Celsius, 1 = Fahrenheit)
 *      bit 1 → Time Stamp present
 *      bit 2 → Temperature Type present
 *  - Bytes 1–4    : Temperature value, IEEE-11073 32-bit FLOAT (little-endian)
 *      Bytes 1–3  : Mantissa (24-bit signed, little-endian)
 *      Byte  4    : Exponent (8-bit signed)
 *      value = mantissa × 10^exponent
 */
object TemperatureParser : CharacteristicParser<TemperatureData> {

    override fun parse(bytes: ByteArray): TemperatureData {
        require(bytes.size >= 5) { "Temperature measurement must be at least 5 bytes" }

        val flags      = bytes[0].toInt() and 0xFF
        val isFahrenheit = flags and 0x01 != 0

        // ── IEEE-11073 FLOAT ──────────────────────────────────────────────────
        // Bytes 1-3: 24-bit signed mantissa, little-endian
        val mantissaRaw = (bytes[1].toInt() and 0xFF) or
                          ((bytes[2].toInt() and 0xFF) shl 8) or
                          ((bytes[3].toInt() and 0xFF) shl 16)

        // Sign-extend from 24-bit to 32-bit
        val mantissa = if (mantissaRaw and 0x800000 != 0)
            mantissaRaw or (-0x1000000)   // set upper 8 bits to 1
        else
            mantissaRaw

        // Byte 4: signed exponent
        val exponent = bytes[4].toInt()   // Kotlin Byte is signed; no masking needed

        val rawValue = (mantissa * Math.pow(10.0, exponent.toDouble())).toFloat()

        return if (isFahrenheit) {
            TemperatureData(
                celsius     = (rawValue - 32f) * 5f / 9f,
                fahrenheit  = rawValue,
                unit        = TemperatureUnit.FAHRENHEIT
            )
        } else {
            TemperatureData(
                celsius     = rawValue,
                fahrenheit  = rawValue * 9f / 5f + 32f,
                unit        = TemperatureUnit.CELSIUS
            )
        }
    }
}

