package com.example.axxtransdemo.data.model

import android.util.Log
import com.example.axxtransdemo.data.model.util.HexUtil
import com.example.axxtransdemo.data.model.util.TlvTagConstant
import okhttp3.internal.and
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.or

object GpoUtil {
    private val TAG = GpoUtil::class.java.name
    fun generatePdol(pdol: ByteArray?): ByteArray? {
        // Returning result
        var result: ByteArray? = null
        // - Returning result
        var pdolLength = 0
        val tlvObjectArrayList: MutableList<TlvObject> = ArrayList<TlvObject>()
        if (pdol != null) {
            var i = 0
            while (i < pdol.size) {
                var goNext = i
                var tlvTag = byteArrayOf(
                    pdol[goNext++]
                )
                if (tlvTag[0] and 0x1F == 0x1F) {
                    tlvTag = byteArrayOf(
                        tlvTag[0], pdol[goNext++]
                    )
                }
                val tlvObj = TlvObject(tlvTag, pdol[goNext])
                tlvObjectArrayList.add(tlvObj)
                i += tlvObj.tlvTag.size
                i++
            }
            for (tlvObject in tlvObjectArrayList) {
                pdolLength += tlvObject.tlvTagLength
            }
        }
        val byteArrayOutputStream = ByteArrayOutputStream()
        try {
            byteArrayOutputStream.write(
                byteArrayOf(
                    0x83.toByte(),
                    pdolLength.toByte()
                )
            )
            if (pdol != null) {
                for (tlvObject in tlvObjectArrayList) {
                    val generatePdolResult = ByteArray(tlvObject.tlvTagLength.toInt())
                    var resultValue: ByteArray? = null
                    val transactionDate = Date()

                    // TTQ (Terminal Transaction Qualifiers); 9F66; 4 Byte(s)
                    if (Arrays.equals(tlvObject.tlvTag, TlvTagConstant.TTQ_TLV_TAG)) {
                        Log.d(
                            TAG,
                            "Generate PDOL -> TTQ (Terminal Transaction Qualifiers); " + "9F66" + "; " + tlvObject.tlvTagLength + " Byte(s)"
                        )
                        val data = ByteArray(4)
                        data[0] =
                            data[0] or (1 shl 5) // Contactless EMV mode supported (bit index (in the example: "5") <= 7)
                        resultValue = Arrays.copyOf(data, data.size)
                        resultValue = byteArrayOf(
                            0x27.toByte(),
                            0x80.toByte(),
                            0x00.toByte(),
                            0x00.toByte()
                        )
                    } else if (tlvObject.tlvTag.contentEquals(TlvTagConstant.AMOUNT_AUTHORISED_TLV_TAG)
                    ) {
                        Log.d(
                            TAG,
                            "Generate PDOL -> Amount, Authorised (Numeric); " + "9F02" + "; " + tlvObject.tlvTagLength + " Byte(s)"
                        )

                        //resultValue = new byte[tlvObject.getTlvTagLength()];
                        resultValue = byteArrayOf(
                            0x00.toByte(),
                            0x00.toByte(),
                            0x00.toByte(),
                            0x00.toByte(),
                            0x00.toByte(),
                            0x01.toByte()
                        )
                    } else if (Arrays.equals(
                            tlvObject.tlvTag,
                            TlvTagConstant.AMOUNT_OTHER_TLV_TAG
                        )
                    ) {
                        Log.d(
                            TAG,
                            "Generate PDOL -> Amount, Other (Numeric); " + "9F03" + "; " + tlvObject.tlvTagLength + " Byte(s)"
                        )
                        resultValue = ByteArray(tlvObject.tlvTagLength.toInt())

                        /*resultValue = new byte[]{
                                (byte) 0x00,
                                (byte) 0x00,
                                (byte) 0x00,
                                (byte) 0x00,
                                (byte) 0x00,
                                (byte) 0x00
                        };*/
                    } else if (tlvObject.tlvTag.contentEquals(TlvTagConstant.TERMINAL_COUNTRY_CODE_TLV_TAG)
                    ) {
                        Log.d(
                            TAG,
                            "Generate PDOL -> Terminal Country Code; " + "9F1A" + "; " + tlvObject.tlvTagLength + " Byte(s)"
                        )
                        resultValue = byteArrayOf(
                            0x07.toByte(),
                            0x92.toByte()
                        )

                        // https://en.wikipedia.org/wiki/ISO_3166-1

                        // Example: Turkey: 792 (Hexadecimal representation: 0792); Reference: https://en.wikipedia.org/wiki/ISO_3166-1
                    } else if (Arrays.equals(
                            tlvObject.tlvTag,
                            TlvTagConstant.TRANSACTION_CURRENCY_CODE_TLV_TAG
                        )
                    ) {
                        Log.d(
                            TAG,
                            "Generate PDOL -> Transaction Currency Code; " + "5F2A" + "; " + tlvObject.tlvTagLength + " Byte(s)"
                        )
                        resultValue = byteArrayOf(
                            0x09.toByte(),
                            0x40.toByte()
                        )

                        // https://en.wikipedia.org/wiki/ISO_4217

                        // Example: Turkish Lira (TRY; Turkish Lira): 949 (Hexadecimal representation: 0949)
                    } else if (Arrays.equals(tlvObject.tlvTag, TlvTagConstant.TVR_TLV_TAG)) {
                        Log.d(
                            TAG,
                            "Generate PDOL -> TVR (Transaction Verification Results); " + "95" + "; " + tlvObject.tlvTagLength + " Byte(s)"
                        )
                        resultValue = ByteArray(tlvObject.tlvTagLength.toInt())

                        /*resultValue = new byte[]{
                                (byte) 0x00,
                                (byte) 0x00,
                                (byte) 0x00,
                                (byte) 0x00,
                                (byte) 0x01
                        };*/
                    } else if (Arrays.equals(
                            tlvObject.tlvTag,
                            TlvTagConstant.TRANSACTION_DATE_TLV_TAG
                        )
                    ) {
                        Log.d(
                            TAG,
                            "Generate PDOL -> Transaction Date; " + "9A" + "; " + tlvObject.tlvTagLength + " Byte(s)"
                        )
                        var simpleDateFormat: SimpleDateFormat? = null
                        try {
                            simpleDateFormat = SimpleDateFormat(
                                "yyMMdd",
                                Locale.getDefault()
                            ) // Format: Year, Month in year, Day in month
                        } catch (e: Exception) {
                            Log.e(TAG, e.toString())
                        }
                        if (simpleDateFormat != null) {
                            var dateFormat: String? = null
                            try {
                                dateFormat = simpleDateFormat.format(transactionDate)
                            } catch (e: Exception) {
                                Log.e(TAG, e.toString())
                            }
                            if (dateFormat != null) {
                                resultValue = HexUtil.hexadecimalToBytes(dateFormat)
                            }
                        }
                    } else if (Arrays.equals(
                            tlvObject.tlvTag,
                            TlvTagConstant.TRANSACTION_TYPE_TLV_TAG
                        )
                    ) {
                        Log.d(
                            TAG,
                            "Generate PDOL -> Transaction Type; " + "9C" + "; " + tlvObject.tlvTagLength + " Byte(s)"
                        )
                        resultValue = byteArrayOf(
                            0x00.toByte()
                        )
                    } else if (Arrays.equals(
                            tlvObject.tlvTag,
                            TlvTagConstant.TRANSACTION_TIME_TLV_TAG
                        )
                    ) {
                        Log.d(
                            TAG,
                            "Generate PDOL -> Transaction Date; " + "9F21" + "; " + tlvObject.tlvTagLength + " Byte(s)"
                        )

                        // "SimpleDateFormat" Reference: https://developer.android.com/reference/java/text/SimpleDateFormat.html
                        var simpleDateFormat: SimpleDateFormat? = null
                        try {
                            simpleDateFormat = SimpleDateFormat(
                                "HHmmss",
                                Locale.getDefault()
                            ) // Format: Hour in day (0-23), Minute in hour, Second in minute
                        } catch (e: Exception) {
                            Log.e(TAG, e.toString())
                        }
                        if (simpleDateFormat != null) {
                            var dateFormat: String? = null
                            try {
                                dateFormat = simpleDateFormat.format(transactionDate)
                            } catch (e: Exception) {
                                Log.e(TAG, e.toString())
                            }
                            if (dateFormat != null) {
                                resultValue = HexUtil.hexadecimalToBytes(dateFormat)
                            }
                        }
                    } else if (Arrays.equals(tlvObject.tlvTag, TlvTagConstant.TERMINAL_TYPE)) {
                        Log.d(
                            TAG,
                            "Generate PDOL -> Terminal Type; " + "9F35" + "; " + tlvObject.tlvTagLength + " Byte(s)"
                        )
                        resultValue = byteArrayOf(
                            0x21.toByte()
                        )
                    } else if (Arrays.equals(tlvObject.tlvTag, TlvTagConstant.UN_TLV_TAG)) {
                        Log.d(
                            TAG,
                            "Generate PDOL -> UN (Unpredictable Number); " + "9F37" + "; " + tlvObject.tlvTagLength + " Byte(s)"
                        )

                        // Generate random unpredictable number
                        var unSecureRandom: SecureRandom? = null
                        try {
                            unSecureRandom = SecureRandom()
                        } catch (e: Exception) {
                            Log.e(TAG, e.toString())
                        }
                        if (unSecureRandom != null) {
                            try {
                                unSecureRandom.nextBytes(generatePdolResult)
                            } catch (e: Exception) {
                                Log.e(TAG, e.toString())
                            }
                        }
                        // - Generate random unpredictable number
                    }
                    // - UN (Unpredictable Number); 9F37, 1 or 4 Byte(s)
                    if (resultValue != null) {
                        try {
                            System.arraycopy(
                                resultValue,
                                0,
                                generatePdolResult,
                                0,
                                Math.min(resultValue.size, generatePdolResult.size)
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, e.toString())
                        }
                    }
                    byteArrayOutputStream.write(generatePdolResult) // Data
                }
            }
            byteArrayOutputStream.close()
            result = byteArrayOutputStream.toByteArray()
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
        return result
    }
}