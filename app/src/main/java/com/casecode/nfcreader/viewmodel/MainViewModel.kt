package com.casecode.nfcreader.viewmodel

import android.app.Application
import android.content.ContentValues
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.casecode.nfcreader.Coroutines
import com.casecode.nfcreader.NFCManager
import com.casecode.nfcreader.NFCStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.experimental.and

/**
 * ViewModel class for managing NFC (Near Field Communication) functionality and related data.
 * @param application The application context.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
        private const val prefix = "android.nfc.tech."
    }

    private val liveNFC: MutableStateFlow<NFCStatus?>
    private val liveToast: MutableSharedFlow<String?>
    private val liveTag: MutableStateFlow<String?>

    init {
        Log.d(TAG, "constructor")
        liveNFC = MutableStateFlow(null)
        liveToast = MutableSharedFlow()
        liveTag = MutableStateFlow(null)
    }

    /**
     * Updates the toast message.
     * @param message The message to be displayed in the toast.
     */
    private fun updateToast(message: String) {
        Coroutines.io(this@MainViewModel) {
            liveToast.emit(message)
        }
    }

    /**
     * Posts a toast message.
     * @param message The message to be posted.
     */
    private suspend fun postToast(message: String) {
        Log.d(TAG, "postToast(${message})")
        liveToast.emit(message)
    }

    /**
     * Observes the toast messages.
     * @return A shared flow of toast messages.
     */
    fun observeToast(): SharedFlow<String?> = liveToast.asSharedFlow()

    /**
     * Retrieves the NFC flags for configuring the NFC reader mode.
     * @return The NFC flags.
     */
    fun getNFCFlags(): Int {
        return NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or
                NfcAdapter.FLAG_READER_NFC_V or
                NfcAdapter.FLAG_READER_NFC_BARCODE //or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
    }

    /**
     * Retrieves the extras for configuring the NFC reader mode.
     * @return The extras bundle.
     */
    fun getExtras(): Bundle {
        val options = Bundle()
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 30000)
        return options
    }

    /**
     * Handles the NFC toggle switch.
     * @param isChecked Indicates whether NFC is checked (enabled) or not.
     */
    fun onCheckNFC(isChecked: Boolean) {
        Coroutines.io(this@MainViewModel) {
            Log.d(TAG, "onCheckNFC(${isChecked})")
            if (isChecked) {
                postNFCStatus(NFCStatus.Tap)
            } else {
                postNFCStatus(NFCStatus.NoOperation)
                postToast("NFC is Disabled, Please Toggle On!")
            }
        }
    }

    /**
     * Reads data from an NFC tag.
     * @param tag The NFC tag to be read.
     */
    fun readTag(tag: Tag?) {
        Coroutines.default(this@MainViewModel) {
            Log.d(TAG, "readTag(${tag} ${tag?.techList})")
            postNFCStatus(NFCStatus.Process)
            val stringBuilder: StringBuilder = StringBuilder()
            val id: ByteArray? = tag?.id
            stringBuilder.append("Tag ID (hex): ${getHex(id!!)} \n")
            stringBuilder.append("Tag ID (dec): ${getDec(id)} \n")
            stringBuilder.append("Tag ID (reversed): ${getReversed(id)} \n")
            stringBuilder.append("Technologies: ")
            tag.techList.forEach { tech ->
                stringBuilder.append(tech.substring(prefix.length))
                stringBuilder.append(", ")
            }
            stringBuilder.delete(stringBuilder.length - 2, stringBuilder.length)
            tag.techList.forEach { tech ->
                if (tech.equals(MifareClassic::class.java.name)) {
                    stringBuilder.append('\n')
                    val mifareTag: MifareClassic = MifareClassic.get(tag)
                    val type: String =
                        when (mifareTag.type) {
                            MifareClassic.TYPE_CLASSIC -> "Classic"
                            MifareClassic.TYPE_PLUS -> "Plus"
                            MifareClassic.TYPE_PRO -> "Pro"
                            else -> "Unknown"
                        }
                    stringBuilder.append("Mifare Classic type: $type \n")
                    stringBuilder.append("Mifare size: ${mifareTag.size} bytes \n")
                    stringBuilder.append("Mifare sectors: ${mifareTag.sectorCount} \n")
                    stringBuilder.append("Mifare blocks: ${mifareTag.blockCount}")
                }
                if (tech.equals(MifareUltralight::class.java.name)) {
                    stringBuilder.append('\n');
                    val mifareUlTag: MifareUltralight = MifareUltralight.get(tag);
                    val type: String =
                        when (mifareUlTag.type) {
                            MifareUltralight.TYPE_ULTRALIGHT -> "Ultralight"
                            MifareUltralight.TYPE_ULTRALIGHT_C -> "Ultralight C"
                            else -> "Unkown"
                        }
                    stringBuilder.append("Mifare Ultralight type: ");
                    stringBuilder.append(type)
                }
            }
            Log.d(TAG, "Datum: $stringBuilder")
            Log.d(ContentValues.TAG, "dumpTagData Return \n $stringBuilder")
            postNFCStatus(NFCStatus.Read)
            liveTag.emit("${getDateTimeNow()} \n $stringBuilder")
        }
    }

    /**
     * Updates the NFC status.
     * @param status The new NFC status.
     */
    fun updateNFCStatus(status: NFCStatus) {
        Coroutines.io(this@MainViewModel) {
            postNFCStatus(status)
        }
    }

    /**
     * Posts the NFC status.
     * @param status The NFC status to be posted.
     */
    private suspend fun postNFCStatus(status: NFCStatus) {
        Log.d(TAG, "postNFCStatus(${status})")
        if (NFCManager.isSupportedAndEnabled(getApplication())) {
            liveNFC.emit(status)
        } else if (NFCManager.isNotEnabled(getApplication())) {
            liveNFC.emit(NFCStatus.NotEnabled)
            postToast("Please Enable your NFC!")
            liveTag.emit("Please Enable your NFC!")
        } else if (NFCManager.isNotSupported(getApplication())) {
            liveNFC.emit(NFCStatus.NotSupported)
            postToast("NFC Not Supported!")
            liveTag.emit("NFC Not Supported!")
        }
        if (NFCManager.isSupportedAndEnabled(getApplication()) && status == NFCStatus.Tap) {
            liveTag.emit("Please Tap Now!")
        } else {
            liveTag.emit(null)
        }
    }

    /**
     * Observes the NFC status.
     * @return A state flow of NFC status.
     */
    fun observeNFCStatus(): StateFlow<NFCStatus?> = liveNFC.asStateFlow()

    /**
     * Retrieves the current date and time formatted string.
     * @return The current date and time formatted string.
     */
    private fun getDateTimeNow(): String {
        Log.d(TAG, "getDateTimeNow()")
        val timeFormat: DateFormat = SimpleDateFormat.getDateTimeInstance()
        val now = Date()
        Log.d(ContentValues.TAG, "getDateTimeNow() Return ${timeFormat.format(now)}")
        return timeFormat.format(now)
    }

    /**
     * Converts a byte array to a hexadecimal string.
     * @param bytes The byte array to be converted.
     * @return The hexadecimal representation of the byte array.
     */
    private fun getHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (i in bytes.indices.reversed()) {
            val b: Int = bytes[i].and(0xff.toByte()).toInt()
            if (b < 0x10) sb.append('0')
            sb.append(Integer.toHexString(b))
            if (i > 0)
                sb.append(" ")
        }
        return sb.toString()
    }

    /**
     * Converts a byte array to a decimal number.
     * @param bytes The byte array to be converted.
     * @return The decimal representation of the byte array.
     */
    private fun getDec(bytes: ByteArray): Long {
        Log.d(TAG, "getDec()")
        var result: Long = 0
        var factor: Long = 1
        for (i in bytes.indices) {
            val value: Long = bytes[i].and(0xffL.toByte()).toLong()
            result += value * factor
            factor *= 256L
        }
        return result
    }

    /**
     * Converts a byte array to a reversed decimal number.
     * @param bytes The byte array to be converted.
     * @return The reversed decimal representation of the byte array.
     */
    private fun getReversed(bytes: ByteArray): Long {
        Log.d(TAG, "getReversed()")
        var result: Long = 0
        var factor: Long = 1
        for (i in bytes.indices.reversed()) {
            val value = bytes[i].and(0xffL.toByte()).toLong()
            result += value * factor
            factor *= 256L
        }
        return result
    }

    /**
     * Observes the NFC tag.
     * @return A state flow of NFC tag information.
     */
    fun observeTag(): StateFlow<String?> {
        return liveTag.asStateFlow()
    }
}