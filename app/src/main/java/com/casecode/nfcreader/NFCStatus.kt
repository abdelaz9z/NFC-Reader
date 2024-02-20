package com.casecode.nfcreader

/**
 * Enum class representing different NFC (Near Field Communication) statuses.
 */
enum class NFCStatus {
    /**
     * No NFC operation is currently ongoing.
     */
    NoOperation,

    /**
     * NFC tag has been tapped.
     */
    Tap,

    /**
     * NFC process is ongoing.
     */
    Process,

    /**
     * NFC confirmation is required.
     */
    Confirmation,

    /**
     * NFC tag is being read.
     */
    Read,

    /**
     * NFC tag is being written to.
     */
    Write,

    /**
     * NFC is not supported on the device.
     */
    NotSupported,

    /**
     * NFC is not enabled on the device.
     */
    NotEnabled,
}