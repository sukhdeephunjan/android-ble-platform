package com.example.blelib.bleCore

/**
 * Central registry of BLE Service and Characteristic UUIDs.
 *
 * Contains Bluetooth SIG standard UUIDs
 * and vendor-specific UUID placeholders.
 */
object Uuids {

    /* ----------------------------- */
    /* Standard GATT Services        */
    /* ----------------------------- */

    const val GENERIC_ACCESS =
        "00001800-0000-1000-8000-00805f9b34fb"

    const val GENERIC_ATTRIBUTE =
        "00001801-0000-1000-8000-00805f9b34fb"

    const val DEVICE_INFORMATION =
        "0000180A-0000-1000-8000-00805f9b34fb"

    const val BATTERY_SERVICE =
        "0000180F-0000-1000-8000-00805f9b34fb"

    const val HEART_RATE_SERVICE =
        "0000180D-0000-1000-8000-00805f9b34fb"


    /* ----------------------------- */
    /* Heart Rate Characteristics    */
    /* ----------------------------- */

    const val HEART_RATE_MEASUREMENT =
        "00002A37-0000-1000-8000-00805f9b34fb"

    const val BODY_SENSOR_LOCATION =
        "00002A38-0000-1000-8000-00805f9b34fb"

    const val HEART_RATE_CONTROL_POINT =
        "00002A39-0000-1000-8000-00805f9b34fb"


    /* ----------------------------- */
    /* Battery Characteristics       */
    /* ----------------------------- */

    const val BATTERY_LEVEL =
        "00002A19-0000-1000-8000-00805f9b34fb"


    /* ----------------------------- */
    /* Device Information            */
    /* ----------------------------- */

    const val MANUFACTURER_NAME =
        "00002A29-0000-1000-8000-00805f9b34fb"

    const val MODEL_NUMBER =
        "00002A24-0000-1000-8000-00805f9b34fb"

    const val SERIAL_NUMBER =
        "00002A25-0000-1000-8000-00805f9b34fb"


    /* ----------------------------- */
    /* Vendor / Custom Services      */
    /* ----------------------------- */

    // Example placeholder for NPH / custom sensors
    const val NPH_SENSOR_SERVICE =
        "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"

    const val NPH_MEASUREMENT =
        "YYYYYYYY-YYYY-YYYY-YYYY-YYYYYYYYYYYY"
}
