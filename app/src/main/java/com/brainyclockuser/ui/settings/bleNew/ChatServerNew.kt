package com.brainyclockuser.ui.settings.bleNew

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import com.brainyclockuser.ui.settings.ble.MESSAGE_UUID
import com.brainyclockuser.ui.settings.ble.SCAN_SERVICE_UUID
import com.brainyclockuser.ui.settings.ble.SERVICE_UUID
import java.util.*

private fun BluetoothGattCharacteristic.isWriteable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

private fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
    return (properties and property) != 0
}
interface IChatServerActionListener2 {

    fun onPeripheralConnectDataReceived(data: String)

    fun onPeripheralCharactersticDiscovered()

}
interface IChatServerActionListener {

    fun onPeripheralConnectDataReceived(data: String)

    fun onPeripheralCharactersticDiscovered()

    fun onReceivedStatusMessage(data: String)
}

@SuppressLint("MissingPermission")
object ChatServerNew {
    var app: Application? = null

    var device: BluetoothDevice? = null
    var isDisconnected: Boolean = true
    private var gattServer: BluetoothGattServer? = null
    private lateinit var bluetoothGatt: BluetoothGatt
    private var characteristicForRead: BluetoothGattCharacteristic? = null
    var characteristicForWrite: BluetoothGattCharacteristic? = null
    private var characteristicForIndicate: BluetoothGattCharacteristic? = null
    private var connectedGatt: BluetoothGatt? = null

    private var chatServerListener: IChatServerActionListener? = null

    private val bluetoothManager: BluetoothManager by lazy {
        app?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val bleAdvertiser by lazy {
        bluetoothAdapter.bluetoothLeAdvertiser
    }
    private val charForIndicate
        get() = gattServer?.getService(SERVICE_UUID)
            ?.getCharacteristic(MESSAGE_UUID)


    fun setAppContext(app: Application) {
        this.app = app
    }

    fun setCurrentChatConnection(device: BluetoothDevice) {
        this.device = device

        connectDevice()
        Log.e("TAG", "setCurrentChatConnection: $device")
    }
    fun connectGatt(){
        device?.connectGatt(app!!.applicationContext,false, gattServerCallbackM)
        isDisconnected = false
    }
    fun disconnectGatt(){
        connectedGatt?.disconnect()
        isDisconnected = true
    }
    fun setConnectListerner(chatServerListener: IChatServerActionListener) {
        this.chatServerListener = chatServerListener
    }

    private val gattServerCallbackM = object : BluetoothGattCallback() {


        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)

            print(status)
        }

    }

    private fun setConnectedGattToNull() {
        connectedGatt = null
        characteristicForRead = null
        characteristicForWrite = null
        characteristicForIndicate = null
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val deviceAddress = gatt.device.address
            val deviceName = gatt.device.name
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.e("TAG", "Connected to $deviceAddress $deviceName")
                    Handler(Looper.getMainLooper()).post {
                        val disStatus = gatt.discoverServices()
                        Log.e("TAG", "$deviceName Discover Status: $disStatus")
                    }
                } else {
                    Log.e("TAG", "Disconnected from $deviceAddress")
                    setConnectedGattToNull()
                    gatt.close()
                }

            } else {
                Log.e(
                    "TAG",
                    "ERROR: onConnectionStateChange status=$status deviceAddress=$deviceAddress, disconnecting"
                )
            }

        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            print(value)
        }
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)


            print(value.toString())

        }


        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            print(characteristic)
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.e("TAG", "onServicesDiscovered services.count=${gatt.services.size} status=$status")

            if (status == 129) {
                Log.e("TAG", "ERROR: status=129 (GATT_INTERNAL_ERROR), disconnecting")
                gatt.disconnect()
                return
            }

            val service = gatt.getService(SCAN_SERVICE_UUID) ?: run {
                Log.e("TAG", "ERROR: Service not found $SCAN_SERVICE_UUID, disconnecting")
                gatt.disconnect()
                return
            }

            isDisconnected = false
            Log.e("TAG", "SUCCESS: Service is found ${service.toString()}, characteristics ${service.characteristics.size}")

            for(characteristic in service.characteristics) {
                if(characteristic.isWriteable()) {
                    connectedGatt = gatt
                    characteristicForWrite = characteristic
                    Log.e("TAG", "SUCCESS: Service ${service.toString()} of Characteristic ${characteristic.toString()} Found with Property ${characteristic.properties} Has Write Type ${characteristic.containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)}")
                    if(chatServerListener != null) {
                        chatServerListener?.onPeripheralCharactersticDiscovered()
                    }
                }
            }

        }

    }

    fun sendMessage(message: String) {
        Log.i("BLEConnection", "WriteCharacterStic ${characteristicForWrite != null}")
        if(characteristicForWrite != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Log.i("BLEConnection", "WriteValue $message")
                connectedGatt?.writeCharacteristic(characteristicForWrite!!, message.toByteArray(Charsets.UTF_8), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
            } else {
                Log.i("BLEConnection", "WriteValue $message")
                characteristicForWrite?.value = message.toByteArray(Charsets.UTF_8)
                characteristicForWrite?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                connectedGatt?.writeCharacteristic(characteristicForWrite)
            }
        }
    }

    fun connectDevice()
    {
//        var connect = device?.createBond()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.device?.connectGatt(app, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            this.device?.connectGatt(app, false, gattCallback)
        }

        Log.e("TAG","Connected: ${device?.name}")
    }

    fun permissionCheck() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            app?.startActivity(enableBtIntent)
            return
        }


        // Check low energy support
        if (!app?.packageManager?.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)!!) {
            // Get a newer device
            println("No LE Support.")
            return
        }

        // Check advertising
        if (!bluetoothAdapter.isMultipleAdvertisementSupported) {
            // Unable to run the server on this device, get a better device
            println("No Advertising Support.")
            return
        }
        @SuppressLint("HardwareIds")
        val deviceInfo = """${""" Device Info Name: ${bluetoothAdapter.name}""".trimIndent()}
            Address: ${bluetoothAdapter.address}  """.trimIndent()

        bleStartAdvertising()

        Log.e("TAG", "permissionCheck: Done ")

    }

    fun bleStartAdvertising() {
        bluetoothAdapter.name = "user"
        bleStartGattServer()
        bleAdvertiser.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
    }

    fun bleStopAdvertising() {
        bleStopGattServer()
        bleAdvertiser.stopAdvertising(advertiseCallback)
    }


    private val advertiseSettings = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
        .setConnectable(true)
        .setTimeout(0)
        .build()

    private val advertiseData = AdvertiseData.Builder()
        .setIncludeDeviceName(false) // don't include name, because if name size > 8 bytes, ADVERTISE_FAILED_DATA_TOO_LARGE
        .addServiceUuid(ParcelUuid(SERVICE_UUID))
        .build()

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.e("TAG", "Advertise start success\n$SERVICE_UUID")

        }

        override fun onStartFailure(errorCode: Int) {
            val desc = when (errorCode) {
                ADVERTISE_FAILED_DATA_TOO_LARGE -> "\nADVERTISE_FAILED_DATA_TOO_LARGE"
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "\nADVERTISE_FAILED_TOO_MANY_ADVERTISERS"
                ADVERTISE_FAILED_ALREADY_STARTED -> "\nADVERTISE_FAILED_ALREADY_STARTED"
                ADVERTISE_FAILED_INTERNAL_ERROR -> "\nADVERTISE_FAILED_INTERNAL_ERROR"
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "\nADVERTISE_FAILED_FEATURE_UNSUPPORTED"
                else -> ""
            }
            Log.e("TAG", "Advertise start failed: errorCode=$errorCode $desc")
//            isAdvertising = false
        }
    }

    private fun bleStartGattServer() {
        val gattServer = bluetoothManager.openGattServer(app, gattServerCallback)
        val service = BluetoothGattService(
            SERVICE_UUID,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
        val charForRead = BluetoothGattCharacteristic(
            MESSAGE_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        val charForWrite = BluetoothGattCharacteristic(
            MESSAGE_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        val charForWriteNoResponse = BluetoothGattCharacteristic(
            MESSAGE_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        val charForIndicate = BluetoothGattCharacteristic(
            MESSAGE_UUID,
            BluetoothGattCharacteristic.PROPERTY_INDICATE,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        val charForNotify = BluetoothGattCharacteristic(
            MESSAGE_UUID,
            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        val charConfigDescriptor = BluetoothGattDescriptor(
            MESSAGE_UUID,
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )
        charForIndicate.addDescriptor(charConfigDescriptor)

        service.addCharacteristic(charForRead)
        service.addCharacteristic(charForWrite)
        service.addCharacteristic(charForWriteNoResponse)
        service.addCharacteristic(charForIndicate)
        service.addCharacteristic(charForNotify)

        val result = gattServer.addService(service)

        this.gattServer = gattServer

    }

    private fun bleStopGattServer() {
        gattServer?.close()
        gattServer = null
        appendLog("gattServer closed")
    }

    private fun bleIndicate() {
        val text = "Data"
        val data = text.toByteArray(Charsets.UTF_8)
        charForIndicate?.let {
            it.value = data
            appendLog("sending indication \"$text\"")
            gattServer?.notifyCharacteristicChanged(device, it, true)
        }
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.e("TAG", "onConnectionStateChange: $device")
                appendLog("Central did connect  $device")

            } else {
                appendLog("Central did disconnect")
//                bleStopAdvertising()
            }
        }

        override fun onNotificationSent(device: BluetoothDevice, status: Int) {
            appendLog("onNotificationSent status=$status")
            Log.e("TAG", "onNotificationSent status=$status")
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            var log: String = "onCharacteristicRead offset=$offset"
            if (characteristic.uuid == MESSAGE_UUID) {
                val strValue = "101"
                gattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    strValue.toByteArray(Charsets.UTF_8)
                )
                log += "\nresponse=success, value=\"$strValue\""
                appendLog(log)
            } else {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                log += "\nresponse=failure, unknown UUID\n${characteristic.uuid}"
                appendLog(log)
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            var log: String =
                "onCharacteristicWrite offset=$offset responseNeeded=$responseNeeded preparedWrite=$preparedWrite"
            if (characteristic.uuid == MESSAGE_UUID) {
                val strValue = value?.toString(Charsets.UTF_8) ?: ""
                if (responseNeeded) {
                    gattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        strValue.toByteArray(Charsets.UTF_8)
                    )
                    log += "\nresponse=success, value=\"$strValue\""
                } else {
                    log += "\nresponse=notNeeded, value=\"$strValue\""
                }
//
                if(chatServerListener != null) {
                    chatServerListener?.onReceivedStatusMessage(strValue)
                }
            } else {
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                    log += "\nresponse=failure, unknown UUID\n${characteristic.uuid}"
                } else {
                    log += "\nresponse=notNeeded, unknown UUID\n${characteristic.uuid}"
                }
            }
            appendLog(log)
        }

        override fun onDescriptorReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            descriptor: BluetoothGattDescriptor
        ) {
            var log = "onDescriptorReadRequest"
            if (descriptor.uuid == MESSAGE_UUID) {
                val returnValue = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    returnValue
                )
            } else {
                log += " unknown uuid=${descriptor.uuid}"
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
            }
            appendLog(log)
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            descriptor: BluetoothGattDescriptor,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray
        ) {
            var strLog = "onDescriptorWriteRequest"
            Log.e("descriptor.uuid", "${descriptor.uuid}")
            if (descriptor.uuid == MESSAGE_UUID) {
                var status = BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED
                Log.e("Descriptor Char UUID ", "${descriptor.characteristic.uuid} // $MESSAGE_UUID")
                if (descriptor.characteristic.uuid == MESSAGE_UUID) {
                    if (Arrays.equals(value, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)) {
//                        subscribedDevices.add(device)
                        status = BluetoothGatt.GATT_SUCCESS
                        strLog += ", subscribed"
                    } else if (Arrays.equals(
                            value,
                            BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                        )
                    ) {
                        status = BluetoothGatt.GATT_SUCCESS
                        strLog += ", unsubscribed"
                    }
                }
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, status, 0, null)
                }
//                updateSubscribersUI()
            } else {
                strLog += " unknown uuid=${descriptor.uuid}"
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
                }
            }
            appendLog(strLog)
        }
    }

    fun appendLog(message: String) {
        Log.e("appendLog", message)

    }
}