package com.example.blelib.bleCore

object HeartRateParser : CharacteristicParser<HeartRateData> {

    override fun parse(bytes: ByteArray): HeartRateData {
        val reader = ByteReader(bytes)

        val flags = reader.readUInt8()
        val isHr16Bit = flags and 0x01 != 0

        val bpm = if (isHr16Bit) reader.readUInt16() else reader.readUInt8()

        return HeartRateData(bpm = bpm)
    }
}
