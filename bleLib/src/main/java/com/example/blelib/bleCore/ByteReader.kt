package com.example.blelib.bleCore

class ByteReader(private val bytes: ByteArray) {
    private var index = 0

    fun readUInt8(): Int =
        bytes[index++].toInt() and 0xFF

    fun readUInt16(): Int {
        val value = (bytes[index].toInt() and 0xFF) or
                ((bytes[index + 1].toInt() and 0xFF) shl 8)
        index += 2
        return value
    }

    fun hasRemaining(): Boolean = index < bytes.size
}
