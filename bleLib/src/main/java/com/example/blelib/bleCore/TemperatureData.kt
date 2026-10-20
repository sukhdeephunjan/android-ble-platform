package com.example.blelib.bleCore

/** Unit reported by the peripheral in the Health Thermometer flags byte. */
enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT
}

/**
 * A single temperature measurement.
 *
 * [fahrenheit] is derived from [celsius] when not supplied, so callers can pick
 * whichever scale they need without repeating the conversion.
 */
data class TemperatureData(
    val celsius: Float,
    val fahrenheit: Float = celsius * 9f / 5f + 32f,
    val unit: TemperatureUnit = TemperatureUnit.CELSIUS
)

