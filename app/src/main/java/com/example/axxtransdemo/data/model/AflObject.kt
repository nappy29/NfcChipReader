package com.example.axxtransdemo.data.model

data class AflObject (
    var sfi: Int = 0,
    var recordNumber: Int = 0,
    var readCommand: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AflObject

        if (sfi != other.sfi) return false
        if (recordNumber != other.recordNumber) return false
        if (readCommand != null) {
            if (other.readCommand == null) return false
            if (!readCommand.contentEquals(other.readCommand)) return false
        } else if (other.readCommand != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sfi
        result = 31 * result + recordNumber
        result = 31 * result + (readCommand?.contentHashCode() ?: 0)
        return result
    }
}