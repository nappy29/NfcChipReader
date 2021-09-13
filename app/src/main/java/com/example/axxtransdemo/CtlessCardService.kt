package com.example.axxtransdemo

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.TagLostException
import android.nfc.tech.IsoDep
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import com.example.axxtransdemo.data.model.*
import com.example.axxtransdemo.data.model.util.*
import java.io.IOException
import java.util.*

class CtlessCardService private constructor(): NfcAdapter.ReaderCallback{

    private val TAG = CtlessCardService::class.java.name

    private var mContext: Activity? = null
    private lateinit var mNfcAdapter: NfcAdapter
    private var mIsoDep: IsoDep? = null

    private var mResultListener: ResultListener? =
        null

    //  reader mode flags: listen for type A (not B), skipping ndef check
    private val READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or
            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS

    private val SW_NO_ERROR = 0x9000
    private val READ_TIMEOUT = 3000
    private val CONNECT_TIMEOUT = 30000
    private val mTimer: CountDownTimer? = null
//    private val mLogMessages: List<LogMessage>? = null
    private var mUserAppIndex = -1
    private var mCard: Card? = null

//    private fun CtlessCardService() {}

    constructor(context: Activity, resultListener: ResultListener) : this() {
        mContext = context
        mResultListener = resultListener
    }

//    fun CtlessCardService(context: Activity, resultListener: ResultListener) {
//        mContext = context
//        mResultListener = resultListener
//    }

    fun start() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext)
        // Check if the device has NFC
        if (mNfcAdapter == null) {
            Toast.makeText(mContext, "NFC not supported", Toast.LENGTH_LONG).show()
        }
        // Check if NFC is enabled on device
        if (!mNfcAdapter.isEnabled) {
            Toast.makeText(
                mContext, "Enable NFC before using the app",
                Toast.LENGTH_LONG
            ).show()
        } else {
            mNfcAdapter.enableReaderMode(mContext, this, READER_FLAGS, null)
        }
    }

    fun setSelectedApplication(index: Int) {
        mUserAppIndex = index
    }

    override fun onTagDiscovered(tag: Tag) {
//        mLogMessages = ArrayList<LogMessage>()
        mCard = Card()
        val tagList = tag.techList
        for (tagName in tagList) {
            Log.d(TAG, "TAG NAME : $tagName")
        }
        mIsoDep = IsoDep.get(tag)
        if (mIsoDep != null && mIsoDep!!.tag != null) {
            Log.d(TAG, "ISO-DEP - Compatible NFC tag discovered: " + mIsoDep!!.tag)
            mResultListener!!.onCardDetect()
            try {
                mIsoDep!!.connect()
                mIsoDep!!.timeout = READ_TIMEOUT
                var command: ByteArray = ApduUtil.selectPpse()!!
                var result = mIsoDep!!.transceive(command)
                var responseData: ByteArray? = evaluateResult("SELECT PPSE", command, result)
                var aid: ByteArray? = TlvUtil.getTlvByTag(responseData!!, TlvTagConstant.AID_TLV_TAG)
                aid = getAidFromMultiApplicationCard(responseData)
                if (aid == null) return
                if (!AidUtil.isApprovedAID(aid)) {
                    throw CommandException("AID NOT SUPPORTED -> " + HexUtil.bytesToHexadecimal(aid))
                }
                command = ApduUtil.selectApplication(aid)!!
                result = mIsoDep!!.transceive(command)
                responseData = evaluateResult("SELECT APPLICATION", command, result)
                var pdol: ByteArray? = TlvUtil.getTlvByTag(responseData!!, TlvTagConstant.PDOL_TLV_TAG)
                pdol = GpoUtil.generatePdol(pdol)
                command = ApduUtil.getProcessingOption(pdol!!)!!
                result = mIsoDep!!.transceive(command)
                responseData = evaluateResult("GET PROCESSING OPTION", command, result)
                val tag80: ByteArray? =
                    TlvUtil.getTlvByTag(responseData!!, TlvTagConstant.GPO_RMT1_TLV_TAG)
                val tag77: ByteArray =
                    TlvUtil.getTlvByTag(responseData, TlvTagConstant.GPO_RMT2_TLV_TAG)!!
                var aflData: ByteArray? = null
                if (tag77 != null) {
                    extractTrack2Data(tag77)
                    aflData = TlvUtil.getTlvByTag(responseData, TlvTagConstant.AFL_TLV_TAG)
                } else if (tag80 != null) {
                    aflData = tag80
                    aflData = Arrays.copyOfRange(aflData, 2, aflData.size)
                }
                if (aflData != null) {
                    Log.d(TAG, "AFL HEX DATA -> " + HexUtil.bytesToHexadecimal(aflData))
                    val aflDatas: List<AflObject> = AflUtil.getAflDataRecords(aflData) as List<AflObject>
                    if (aflDatas != null && !aflDatas.isEmpty() && aflDatas.size < 10) {
                        Log.d(TAG, "AFL DATA SIZE -> " + aflDatas.size)
                        for (aflObject in aflDatas) {
                            command = aflObject.readCommand!!
                            result = mIsoDep!!.transceive(command)
                            responseData = evaluateResult(
                                "READ RECORD (sfi: " + aflObject.sfi
                                    .toString() + " record: " + aflObject.recordNumber
                                    .toString() + ")", command, result
                            )
                            extractTrack2Data(responseData!!)
                        }
                    }
                } else {
                    Log.d(TAG, "AFL HEX DATA -> NULL")
                }

//                Log.e("bank_again?", mCard.toString())
                if (mCard!!.track2 != null) {
                    val cardType: CardType = AidUtil.getCardBrandByAID(aid)
                    mCard!!.cardType=cardType
                } else {
                    throw Exception("CALL YOUR BANK")
                }
                command = ApduUtil.getReadTlvData(TlvTagConstant.PIN_TRY_COUNTER_TLV_TAG)!!
                result = mIsoDep!!.transceive(command)
                evaluateResult("PIN TRY COUNT", command, result)
                command = ApduUtil.getReadTlvData(TlvTagConstant.ATC_TLV_TAG)!!
                result = mIsoDep!!.transceive(command)
                evaluateResult("APPLICATION TRANSACTION COUNTER", command, result, true)
            } catch (e: CommandException) {
                Log.d(TAG, "COMMAND EXCEPTION -> " + e.localizedMessage)
                mResultListener!!.onCardReadFail("COMMAND EXCEPTION -> " + e.localizedMessage)
            } catch (e: TagLostException) {
                Log.d(TAG, "ISO DEP TAG LOST ERROR -> " + e.localizedMessage)
                mResultListener!!.onCardMovedSoFast()
            } catch (e: IOException) {
                Log.d(TAG, "ISO DEP CONNECT ERROR -> " + e.localizedMessage)
                mResultListener!!.onCardReadFail("ISO DEP CONNECT ERROR -> " + e.localizedMessage)
            } catch (e: Exception) {
                Log.d(TAG, "CARD ERROR -> " + e.localizedMessage)
                mResultListener!!.onCardReadFail("CARD ERROR -> " + e.localizedMessage)
            }
            /*finally {
                mNfcAdapter.disableReaderMode(mContext);
            }*/
        } else {
            Log.d(TAG, "ISO DEP is null")
            mResultListener!!.onCardReadFail("ISO DEP is null")
        }
    }

    @Throws(IOException::class)
    private fun evaluateResult(commandName: String, command: ByteArray, result: ByteArray): ByteArray? {
        return evaluateResult(commandName, command, result, false)
    }

    @Throws(IOException::class)
    private fun evaluateResult(commandName: String, command: ByteArray, result: ByteArray, isLastCommand: Boolean): ByteArray? {
        val apduResponse = ApduResponse(result)
        returnMessage(commandName, HexUtil.bytesToHexadecimal(command)!!, HexUtil.bytesToHexadecimal(result)!!, isLastCommand)
        Log.d(
            TAG,
            commandName + " REQUEST : " + HexUtil.bytesToHexadecimal(command)
        )
        return if (apduResponse.isStatus(SW_NO_ERROR)) {
            Log.d(TAG, commandName + " RESULT : " + HexUtil.bytesToHexadecimal(result))
            apduResponse.data
        } else {
            val error = commandName + " ERROR : " + HexUtil.bytesToHexadecimal(result)
            Log.d(TAG, "COMMAND EXCEPTION -> $error")
            apduResponse.data
        }
    }

    @Throws(IOException::class)
    private fun returnMessage(commandName: String, request: String, response: String, isLastCommand: Boolean) {
        val reqMessage = request.replace("..".toRegex(), "$0 ")
        val respMessage = response.replace("..".toRegex(), "$0 ")
//        val logMessage = LogMessage(commandName, reqMessage, respMessage)
//        mLogMessages.add(logMessage)
        if (isLastCommand) {
//            mCard.setLogMessages(mLogMessages)
            mResultListener!!.onCardReadSuccess(mCard)
            mIsoDep!!.close()
            mNfcAdapter.disableReaderMode(mContext)
        }
    }

    private fun getAidFromMultiApplicationCard(responseData: ByteArray): ByteArray? {
        var aid: ByteArray? = null
        // *** FIND MULTIPLE APPLICATIONS ***//
        val appList: List<Application?> = TlvUtil.getApplicationList(responseData)
        if (appList.size > 1) {
            if (mUserAppIndex != -1 && appList.size > mUserAppIndex) {
                aid = appList[mUserAppIndex]!!.aid
                mUserAppIndex = -1
            } else {
                mResultListener!!.onCardSelectApplication(appList)
            }
        } else {
            aid = appList[0]!!.aid
        }
        return aid
    }

    private fun extractTrack2Data(responseData: ByteArray) {
        val track2 = TlvUtil.getTlvByTag(responseData, TlvTagConstant.TRACK2_TLV_TAG)
        val pan = TlvUtil.getTlvByTag(responseData, TlvTagConstant.APPLICATION_PAN_TLV_TAG)
        if (track2 != null && mCard!!.track2 == null) {
            mCard = CardUtil.getCardInfoFromTrack2(track2)
        }
        if (pan != null && mCard?.pan == null) {
            mCard!!.pan = HexUtil.bytesToHexadecimal(pan)
        }
    }

    interface ResultListener {
        fun onCardDetect()
        fun onCardReadSuccess(card: Card?)
        fun onCardReadFail(error: String?)
        fun onCardReadTimeout()
        fun onCardMovedSoFast()
        fun onCardSelectApplication(applications: List<Application?>?)
    }
}