package com.example.axxtransdemo.data.model.util

import android.util.Log
import java.io.ByteArrayOutputStream

object ApduUtil {
    private val TAG = ApduUtil::class.java.name
    private val PSE = "1PAY.SYS.DDF01".toByteArray() // PSE for Contact
    private val PPSE = "2PAY.SYS.DDF01".toByteArray() // PPSE for Contactless
    private const val GPO_P1 = 0x00.toByte()
    private const val GPO_P2 = 0x00.toByte()
    fun selectPse(): ByteArray? {
        // Returning result
        return selectPse(PSE)
    }


    fun selectPpse(): ByteArray? {
        // Returning result
        return selectPse(PPSE)
    }

    // PSE (Proximity Payment System Environment)
    fun selectPse(pse: ByteArray?): ByteArray? {
        // Returning result
        var result: ByteArray? = null
        // - Returning result
        var pseByteArrayOutputStream: ByteArrayOutputStream? = null
        try {
            pseByteArrayOutputStream = ByteArrayOutputStream()
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
        if (pseByteArrayOutputStream != null) {
            try {
                pseByteArrayOutputStream.write(TlvTagConstant.SELECT)
                pseByteArrayOutputStream.write(
                    byteArrayOf(
                        0x04.toByte(),  // P1
                        0x00.toByte() // P2
                    )
                )
                if (pse != null) {
                    pseByteArrayOutputStream.write(
                        byteArrayOf(
                            pse.size.toByte() // Lc
                        )
                    )
                    pseByteArrayOutputStream.write(pse) // Data
                }
                pseByteArrayOutputStream.write(
                    byteArrayOf(
                        0x00.toByte() // Le
                    )
                )
                pseByteArrayOutputStream.close()
                result = pseByteArrayOutputStream.toByteArray()
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
        return result
    }

    // - PSE (Proximity Payment System Environment)
    fun selectApplication(aid: ByteArray): ByteArray? {
        // Returning result
        var result: ByteArray? = null
        // - Returning result
        var byteArrayOutputStream: ByteArrayOutputStream? = null
        try {
            byteArrayOutputStream = ByteArrayOutputStream()
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
        if (byteArrayOutputStream != null) {
            try {
                byteArrayOutputStream.write(TlvTagConstant.SELECT)
                byteArrayOutputStream.write(
                    byteArrayOf(
                        0x04.toByte(),  // P1
                        0x00.toByte(),  // P2
                        aid.size.toByte() // Lc
                    )
                )
                byteArrayOutputStream.write(aid) // Data
                byteArrayOutputStream.write(
                    byteArrayOf(
                        0x00.toByte() // Le
                    )
                )
                byteArrayOutputStream.close()
                result = byteArrayOutputStream.toByteArray()
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
        return result
    }

    fun getProcessingOption(pdolConstructed: ByteArray): ByteArray? {
        // Returning result
        var result: ByteArray? = null
        // - Returning result
        var byteArrayOutputStream: ByteArrayOutputStream? = null
        try {
            byteArrayOutputStream = ByteArrayOutputStream()
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
        if (byteArrayOutputStream != null) {
            try {
                byteArrayOutputStream.write(TlvTagConstant.GET_PROCESSING_OPTIONS)
                byteArrayOutputStream.write(
                    byteArrayOf(
                        GPO_P1,  // P1
                        GPO_P2,  // P2
                        pdolConstructed.size.toByte() // Lc
                    )
                )
                byteArrayOutputStream.write(pdolConstructed) // Data
                byteArrayOutputStream.write(
                    byteArrayOf(
                        0x00.toByte() // Le
                    )
                )
                byteArrayOutputStream.close()

                // Temporary result
                val tempResult = byteArrayOutputStream.toByteArray()
                /// - Temporary result
                if (isGpoCommand(tempResult)) {
                    result = tempResult
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
        return result
    }

    fun getReadTlvData(tlvTag: ByteArray?): ByteArray? {
        // Returning result
        var result: ByteArray? = null
        // - Returning result
        var byteArrayOutputStream: ByteArrayOutputStream? = null
        try {
            byteArrayOutputStream = ByteArrayOutputStream()
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
        if (byteArrayOutputStream != null) {
            try {
                byteArrayOutputStream.write(TlvTagConstant.GET_DATA)
                byteArrayOutputStream.write(tlvTag)
                byteArrayOutputStream.write(
                    byteArrayOf(
                        0x00.toByte() // Le
                    )
                )
                byteArrayOutputStream.close()
                result = byteArrayOutputStream.toByteArray()
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
        return result
    }

    fun verifyPin(pin: ByteArray?): ByteArray? {
        // Returning result
        var pin = pin
        var result: ByteArray? = null
        // - Returning result
        pin = byteArrayOf(
            0x24.toByte(),
            0x12.toByte(),
            0x34.toByte(),
            0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()
        )

        //00 20 00 80 08 24 12 34 FF FF FF FF FF
        var byteArrayOutputStream: ByteArrayOutputStream? = null
        try {
            byteArrayOutputStream = ByteArrayOutputStream()
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
        if (byteArrayOutputStream != null) {
            try {
                byteArrayOutputStream.write(TlvTagConstant.VERIFY)
                byteArrayOutputStream.write(
                    byteArrayOf(
                        0x00.toByte(),  // P1
                        0x80.toByte(),  // P2
                        pin.size.toByte() // Lc
                    )
                )
                byteArrayOutputStream.write(pin) // Pin
                byteArrayOutputStream.close()
                result = byteArrayOutputStream.toByteArray()
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
        return result
    }

    private fun isGpoCommand(commandApdu: ByteArray): Boolean {
        return commandApdu.size > 4 && commandApdu[0] == TlvTagConstant.GET_PROCESSING_OPTIONS[0] && commandApdu[1] == TlvTagConstant.GET_PROCESSING_OPTIONS[1] && commandApdu[2] == GPO_P1 && commandApdu[3] == GPO_P2
    }
}