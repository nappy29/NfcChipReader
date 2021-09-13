package com.example.axxtransdemo.data.model

import java.security.AccessControlException

class ApduResponse(respApdu: ByteArray) {
    var sW1 = 0x00
    var sW2 = 0x00
    var data = ByteArray(0)

    protected var mBytes = ByteArray(0)
    val sW1SW2: Int
        get() = (sW1 shl 8) or sW2

    fun toBytes(): ByteArray {
        return mBytes
    }

    @Throws(AccessControlException::class)
    fun checkLengthAndStatus(length: Int, sw1sw2: Int, message: String) {
        if (sW1SW2 != sw1sw2 || data.size != length) {
            throw AccessControlException(
                "ResponseApdu is wrong at "
                        + message
            )
        }
    }

    @Throws(AccessControlException::class)
    fun checkLengthAndStatus(
        length: Int, sw1sw2List: IntArray,
        message: String
    ) {
        if (data.size != length) {
            throw AccessControlException(
                ("ResponseApdu is wrong at "
                        + message)
            )
        }
        for (sw1sw2: Int in sw1sw2List) {
            if (sW1SW2 == sw1sw2) {
                return  // sw1sw2 matches => return
            }
        }
        throw AccessControlException("ResponseApdu is wrong at $message")
    }

    @Throws(AccessControlException::class)
    fun checkStatus(sw1sw2List: IntArray, message: String) {
        for (sw1sw2: Int in sw1sw2List) {
            if (sW1SW2 == sw1sw2) {
                return  // sw1sw2 matches => return
            }
        }
        throw AccessControlException("ResponseApdu is wrong at $message")
    }

    @Throws(AccessControlException::class)
    fun checkStatus(sw1sw2: Int, message: String) {
        if (sW1SW2 != sw1sw2) {
            throw AccessControlException(
                ("ResponseApdu is wrong at "
                        + message)
            )
        }
    }

    fun isStatus(sw1sw2: Int): Boolean {
        return sW1SW2 == sw1sw2
    }

    init {
        run {
            if (respApdu.size < 2) {
                return@run
            }
            if (respApdu.size > 2) {
                data = ByteArray(respApdu.size - 2)
                System.arraycopy(respApdu, 0, data, 0, respApdu.size - 2)
            }
            sW1 = 0x00FF and respApdu[respApdu.size - 2].toInt()
            sW2 = 0x00FF and respApdu[respApdu.size - 1].toInt()
            mBytes = respApdu
        }
    }

}