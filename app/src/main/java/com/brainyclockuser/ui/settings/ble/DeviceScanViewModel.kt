package com.brainyclockuser.ui.settings.ble

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


private const val TAG = "DeviceScanViewModel"

// 30 second scan period
private const val SCAN_PERIOD = 15000L

class DeviceScanViewModel(app: Application) : AndroidViewModel(app) {

    // LiveData for sending the view state to the DeviceScanFragment
    private val _viewState = MutableLiveData<DeviceScanViewState>()
    val viewState = _viewState as LiveData<DeviceScanViewState>

    // String key is the address of the bluetooth device
    private val scanResults = mutableMapOf<String, BluetoothDevice>()

    // BluetoothAdapter should never be null since BLE is required per
    // the <uses-feature> tag in the AndroidManifest.xml
    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    // This property will be null if bluetooth is not enabled
    private var scanner: BluetoothLeScanner? = null
    private var scanCallback: DeviceScanCallback? = null
    private var scanFilters: List<ScanFilter>? = null
    private var scanSettings: ScanSettings? = null
    private val bluetoothLeScanner = adapter.bluetoothLeScanner
    private var scanning = false
    private val handler = Handler()
//    private val SCAN_PERIOD: Long = 10000

    fun initBle() {
        // Setup scan filters and settings
        scanFilters = buildScanFilters()
        scanSettings = buildScanSettings()

        // Start a scan for BLE devices
        startScan()
    }

    override fun onCleared() {
        super.onCleared()
        stopScanning()
    }

    private fun startScan() {
        // If advertisement is not supported on this device then other devices will not be able to
        // discover and connect to it.
        if (!adapter.isMultipleAdvertisementSupported) {
            _viewState.value = DeviceScanViewState.AdvertisementNotSupported
            return
        }
        if (scanCallback == null) {
            scanner = adapter.bluetoothLeScanner
            Log.d(TAG, "Start Scanning")
            // Update the UI to indicate an active scan is starting
            _viewState.value = DeviceScanViewState.ActiveScan

            // Stop scanning after the scan period
            // Handler(Looper.getMainLooper()).postDelayed({ stopScanning() }, SCAN_PERIOD)
            if (!scanning) { // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    scanning = false
                    bluetoothLeScanner.stopScan(leScanCallback)
                }, SCAN_PERIOD)
                scanning = true
                // bluetoothLeScanner.stopScan(leScanCallback)
                bluetoothLeScanner.startScan(scanFilters, scanSettings, leScanCallback)
            } else {
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
            }

            // Kick off a new scan
              bluetoothLeScanner.stopScan(leScanCallback)
              bluetoothLeScanner.startScan(scanFilters, scanSettings, leScanCallback)
        } else {
            Log.d(TAG, "Already scanning")
        }
    }

    /*fun scanLeDevice() {
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }*/
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.d(TAG, "Scan Results found with $result callback type $callbackType")
            var device = result.device
            var name: String? = result.scanRecord?.deviceName ?: device?.name
            Log.e(TAG, "onScanResult: ${device.name} Name ${name}")
            if(name == null)
                name= "Tablet"

                scanResults[name!!] = device
                _viewState.value = DeviceScanViewState.ScanResults(scanResults)

//            result.device?.let { device ->
//                val deviceName: String
//                if (ActivityCompat.checkSelfPermission(
//                        getApplication(),
//                        Manifest.permission.BLUETOOTH_CONNECT
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return
//                }
//
//                if (device.name == null) {
//                    deviceName = "No Device Name  ${device.address}"
//                } else {
//                    deviceName = "${device.name} // ${device.address}"
//                    Log.e(TAG, "onScanResult: $deviceName ")
//                }
//                scanResults[deviceName] = device
//                Log.e(TAG, "Devices Found: ${scanResults.size} ")
//
////                Log.e(TAG, "onScanResult:444 ${adapter.name}")
//            }
//
//            _viewState.value = DeviceScanViewState.ScanResults(scanResults)
//            Log.e(TAG, "123 onScanResult: ${result.device.uuids}")

//            leDeviceListAdapter.addDevice(result.device)
//            leDeviceListAdapter.notifyDataSetChanged()
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(TAG, "onScanResult Failed: $errorCode ")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.e(TAG, "onBatchResultsFound: ${results?.size} ")
        }

    }

    fun stopScanning() {
        Log.d(TAG, "Stopping Scanning")
        scanner?.stopScan(leScanCallback)
//        leScanCallback = null
        // return the current results
        _viewState.value = DeviceScanViewState.ScanResults(scanResults)
    }

    /**
     * Return a List of [ScanFilter] objects to filter by Service UUID.
     */
    private fun buildScanFilters(): List<ScanFilter> {
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(SCAN_SERVICE_UUID))
            .build()
        return mutableListOf(filter)
    }

    /**
     * Return a [ScanSettings] object set to use low power (to preserve battery life).
     */
    private fun buildScanSettings(): ScanSettings {
        return ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            scanSettingsSinceM
//        } else {
//            scanSettingsBeforeM
//        }
    }

    private val scanSettingsBeforeM = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .setReportDelay(0)
        .build()

    @RequiresApi(Build.VERSION_CODES.M)
    private val scanSettingsSinceM = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
        .setReportDelay(0)
        .build()

    /**
     * Custom ScanCallback object - adds found devices to list on success, displays error on failure.
     */
    private inner class DeviceScanCallback : ScanCallback() {
        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            for (item in results) {
                item.device?.let { device ->
                    val deviceName: String
                    if (device.name == null) {
                        deviceName = "No Device Name ${device.address}"
                    } else {
                        deviceName = device.name
                    }
                    scanResults[deviceName] = device
                }
            }
            _viewState.value = DeviceScanViewState.ScanResults(scanResults)
        }

        override fun onScanResult(
            callbackType: Int,
            result: ScanResult
        ) {
            super.onScanResult(callbackType, result)
            result.device?.let { device ->
                val deviceName: String
                if (device.name == null) {
                    deviceName = "No Device Name ${device.address}"
                } else {
                    deviceName = device.name
                }
                scanResults[deviceName] = device
                Log.e(TAG, "onScanResult:11 ${device.address}")
            }
//            _viewState.value = DeviceScanViewState.ScanResults(scanResults)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            // Send error state to the fragment to display
            val errorMessage = "Scan failed with error: $errorCode"
            _viewState.value = DeviceScanViewState.Error(errorMessage)
        }

    }
}