package com.brainyclockuser.ui.settings.ble

import android.bluetooth.BluetoothDevice

sealed class DeviceConnectionState {
    class Connected(val device: BluetoothDevice) : DeviceConnectionState()
    object Disconnected : DeviceConnectionState()
}