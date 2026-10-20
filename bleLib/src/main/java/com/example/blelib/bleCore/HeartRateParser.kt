package com.example.blelib.bleCore

object HeartRateParser : CharacteristicParser<HeartRateData> {

    override fun parse(bytes: ByteArray): HeartRateData {
        val reader = ByteReader(bytes)

        val flags = reader.readUInt8()
        val isHr16Bit = flags and 0x01 != 0

        val bpm = if (isHr16Bit) reader.readUInt16() else reader.readUInt8()

        val contactSupported = (flags and 0x06) != 0
        val isContactDetected = if (contactSupported) (flags and 0x06) == 0x06 else null

        val hasEnergyExpended = (flags and 0x08) != 0
        val energyExpended = if (hasEnergyExpended) reader.readUInt16() else null

        return HeartRateData(
            bpm = bpm,
            isContactDetected = isContactDetected,
            energyExpended = energyExpended
        )
    }
}
