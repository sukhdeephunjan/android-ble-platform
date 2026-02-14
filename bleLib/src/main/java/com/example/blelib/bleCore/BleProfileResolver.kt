package com.example.blelib.bleCore
/**
 * Resolves which profiles are supported by a connected BLE device
 * based on discovered GATT services.
 */
class BleProfileResolver(
    private val providers: List<BleProfileProvider>
) {

    fun resolve(discoveredServiceUuids: Set<String>): List<BleProfile> {
        val allProfiles = providers.flatMap { it.provideProfiles() }

        return allProfiles.filter { profile ->
            profile.services.all { service ->
                !service.required || discoveredServiceUuids.contains(service.uuid)
            }
        }
    }
}
