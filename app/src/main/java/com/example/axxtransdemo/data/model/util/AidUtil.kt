package com.example.axxtransdemo.data.model.util

import com.example.axxtransdemo.data.model.CardType
import java.util.*

object AidUtil {
    /*
        Mastercard (PayPass)
        RID: A000000004
        PIX: 1010
        AID (Application Identifier): A0000000041010
     */
    private val A0000000041010 = byteArrayOf(
        0xA0.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x04.toByte(),
        0x10.toByte(),
        0x10.toByte()
    )

    /*
        Maestro (PayPass)
        RID: A000000004
        PIX: 3060
        AID (Application Identifier): A0000000043060
     */
    private val A0000000043060 = byteArrayOf(
        0xA0.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x04.toByte(),
        0x30.toByte(),
        0x60.toByte()
    )

    /*
        Visa (PayWave)
        RID: A000000003
        PIX: 1010
        AID (Application Identifier): A0000000031010
     */
    private val A0000000031010 = byteArrayOf(
        0xA0.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x03.toByte(),
        0x10.toByte(),
        0x10.toByte()
    )

    /*
        Visa Electron (PayWave)
        RID: A000000003
        PIX: 2010
        AID (Application Identifier): A0000000032010
     */
    private val A0000000032010 = byteArrayOf(
        0xA0.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x03.toByte(),
        0x20.toByte(),
        0x10.toByte()
    )

    /*
        American Express
        RID: A000000025
        PIX: 0104
        AID (Application Identifier): A0000000250104
    */
    private val A0000000250104 = byteArrayOf(
        0xA0.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x25.toByte(),
        0x01.toByte(),
        0x04.toByte()
    )

    /*
        TROY
        RID: A0000006
        PIX: 723010
        AID (Application Identifier): A0000006723010
    */
    private val A0000006723010 = byteArrayOf(
        0xA0.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x06.toByte(),
        0x72.toByte(),
        0x30.toByte(),
        0x10.toByte()
    )

    /*
    TROY
    RID: A0000006
    PIX: 723020
    AID (Application Identifier): A0000006723020
*/
    private val A0000006723020 = byteArrayOf(
        0xA0.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x06.toByte(),
        0x72.toByte(),
        0x30.toByte(),
        0x20.toByte()
    )

    fun isApprovedAID(aid: ByteArray?): Boolean {
        val shortAid = Arrays.copyOfRange(aid, 0, 7)
        return if (Arrays.equals(A0000000041010, shortAid)) true else if (Arrays.equals(
                A0000000043060, shortAid
            )
        ) true else if (Arrays.equals(A0000000031010, shortAid)) true else if (Arrays.equals(
                A0000000032010, shortAid
            )
        ) true else if (Arrays.equals(A0000000250104, shortAid)) true else if (Arrays.equals(
                A0000006723010, shortAid
            )
        ) true else if (Arrays.equals(A0000006723020, shortAid)) true else false
    }

    fun getCardBrandByAID(aid: ByteArray?): CardType {
        val shortAid = Arrays.copyOfRange(aid, 0, 7)
        return if (Arrays.equals(A0000000041010, shortAid)) CardType.MC else if (Arrays.equals(
                A0000000043060, shortAid
            )
        ) CardType.MC else if (Arrays.equals(
                A0000000031010, shortAid
            )
        ) CardType.VISA else if (Arrays.equals(
                A0000000032010, shortAid
            )
        ) CardType.VISA else if (Arrays.equals(
                A0000000250104, shortAid
            )
        ) CardType.AMEX else if (Arrays.equals(
                A0000006723010, shortAid
            )
        ) CardType.TROY else if (Arrays.equals(
                A0000006723020, shortAid
            )
        ) CardType.TROY else CardType.UNKNOWN
    }
}
