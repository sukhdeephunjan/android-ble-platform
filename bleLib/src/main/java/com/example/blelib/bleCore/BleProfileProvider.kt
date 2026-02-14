package com.example.blelib.bleCore

/**
 * Allows profiles to be supplied from different modules.
 *
 * Enables:
 * - Standard profiles
 * - Vendor-specific profiles
 * - Feature-based profile loading
 */
interface BleProfileProvider {
    fun provideProfiles(): List<BleProfile>
}

interface CharacteristicParser<T> {
    fun parse(bytes: ByteArray): T
}