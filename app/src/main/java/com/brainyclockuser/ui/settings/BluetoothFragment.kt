package com.brainyclockuser.ui.settings

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.brainyclockuser.R
import com.brainyclockuser.base.BaseFragment
import com.brainyclockuser.databinding.FragmentBluetoothBinding
import com.brainyclockuser.ui.clockin.ClockInFragment
import com.brainyclockuser.ui.clockin.ClockInFragmentDirections
import com.brainyclockuser.ui.clockin.ShiftsModel
import com.brainyclockuser.ui.settings.ble.DeviceScanViewModel
import com.brainyclockuser.ui.settings.ble.DeviceScanViewState
import com.brainyclockuser.ui.settings.bleNew.ChatServerNew
import com.brainyclockuser.ui.settings.bleNew.IChatServerActionListener
import com.brainyclockuser.ui.settings.bleNew.IChatServerActionListener2
import com.brainyclockuser.utils.AppConstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask


class BluetoothFragment : BaseFragment() {

    private lateinit var binding: FragmentBluetoothBinding
    private lateinit var adapter: AvailableBluetoothAdapter

    private val viewModel: DeviceScanViewModel by viewModels()

    private lateinit var listView: ListView
    private val mDeviceList = ArrayList<String>()
    private var mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private var scanning = false
    private val handler = Handler()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000
    private var tabletFound: Boolean = false

    private lateinit var shiftCheckTimer: Timer
    private val bluetoothManager: BluetoothManager by lazy {
        requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        shiftCheckTimer = Timer()
        binding = FragmentBluetoothBinding.inflate(inflater)
        binding.btnTitle = getString(R.string.scan)

        //Timer for 30 seconds
        shiftCheckTimer.schedule(object : TimerTask() {
            override fun run() {
                if(!tabletFound) {
                    shiftCheckTimer.cancel()
                    requireActivity().runOnUiThread {
                        showError("Please make sure you are in radius of 5 meters of tablet device .")
                        if (binding.btnScan.text == getString(R.string.stop_scanning)) {
                            viewModel.stopScanning()
                        }
                    }
                }
            }
        }, 1000L * 30L)

        return binding.root
    }

    private val viewStateObserver = Observer<DeviceScanViewState> { state ->
        when (state) {
            is DeviceScanViewState.ActiveScan -> {
//                Log.e("TAG 11", "${state}: ")
                binding.btnTitle = getString(R.string.stop_scanning)
            }
            is DeviceScanViewState.ScanResults -> {
                binding.btnTitle = getString(R.string.scan)
//                Log.e("TAG 22", "${state.scanResults.values}: ")
                showResults(state.scanResults)
            }
            is DeviceScanViewState.Error -> {
                binding.btnTitle = getString(R.string.scan)
//                Log.e("TAG 33", "${state.message}: ")
                showError(state.message)
            }
            is DeviceScanViewState.AdvertisementNotSupported -> {
//                Log.e("TAG 44", "${state.exhaustive}: ")
                binding.btnTitle = getString(R.string.scan)
                showError("BLE advertising is not supported on this device")
            }
            else -> {

            }
        }.exhaustive
    }

    private val <T> T.exhaustive: T
        get() = this

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.ivRight.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.rvPairedDevices.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvAvailableDevices.layoutManager = LinearLayoutManager(requireActivity())

        adapter = AvailableBluetoothAdapter(object : SelectBluetoothDevice {
            override fun onBluetoothDeviceClick(position: Int, availableDevice: AvailableBluetoothDevice) {
                var device = availableDevice.device

                println("No Advertising Support.")
                val dialog = ProgressDialog.show(
                    requireContext(),
                    "Connecting Device",
                    "Please wait....",
                    true
                )
                ChatServerNew.setCurrentChatConnection(device)

//                shiftCheckTimer.cancel()

                dialog.setCancelable(false)
//                ChatServerNew.connectDevice()
                Handler(Looper.getMainLooper()).postDelayed({

                    dialog.dismiss()
                    AppConstant.IsConnect = true
                    findNavController().popBackStack()
                    /* Toast.makeText(requireActivity(), "connected to the device", Toast.LENGTH_SHORT)
                         .show()*/
                }, 1000)

            }
        })
        binding.rvAvailableDevices.adapter = adapter
        initBle()
    }


    private fun showResults(scanResults: Map<String, BluetoothDevice>) {
        binding.noDeviceFound = scanResults.isEmpty()
        if (scanResults.isNotEmpty()) {
//            val filteredList = scanResults.values.toList().filter { it.name!=null && it.name.toLowerCase()=="tablet" }
//            val filteredList = scanResults.values.toList()
            val filteredList = ArrayList<AvailableBluetoothDevice>()
            scanResults.forEach { entry ->
                filteredList.add(AvailableBluetoothDevice(entry.key, entry.value))
            }
            tabletFound = true
            adapter.updateItems(filteredList)
        }
        else{
            tabletFound = false
        }
    }

    private fun initBle() {
        checkPermissions()
        binding.btnScan.setOnClickListener {
            binding.noDeviceFound = false
            if (binding.btnScan.text == getString(R.string.scan)) {
                checkPermissions()
            } else if (binding.btnScan.text == getString(R.string.stop_scanning)) {
//                shiftCheckTimer.cancel()
                viewModel.stopScanning()
            }
        }
    }

    private var requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                viewModel.initBle()
//                ChatServer.startServer(requireActivity().application)
                viewModel.viewState.observe(viewLifecycleOwner, viewStateObserver)
            } else {
                showError("Need to turn on bluetooth to connect to the tablet device.")
                //deny
            }
        }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var anyDenied = false
            permissions.entries.forEach {
                if (!it.value) {
                    anyDenied = true
                }
                Log.d("test006", "${it.key} = ${it.value}")
            }
            if (!anyDenied)
                onPermissionGranted()
            else {
                showError("We need permissions to use bluetooth")
                findNavController().popBackStack()
            }
        }

    private fun checkPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                )
            )
        } else {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private fun onPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
            AlertDialog.Builder(requireActivity())
                .setTitle("")
                .setMessage("Bluetooth needs to access current location. Please turn on location")
                .setNegativeButton(
                    R.string.cancel
                ) { _, _ ->
                    findNavController().popBackStack()
                }
                .setPositiveButton(
                    "Go to Settings"
                ) { _, _ ->
                    findNavController().popBackStack()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    requestBluetooth.launch(intent)
                }
                .setCancelable(false)
                .show()
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
    }

    private fun checkGPSIsOpen(): Boolean {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager?
                ?: return false
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    // Run the chat server as long as the app is on screen
//    override fun onStart() {
//        super.onStart()
//        ChatServer.startServer(requireActivity().application)
//    }

//    override fun onStop() {
//        super.onStop()
//        ChatServer.stopServer()
//    }

    interface SelectBluetoothDevice {
        fun onBluetoothDeviceClick(position: Int, availableDevice: AvailableBluetoothDevice)
    }
}