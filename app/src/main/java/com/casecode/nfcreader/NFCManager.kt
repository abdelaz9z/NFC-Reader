package com.casecode.nfcreader

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log

/**
 * Utility object for managing NFC (Near Field Communication) functionality.
 */
object NFCManager {
    private val TAG = NFCManager::class.java.simpleName

    /**
     * Enables the NFC reader mode.
     * @param context The context in which NFC functionality is enabled.
     * @param activity The activity where NFC reader mode is enabled.
     * @param callback The callback to handle NFC events.
     * @param flags Flags to configure the reader mode.
     * @param extras Additional parameters for configuring the reader mode.
     */
    fun enableReaderMode(
        context: Context,
        activity: Activity,
        callback: NfcAdapter.ReaderCallback,
        flags: Int,
        extras: Bundle
    ) {
        try {
            NfcAdapter.getDefaultAdapter(context)
                .enableReaderMode(activity, callback, flags, extras)
        } catch (ex: UnsupportedOperationException) {
            Log.e(TAG, context.getString(R.string.unsupportedoperationexception, ex.message), ex)
        }
    }

    /**
     * Disables the NFC reader mode.
     * @param context The context in which NFC functionality is disabled.
     * @param activity The activity where NFC reader mode is disabled.
     */
    fun disableReaderMode(context: Context, activity: Activity) {
        try {
            NfcAdapter.getDefaultAdapter(context).disableReaderMode(activity)
        } catch (ex: UnsupportedOperationException) {
            Log.e(TAG, context.getString(R.string.unsupportedoperationexception, ex.message), ex)
        }
    }

    /**
     * Checks if NFC is supported on the device.
     * @param context The context to check NFC support.
     * @return `true` if NFC is supported, `false` otherwise.
     */
    fun isNotSupported(context: Context): Boolean {
        return !isSupported(context)
    }

    /**
     * Checks if NFC is enabled on the device.
     * @param context The context to check NFC enablement.
     * @return `true` if NFC is enabled, `false` otherwise.
     */
    fun isNotEnabled(context: Context): Boolean {
        return !isEnabled(context)
    }

    /**
     * Checks if NFC is both supported and enabled on the device.
     * @param context The context to check NFC support and enablement.
     * @return `true` if NFC is both supported and enabled, `false` otherwise.
     */
    fun isSupportedAndEnabled(context: Context): Boolean {
        return isSupported(context) && isEnabled(context)
    }

    private fun isSupported(context: Context): Boolean {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        return nfcAdapter != null
    }

    private fun isEnabled(context: Context): Boolean {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        return nfcAdapter?.isEnabled ?: false
    }
}