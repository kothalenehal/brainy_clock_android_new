package com.brainyclockuser.ui.settings.ble
import java.util.*

/**
 * Constants for use in the Bluetooth LE Chat sample
 */
/**
 * UUID identified with this app - set as Service UUID for BLE Chat.
 *
 * Bluetooth requires a certain format for UUIDs associated with Services.
 * The official specification can be found here:
 * [://www.bluetooth.org/en-us/specification/assigned-numbers/service-discovery][https]
 */
//val SERVICE_UUID: UUID = UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb")
//val SERVICE_UUID: UUID = UUID.fromString("dcc2e754-6619-4eb3-86d4-6c8402df1863")
val SERVICE_UUID: UUID = UUID.fromString("dcc2e754-6619-4eb3-86d4-6c8402df1862")
val SCAN_SERVICE_UUID: UUID = UUID.fromString("dcc2e754-6619-4eb3-86d4-6c8402df1863")
/**
 * UUID for the message
 */
//val MESSAGE_UUID: UUID = UUID.fromString("7db3e235-3608-41f3-a03c-955fcbd2ea4b")
val MESSAGE_UUID: UUID = UUID.fromString("049c0650-d543-4636-9652-a0201913f2c8")

//val SERVICE_UUID: UUID = UUID.fromString("25AE1441-05D3-4C5B-8281-93D4E07420CF")
//val CHAR_FOR_READ_UUID: UUID = UUID.fromString("25AE1442-05D3-4C5B-8281-93D4E07420CF")
//val CHAR_FOR_WRITE_UUID: UUID = UUID.fromString("25AE1443-05D3-4C5B-8281-93D4E07420CF")
//val CHAR_FOR_INDICATE_UUID: UUID = UUID.fromString("25AE1444-05D3-4C5B-8281-93D4E07420CF")
//val CCC_DESCRIPTOR_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
/**
 * UUID to confirm device connection
 */
val CONFIRM_UUID: UUID = UUID.fromString("36d4dc5c-814b-4097-a5a6-b93b39085928")

const val REQUEST_ENABLE_BT = 1
