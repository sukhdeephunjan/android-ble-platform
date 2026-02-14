package com.example.blelib.bleCore

data class HeartRateData(
    val bpm: Int,
    val isContactDetected: Boolean? = null,
    val energyExpended: Int? = null
)
