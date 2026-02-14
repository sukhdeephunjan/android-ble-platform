package com.example.composeapp.bledeviceui.scanner

import com.example.blelib.bleCore.BleProfile
import com.example.composeapp.bledeviceui.model.ScanResultWrapper
import com.example.composeapp.bledeviceui.model.UiBleDevice
import javax.inject.Inject

class ScanResultMapper @Inject constructor() {

    fun map(
        wrapper: ScanResultWrapper,
        profiles: List<BleProfile>
    ): UiBleDevice {

        val advertisedUuids: Set<String> =
            wrapper.serviceUuids
                .map { it.toString() }
                .toSet()

        val matchedProfileIds: Set<String> =
            profiles
                .filter { profile ->
                    profile.services.any { service ->
                        service.uuid in advertisedUuids
                    }
                }
                .map { it.id }
                .toSet()

        return UiBleDevice(
            id = wrapper.deviceId,
            name = wrapper.deviceName ?: "Unnamed Device",
            profileIds = matchedProfileIds
        )
    }
}