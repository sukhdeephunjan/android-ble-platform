package com.example.blelib.bleCore

enum class TemperatureUnit { CELSIUS, FAHRENHEIT }

/**
 * Parsed result from the Health Thermometer Measurement characteristic (0x2A1C).
 *
 * @param celsius  Temperature in Celsius (always present — converted if device sent Fahrenheit).
 * @param fahrenheit  Temperature in Fahrenheit.
 * @param unit  The unit originally reported by the peripheral.
 */
data class TemperatureData(
    val celsius: Float,
    val fahrenheit: Float = celsius * 9f / 5f + 32f,
    val unit: TemperatureUnit = TemperatureUnit.CELSIUS
)

