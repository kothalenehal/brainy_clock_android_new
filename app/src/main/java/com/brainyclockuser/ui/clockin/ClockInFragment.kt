package com.brainyclockuser.ui.clockin

import AllStaffShift
import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.camera.core.ImageCapture
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.brainyclockuser.BrainyClockUserApp
import com.brainyclockuser.R
import com.brainyclockuser.base.BaseFragment
import com.brainyclockuser.databinding.DialogBluetoothConnectionBinding
import com.brainyclockuser.databinding.DialogRequestOvertimeBinding
import com.brainyclockuser.databinding.FragmentClockInBinding
import com.brainyclockuser.geofencing.GeofenceBroadcastReceiver
import com.brainyclockuser.geofencing.IGeofenceListner
import com.brainyclockuser.helper.DeviceCapabilityHelper
import com.brainyclockuser.service.NotificationService
import com.brainyclockuser.ui.MainActivity
import com.brainyclockuser.ui.settings.ble.DeviceConnectionState
import com.brainyclockuser.ui.settings.ble.Message
import com.brainyclockuser.ui.settings.bleNew.ChatServerNew
import com.brainyclockuser.ui.settings.bleNew.IChatServerActionListener
import com.brainyclockuser.ui.shifts.SiteEmployeeViewModel
import com.brainyclockuser.utils.AppConstant
import com.brainyclockuser.utils.OnItemClickListener
import com.brainyclockuser.utils.PrefUtils
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.QRResult.QRSuccess
import io.github.g00fy2.quickie.ScanCustomCode
import io.github.g00fy2.quickie.config.ScannerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ClockInFragment : BaseFragment() {
    private lateinit var binding: FragmentClockInBinding
    private lateinit var adapter: ClockInAdapter
    private val viewModel: ShiftViewModel by viewModels()
    private var shiftsList = ArrayList<ShiftsModels>()
    private var bleConnected = false
    private val notificationService = NotificationService.getInstance()
    var name: Message? = null
    private lateinit var device1: BluetoothDevice

    private lateinit var empEmail: String
    private lateinit var empName: String

    private var geoFenceEnterd = false

    private var shiftType: Int = -1

    private lateinit var shiftCheckTimer: Timer

    private lateinit var acknowledgementTimer: Timer

    ////Selfie Code
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>

    val scanCustomCode = registerForActivityResult(ScanCustomCode(), ::handleResult)

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private val viewModelSite: SiteEmployeeViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClockInBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authenticateFingerprint()
        shiftCheckTimer = Timer()
        acknowledgementTimer = Timer()

        empEmail = prefUtils.getStringData(requireActivity(), AppConstant.SharedPreferences.EMAIL)!!
        empName = prefUtils.getStringData(requireActivity(), AppConstant.SharedPreferences.NAME)!!
        Log.e("TAG", "checkEmployeeInTablet: CAll $empEmail")

        //to check the functionality of app as gps is not working properly will remove below 5 lines later
//        binding.btnUploadSelfie.visibility = View.VISIBLE
//        geoFenceEnterd = true
        viewModel.callShiftApi()
        setButtonActions()

        viewModel.loading.observe(viewLifecycleOwner) { if (it) showLoader() else hideLoader() }
        viewModel.throwable.observe(viewLifecycleOwner) { handleError(it) }
        viewModel.shifts.observe(viewLifecycleOwner) {
            Log.e("Size ", it.size.toString())
            var shiftID = 0
            if(it.isNotEmpty()) {
                shiftID = it[0].shiftId ?: 0
            }
            prefUtils.saveData(
                requireActivity(),
                AppConstant.SharedPreferences.SHIFT_ID,
                shiftID
            )

            setupRecyclerView(ArrayList<ShiftsModels>(it!! as ArrayList<ShiftsModels>))

            if(viewModelSite.allEmployees.value != null)  {
                displayWhosIn(viewModelSite.allEmployees.value!!)
            }

            updateUI(ArrayList(it))


        }

        viewModel.attendance.observe(viewLifecycleOwner) {
            if (it.success) {
                viewModel.callShiftApi()
                Log.e("attnfdg", "${it.message}")
            } else {
                Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
            }

        }

        viewModel.responseVerifyQRCode.observe(viewLifecycleOwner) {
            if (it.success) {
//                viewModel.markAttendance()
//                if (geoFenceEnterd && shiftsList.isNotEmpty() && shiftsList[0].clockIn.isNullOrEmpty()) {
//                    callMarkAttendance(shiftsList[0].shiftId!!, AppConstant.CLOCK_IN)
//                }

                if (DeviceCapabilityHelper.checkBiometricSupport(requireContext())) {
                    // Start BiometricAuthActivity for biometric authentication
//                    val intent = Intent(activity, BiometricAuthActivity::class.java)
//                    startBiometricAuth.launch(intent)
                    biometricPrompt.authenticate(promptInfo)
                } else {
                    Toast.makeText(
                        context,
                        "Please set up fingerprint authentication first.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } else {
                Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()

            }
        }


        binding.ivBluetooth.setOnClickListener { showBluetoothConnectionDialog() }
        binding.btnCheck.setOnClickListener {
//            if (name is Message.RemoteMessage) {
//                Log.e("TAG", "onViewCreated: Call")
//            }

            testConnection()

        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.btnUploadSelfie.setOnClickListener {
            startCamera()

        }

        takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {

                binding.btnUploadSelfie.visibility = View.GONE
                // Handle the captured image URI
                val data: Intent? = result.data
                // data?.data?.let { uri ->
                Toast.makeText(context, "Match Found", Toast.LENGTH_SHORT).show()
                if (!AppConstant.IsConnect) {
                    bleConnected = true
                    AppConstant.IsConnect = true
                    viewModel.callShiftApi()
                    // setButtonClicks()
                    setButtonActions()
                }
                //displayCapturedImage(uri)
                //}
            }
        }



        binding.btnUploadSelfie.visibility = View.GONE
        if (AppConstant.IsConnect) {
            bleConnected = true
            viewModel.callShiftApi()
            // setButtonClicks()
            setButtonActions()
        }

//        Log.e("TAG Call", "onViewCreated: Call")
//        ChatServer.connectionRequest.observe(viewLifecycleOwner, connectionRequestObserver)
//        ChatServer.deviceConnection.observe(viewLifecycleOwner, deviceConnectionObserver)

        var allowedType = //3
            PrefUtils().getIntData(
                BrainyClockUserApp.getAppComponent().provideApplication(),
                AppConstant.SharedPreferences.ApplicationType
            )
        Log.e("typeabc", "${allowedType}")

        if (allowedType == 5) {

        }
        if (allowedType == 3) {
            GeofenceBroadcastReceiver.setConnectListerner(object : IGeofenceListner {
                override fun onGeofenceEnter() {
                    Log.e("geoabc", "okk")
//                    binding.btnUploadSelfie.visibility = View.VISIBLE
                    geoFenceEnterd = true
                    binding.btnBeginYourShift.text =
                        activity?.resources?.getString(R.string.scan_qr)
                    setButtonActions()
                    Toast.makeText(
                        context,
                        activity?.resources?.getString(R.string.location_verified),
                        Toast.LENGTH_SHORT
                    ).show()

                    Log.e("tyrenkfd", "${geoFenceEnterd}//${shiftsList.isNotEmpty()}")
                    if (geoFenceEnterd && shiftsList.isNotEmpty() && shiftsList[0].clockIn.isNullOrEmpty()) {
                        updateUI(viewModel.shifts.value!! as ArrayList<ShiftsModels>)
                    }
                }

                override fun onGeofenceExit() {
                    Log.e("geoabcde", "okk")

                    binding.btnUploadSelfie.visibility = View.GONE
                    Toast.makeText(context, "Geofence Exit", Toast.LENGTH_SHORT).show()
                }
            })
        }

        binding.ivQrScanner.setOnClickListener { scan() }

        viewModelSite.callSiteEmployeeApi()

        viewModelSite.allEmployees.observe(viewLifecycleOwner) { displayWhosIn(it) }

    }

    private fun displayWhosIn(allStaffShifts: List<AllStaffShift>) {
        val clockedInCount = allStaffShifts.filter { value -> value.onlyClockedIn() }.size
        val clockedOutCount = allStaffShifts.filter { value -> value.getCustomStatus() == AllStaffShift.STATUS_CLOCKED_OUT }.size
        var absentCount = allStaffShifts.filter { value -> value.getCustomStatus() == AllStaffShift.STATUS_ABSENT }.size
        var lateArrivalCount = allStaffShifts.filter { value -> value.getCustomStatus() == AllStaffShift.STATUS_LATE }.size
        binding.whoClockedIn.text = "$clockedInCount clocked in";
        binding.whoClockedOut.text = "$clockedOutCount clocked out";

//        val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
//
//        val upcomingShifts = shiftsList
//        if(upcomingShifts.isNotEmpty()) {
//            absentCount = upcomingShifts.filter { upcomingShift ->
//                if(!upcomingShift.startDate.isNullOrBlank()) {
//                    val startDate = dateFormatter.parse(upcomingShift.startDate)
//                    val differenceInMilliseconds = Date().time - startDate.time
//                    val differenceInMinutes = differenceInMilliseconds / (1000 * 60)
//                    differenceInMinutes > AllStaffShift.ABSENT_BUFFER_IN_MIN
//                } else {
//                    false
//                }
//            }.size
//            lateArrivalCount = upcomingShifts.filter { upcomingShift ->
//                if(!upcomingShift.startDate.isNullOrBlank()) {
//                    val startDate = dateFormatter.parse(upcomingShift.startDate)
//                    val differenceInMilliseconds = Date().time - startDate.time
//                    val differenceInMinutes = differenceInMilliseconds / (1000 * 60)
//                    differenceInMinutes > AllStaffShift.LATE_BUFFER_IN_MIN && differenceInMinutes < AllStaffShift.ABSENT_BUFFER_IN_MIN
//                } else {
//                    false
//                }
//            }.size
//        }
        binding.whoAbsent.text = "$absentCount absent";
        binding.whoLateArrival.text = "$lateArrivalCount late arrivals";
    }

    private fun authenticateFingerprint() {
        val executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt =
            BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        requireContext(),
                        "Authentication error: $errString",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    if (geoFenceEnterd && shiftsList.isNotEmpty() && shiftsList[0].clockIn.isNullOrEmpty()) {
                        callMarkAttendance(shiftsList[0].shiftId!!, AppConstant.CLOCK_IN)
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT)
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

//        biometricPrompt.authenticate(promptInfo)
    }

    private fun scan() {

        scanCustomCode.launch(
            ScannerConfig.build {
//                setBarcodeFormats(listOf(BarcodeFormat.FORMAT_CODE_128)) // set interested barcode formats
                setOverlayStringRes(R.string.scan_barcode) // string resource used for the scanner overlay
                setOverlayDrawableRes(R.drawable.ic_scanner) // drawable resource used for the scanner overlay
                setHapticSuccessFeedback(false) // enable (default) or disable haptic feedback when a barcode was detected
                setShowTorchToggle(true) // show or hide (default) torch/flashlight toggle button
                setShowCloseButton(true) // show or hide (default) close button
                setHorizontalFrameRatio(2.2f) // set the horizontal overlay ratio (default is 1 / square frame)
                setUseFrontCamera(false) // use the front camera

            }
        )

    }

    fun handleResult(result: QRResult) {

        Log.e("reslt", "${result}")
        Log.e("reslt", "${result.toString()}")

        val text = when (result) {
            is QRSuccess -> {
                result.content.rawValue
                // decoding with default UTF-8 charset when rawValue is null will not result in meaningful output, demo purpose
                    ?: result.content.rawBytes?.let { String(it) }.orEmpty()
            }

            QRResult.QRUserCanceled -> "User canceled"
            QRResult.QRMissingPermission -> "Missing permission"
            is QRResult.QRError -> "${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}"
        }
        Log.e("texrsr", text)
        if (result is QRSuccess)
            verifyQRCode(text)

        /*  if ( geoFenceEnterd && shiftsList.isNotEmpty() && shiftsList[0].clockIn.isNullOrEmpty()) {
              verifyQRCode(result.toString())
              *//*callMarkAttendance(
                shiftsList[0].shiftId!!,
                AppConstant.LUNCH_IN
            )*//*
        }*/
    }

    private fun startCamera() {
        // Check camera permissions
        if (allPermissionsGranted()) {
            // Start the camera activity
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureLauncher.launch(intent)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(),
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        cameraExecutor.shutdown()
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    companion object {
        private const val TAG = "CameraActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private fun testConnection() {
        val jsonObject = JSONObject()
        val empID = prefUtils.getIntData(
            requireActivity(),
            AppConstant.SharedPreferences.EMPLOYEE_ID
        )
        Log.e("Employee ID to write", "$empID")
        jsonObject.put("empID", empID.toString())
        ChatServerNew.sendMessage(jsonObject.toString())
    }

//    override fun onStart() {
//        super.onStart()
////        ChatServer.connectionRequest.observe(viewLifecycleOwner, connectionRequestObserver)
////        ChatServer.deviceConnection.observe(viewLifecycleOwner, deviceConnectionObserver)
////        ChatServer.messages.observe(viewLifecycleOwner, messageObserver)
//    }

    private val connectionRequestObserver = Observer<BluetoothDevice> { device ->
        Log.e("---", "Connection request observer: have device $device")
//        ChatServer.setCurrentChatConnection(device)
    }

    private val deviceConnectionObserver = Observer<DeviceConnectionState> { state ->
        when (state) {
            is DeviceConnectionState.Connected -> {
                val device = state.device
                device1 = device
                bleConnected = true
                binding.bleConnected = true
//                Log.e("---", "Gatt connection observer: have device ${device.name}")

                checkEmployeeInTablet()
            }

            is DeviceConnectionState.Disconnected -> {
                bleConnected = false
                binding.bleConnected = false
                Log.e("---", "disconnected: ")
            }

            else -> {}
        }
    }

    private val messageObserver = Observer<Message> { message ->
        Log.e("---", "User Have message ${message.text}")
        Log.e("---", "User Have message ${message is Message.RemoteMessage}")
        if (message.text == "EOM") {
            Toast.makeText(
                requireContext(),
                "Something went Wrong Please Try Again",
                Toast.LENGTH_SHORT
            ).show()
            return@Observer
        }
        if (message is Message.RemoteMessage) {//receive message from tablet app
            Log.e("---", "this is remote message")
            name = message
            Toast.makeText(requireContext(), "User Verified", Toast.LENGTH_SHORT).show()
            if (message.text == prefUtils.getIntData(
                    requireActivity(),
                    AppConstant.SharedPreferences.EMPLOYEE_ID
                ).toString()
            ) {
//                viewModel.callShiftApi()
                // setButtonClicks()
            }
        } else {
            viewModel.callShiftApi()
        }
    }

    private fun setupRecyclerView(list: ArrayList<ShiftsModels>) {
        this.updateUI(list)
        adapter = ClockInAdapter(shiftsList, object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                Log.e("---", "onItemClick: $position")
                Log.e("---", "onItemClick: ${shiftsList[position].shiftId}")
//                 showOvertimeDialog(shiftsList[position].shiftId)

                Log.e("TAG>>>", "formatter: " + getCurrentTime())
                viewModel.markAttendance(
                    shiftsList[position].shiftId!!,
                    getCurrentTime(),
                    "timeIn",
                    shiftsList[position].eventId!!,
                    shiftsList[position].position,
                    shiftsList[position].locationId
                )

//                Log.e("TAG>>>", "onItemClick: "+ viewModel.postAttendanceModel.value!!.message)

//                if (viewModel.postAttendanceModel.value!!.success) {
//                    binding.btnTakeABreak.visibility = View.VISIBLE
//                    binding.btnClockOut.visibility = View.VISIBLE
//                }

            }
        })
        binding.rvShifts.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvShifts.adapter = adapter
        shiftCheckTimer.schedule(object : TimerTask() {
            override fun run() {
                runBlocking(Dispatchers.Main) { updateUI(ArrayList<ShiftsModels>(viewModel.shifts.value!! as ArrayList<ShiftsModels>)); }
            }
        }, 1000L * 60L, 1000L * 60L)
    }

    fun getCurrentTime(): String {
        val currentTime = LocalTime.now()
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        return currentTime.format(timeFormatter)
    }

    override fun onDestroyView() {
        if (shiftCheckTimer != null) {
            shiftCheckTimer.cancel()
        }
        super.onDestroyView()
    }

    private fun updateUI(list: ArrayList<ShiftsModels>) {
        binding.canBegin = false
        binding.nextShift = false
        binding.noUpcoming = false
        binding.clockedOut = false
        binding.lunchIn = false
        binding.resumeShift = false
        binding.lunchOver = false

        binding.tvHeader.visibility = View.VISIBLE
        binding.tvSubHeader.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        if (list.isEmpty()) {
            binding.tvHeader.text = getString(R.string.no_shift_for_today)
            binding.tvSubHeader.text = getString(R.string.see_you_tomorrow)
            return
        }
        var isAnyActive = false
        for (e in list) {
//            e.customStatus = getShiftStatus(e)
            Log.d("ClickIn", "${e.customStatus} $e")
            if (e.customStatus == AppConstant.ACTIVE_SHIFT) {
                isAnyActive = true
            }
        }
        //update all shift status if there is any active shift &
        //also check if the active shift has clocked in or not, if not clocked in yet show shift is upcoming
        if (isAnyActive) {
            for (e in list) {
                if (e.customStatus == AppConstant.UPCOMING_SHIFT)
                    e.customStatus = AppConstant.OTHER_SHIFT
                else if (e.customStatus == AppConstant.ACTIVE_SHIFT && !e.clockIn.isNullOrBlank())
                    e.customStatus = AppConstant.ACTIVE_SHIFT
                Log.d("ClickInFinal", "${e.customStatus} $e")
            }
        } else if (list.size > 1) {
            //if there is multiple shifts & all are upcoming shifts, we have to change status of all shifts as OTHER_SHIFT
            // and keep only 1st shift as UPCOMING_SHIFT
            val filter = list.filter { it.customStatus == AppConstant.UPCOMING_SHIFT }
            val isAllUpcoming =
                filter.size == list.size //if the size is same for both list it means all shifts are upcoming
            if (isAllUpcoming) {
                var firstShift = list.get(0)
                for (i in 1 until list.size) {
                    if (list[i].customStatus == AppConstant.UPCOMING_SHIFT && (firstShift.getClockedStatus() != AllStaffShift.STATUS_CLOCKED_OUT)) {
                        list[i].customStatus = AppConstant.OTHER_SHIFT
                    }
                }
                if(firstShift.getClockedStatus() == AllStaffShift.STATUS_CLOCKED_OUT) {
                    list.sortBy { it.getClockedStatus() }
                }
            }

        }

        list.sortBy { it.customStatus }

        shiftsList.clear()
        shiftsList.addAll(list)

        this.updateTitle()
        this.updateActions()
        /*
                if (geoFenceEnterd && shiftsList.isNotEmpty() && shiftsList[0].clockIn.isNullOrEmpty()) {
                    callMarkAttendance(shiftsList[0].shiftId!!, AppConstant.CLOCK_IN)
                }*/
    }

    private fun updateActions() {
        binding.clockedOut = false

        if(shiftsList.isNotEmpty()) {
            val upcomingShift = shiftsList[0]
            val status = upcomingShift.getClockedStatus()
            if(status == AllStaffShift.STATUS_PRESENT) {
                binding.clockedOut = true
                binding.lunchIn = true
            }
            if(status == AllStaffShift.STATUS_LUNCH_IN) {
                binding.clockedOut = true
                binding.resumeShift = true
            }
            if(status == AllStaffShift.STATUS_LUNCH_OUT) {
                binding.clockedOut = true
            }
        }
    }

    private fun updateTitle() {
        val title = getTitle2()
        binding.tvHeader.text = title.first
        binding.tvSubHeader.text = title.second
        if(title.third != null) {
            binding.progressBar.visibility = View.VISIBLE
            binding.progressBar.max = title.third!!.second.toInt()
            binding.progressBar.progress = title.third!!.first.toInt()
        }
        Log.e("Header ", title.first + " // " + title.second)
    }

    private fun getTitle2(): Triple<String, String, Pair<Long, Long>?> {
        var header = getString(R.string.no_shift_for_today)
        var subHeader = getString(R.string.see_you_tomorrow)
        var durationData: Pair<Long, Long>? = null
        if (shiftsList.isEmpty()) {
            return Triple(header, subHeader, null)
        }
        var firstShift = shiftsList[0]
        var secondShift: ShiftsModels? = null
        if(shiftsList.size > 1) {
            secondShift = shiftsList[1]
        }
        val dayShift = firstShift.getDayShift()
        val status = dayShift.getClockedStatus()
        val lateTimes = dayShift.lateTimes()
        val clockedInTimes = dayShift.getClockInTimes()
        if(lateTimes != null) {
            header = getString(R.string.you_running_late)
            subHeader = getString(
                R.string.for_your_shift_by_,
                lateTimes.first.toString(),
                lateTimes.second.toString()
            )
        }
        if(status == AllStaffShift.STATUS_PRESENT && clockedInTimes.second != null) {
            header = getString(R.string.currently_active)
            val shiftEndTime = clockedInTimes.second
            val shiftStartTime = clockedInTimes.first
            val diffInTimes = dayShift.diffInTimes(Date(),shiftEndTime!!)
            subHeader = getString(
                R.string.until_shift_ends,
                diffInTimes?.first.toString(),
                diffInTimes?.second.toString()
            )
//            val startTime = dayShift.getClockInOutTimes()?.first
            if(shiftStartTime != null && shiftEndTime != null) {
                val diffShiftTime = dayShift.diffInTimes(shiftStartTime, shiftEndTime)
                if(diffShiftTime != null && diffInTimes != null) {
                    if(diffInTimes.first >= 0 && diffInTimes.second >= 0) {
                        durationData = Pair(diffInTimes.first * 60 + diffInTimes.second, diffShiftTime.first * 60 + diffShiftTime.second)
                    } else {
                        subHeader = getString(
                            R.string.shift_over
                        )
                    }
                }
            }
        }
        if(status == AllStaffShift.STATUS_CLOCKED_OUT) {
            header = getString(R.string.thats_all_for_today)
            subHeader = getString(R.string.see_you_tomorrow)
            if(secondShift != null) {
                val secondShiftDayShift = secondShift.getDayShift()
                val secondShiftStatus = secondShiftDayShift.getClockedStatus()
                if(secondShiftStatus == AllStaffShift.STATUS_INITIAL) {
                    header = getString(R.string.next_shift_starts)
                    val diffInTimes = secondShiftDayShift.diffInTimes(Date(),secondShiftDayShift.getClockInTimes()?.first!!)
                    subHeader = getString(R.string.in_, diffInTimes?.first.toString(), if(diffInTimes?.second!! > 0) (diffInTimes?.second).toString() else "0")
                }
            }
        }
        if(status == AllStaffShift.STATUS_LUNCH_IN) {
            header = firstShift.shiftName!!
//            subHeader = getString(R.string.lunch_break)
            val shiftEndTime = clockedInTimes.second
            val shiftStartTime = clockedInTimes.first
            val diffInTimes = dayShift.diffInTimes(Date(),shiftEndTime!!)
            subHeader = getString(
                R.string.until_shift_ends,
                diffInTimes?.first.toString(),
                diffInTimes?.second.toString()
            )
            if(shiftStartTime != null && shiftEndTime != null) {
                val diffShiftTime = dayShift.diffInTimes(shiftStartTime, shiftEndTime)
                if(diffShiftTime != null && diffInTimes != null) {
                    if(diffInTimes.first >= 0 && diffInTimes.second >= 0) {
                        durationData = Pair(diffInTimes.first * 60 + diffInTimes.second, diffShiftTime.first * 60 + diffShiftTime.second)
                    }
                }
            }
        }
        if(status == AllStaffShift.STATUS_LUNCH_OUT) {
            header = firstShift.shiftName!!
//            subHeader = getString(R.string.lunch_over)
            val shiftEndTime = clockedInTimes.second
            val shiftStartTime = clockedInTimes.first
            val diffInTimes = dayShift.diffInTimes(Date(),shiftEndTime!!)
            subHeader = getString(
                R.string.until_shift_ends,
                diffInTimes?.first.toString(),
                diffInTimes?.second.toString()
            )
            if(shiftStartTime != null && shiftEndTime != null) {
                val diffShiftTime = dayShift.diffInTimes(shiftStartTime, shiftEndTime)
                if(diffShiftTime != null && diffInTimes != null) {
                    if(diffInTimes.first >= 0 && diffInTimes.second >= 0) {
                        durationData = Pair(diffInTimes.first * 60 + diffInTimes.second, diffShiftTime.first * 60 + diffShiftTime.second)
                    }
                }
            }
        }
        if(status == AllStaffShift.STATUS_INITIAL) {
            header = getString(R.string.upcoming_shift)
            val diffInTimes = dayShift.diffInTimes(Date(),firstShift.getDayShift()?.getClockInTimes()?.first!!)
            subHeader = getString(R.string.shift_starts_in, diffInTimes?.first.toString(), diffInTimes?.second.toString())
            if(diffInTimes != null) {
                val durationInMin = diffInTimes!!.first * 60L + diffInTimes!!.second
                if(durationInMin in 0..10) {
                    durationData = Pair(durationInMin, 10)
                }
                if(durationInMin < 0) {
                    subHeader = getString(R.string.you_running_late)
                }
            }

        }
        if(status == AllStaffShift.STATUS_ABSENT) {
            header = firstShift.shiftName!!
            subHeader = ""
        }
        return Triple(header, subHeader, durationData)
    }

    private fun activateButton(shiftModel: ShiftsModels) {

        binding.btnBeginYourShift.visibility = View.GONE
        binding.btnClockOut.visibility = View.GONE
        binding.btnResumeShift.visibility = View.GONE
        binding.btnTakeABreak.visibility = View.GONE

        /* val lateTime = getLateTime(shiftsList[0])

         //1. shift will start after some time (eg. 1 hour)

         if (lateTime.first >= AppConstant.BEGIN_SHIFT_BEFORE && lateTime.second == getString(R.string.minutes)) {
             if (shiftModel.clockIn.isNullOrBlank()) {
                 binding.canBegin = true
                 binding.btnBeginYourShift.visibility = View.VISIBLE

             } else if (shiftModel.clockOut.isNullOrBlank()) {
                 binding.canBegin = false
                 binding.clockedOut = true
                 binding.btnClockOut.visibility = View.VISIBLE

                 if (shiftModel.clockIn.isNullOrBlank()) {
                     binding.lunchIn = true
                     binding.btnTakeABreak.visibility = View.VISIBLE

                 } else if (shiftModel.clockOut.isNullOrBlank()) {
                     binding.lunchIn = false
                     binding.resumeShift = true
                     binding.btnResumeShift.visibility = View.VISIBLE
                 } else {
                     binding.lunchIn = false
                     binding.resumeShift = false

                 }
             }
         }*/

    }

    /* private fun getTitle2(): Pair<String, String> {

         if (shiftsList.isEmpty()) {
             return Pair("", "")
         }

         var header = ""
         var subHeader = ""

         Log.d("LOG", shiftsList[0].customStatus.toString())

         if (shiftsList[0].clockIn.isNullOrBlank() || shiftsList[0].customStatus == AppConstant.UPCOMING_SHIFT) {
             val lateTime = getLateTime(shiftsList[0])
             if (lateTime.first < 0) {
                 //1. shift will start after some time (eg. 1 hour)

                 if (lateTime.first >= AppConstant.BEGIN_SHIFT_BEFORE && lateTime.second == getString(
                         R.string.minutes
                     )
                 ) {

                     binding.canBegin = true
                     binding.nextShift = false
                 } else {
                     binding.canBegin = false
                     binding.nextShift = true
                 }
                 binding.nextShiftTime = "${abs(lateTime.first)} ${lateTime.second}"
                 header = getString(R.string.next_shift_starts)
                 subHeader = getString(R.string.in_, abs(lateTime.first).toString(), lateTime.second)

             } else {
                 //2. shift has been started, but user haven't clocked in yet
                 binding.canBegin = true
                 header = getString(R.string.you_running_late)
                 subHeader = getString(
                     R.string.for_your_shift_by_,
                     lateTime.first.toString(),
                     lateTime.second
                 )
             }
             activateButton(shiftsList[0])

         } else if (shiftsList[0].customStatus == AppConstant.ACTIVE_SHIFT*//* && (shiftsList[0].clockIn.isNullOrBlank() && shiftsList[0].clockOut.isNullOrBlank())*//*) {
            //possibility
            //1. user clocked in for shift
            if (shiftsList[0].clockIn.isNullOrBlank()) {
                binding.canBegin = true
            } else
                binding.clockedOut = true
            header = shiftsList[0].shiftName.toString()
            subHeader = getString(R.string.currently_active)

            if (!shiftsList[0].clockIn.isNullOrBlank() && !shiftsList[0].empLunchOut.isNullOrBlank()) {
                //3. user back to work after lunch break
                binding.lunchOver = true

                //user can request for overtime at this time
//                shiftsList[0].customStatus = AppConstant.ACTIVE_OVERTIME_SHIFT
//                adapter.notifyItemChanged(0)

            } else if (shiftsList[0].empLunchIn.isNullOrBlank()) {
                //user can go to lunch
                binding.lunchIn = true
            } else if (shiftsList[0].empLunchOut.isNullOrBlank()) {
                //2. user is in lunch break
                binding.resumeShift = true
                subHeader = getString(R.string.lunch_break)
            }
            activateButton(shiftsList[0])
        } else {
            //possibility
            //1. last shift clocked out - skip
            var allClockedOut = false
            for (s in shiftsList) {
                if (s.customStatus == AppConstant.CLOCKED_OUT_SHIFT && s.clockIn.isNullOrBlank())
                    allClockedOut = true
            }
            if (allClockedOut) {
                //2. all shift clocked out
                binding.noUpcoming = true
                header = getString(R.string.thats_all_for_today)
                subHeader = getString(R.string.see_you_tomorrow)
            } else {
                //3. there is no shift for today
                binding.noUpcoming = true
                header = getString(R.string.no_shift_for_today)
                subHeader = getString(R.string.see_you_tomorrow)
            }
        }

        return Pair(header, subHeader)
    }

    private fun getLateTime(shiftsModel: ShiftsModels): Pair<Long, String> {
        val currentTime = Calendar.getInstance(Locale.getDefault())
        val shiftClockIn = "${currentTime[Calendar.DAY_OF_MONTH]}-" +
                "${currentTime[Calendar.MONTH] + 1}-" +
                "${currentTime[Calendar.YEAR]} " +
                "${shiftsModel.shiftClockInTime}"

        val df = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        var different: Long = currentTime.time.time - df.parse(shiftClockIn)?.time!!

        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24

        different %= daysInMilli

        val elapsedHours: Long = different / hoursInMilli
        different %= hoursInMilli

        val elapsedMinutes: Long = different / minutesInMilli
        different %= minutesInMilli

        return if (elapsedHours == 0L)
            Pair(elapsedMinutes, getString(R.string.minutes))
        else Pair(elapsedHours, getString(R.string.hours))
    }

    private fun getShiftStatus(shiftsModel: ShiftsModels): Int {

        val currentTime = Calendar.getInstance(Locale.getDefault())

        val shiftClockIn = "${currentTime[Calendar.DAY_OF_MONTH]}-" +
                "${currentTime[Calendar.MONTH] + 1}-" +
                "${currentTime[Calendar.YEAR]} " +
                "${shiftsModel.shiftClockInTime}"


        val s = shiftsModel.shiftClockInTime!!.split(":")[0].toInt()
        val e = shiftsModel.shiftClockOutTime!!.split(":")[0].toInt()

        val shiftClockOut = if (s > e) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_MONTH, 1)
            "${cal[Calendar.DAY_OF_MONTH]}-" +
                    "${(cal[Calendar.MONTH]) + 1}-" +
                    "${cal[Calendar.YEAR]} " +
                    "${shiftsModel.shiftClockOutTime}"
        } else {
            "${currentTime[Calendar.DAY_OF_MONTH]}-" +
                    "${(currentTime[Calendar.MONTH]) + 1}-" +
                    "${currentTime[Calendar.YEAR]} " +
                    "${shiftsModel.shiftClockOutTime}"
        }
        val df = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

        if (currentTime.time.after(df.parse(shiftClockOut)))//clocked out
            return if (!shiftsModel.clockIn.isNullOrBlank() && !shiftsModel.clockOut.isNullOrBlank())//if not clocked in show as other
                AppConstant.CLOCKED_OUT_SHIFT
            else AppConstant.OTHER_SHIFT
        else {
            if (currentTime.time.before(df.parse(shiftClockIn))) {//upcoming
                return AppConstant.UPCOMING_SHIFT
            } else if (currentTime.time.after(df.parse(shiftClockIn))) {//ongoing
                return when (shiftsModel.overtimeRequestPending) {
                    "approved" -> AppConstant.ACTIVE_IN_OVERTIME_SHIFT
                    "pending" -> AppConstant.ACTIVE_OVERTIME_PENDING_SHIFT
                    "rejected" -> AppConstant.ACTIVE_OVERTIME_NA_SHIFT
                    else -> {
                        if (!shiftsModel.clockIn.isNullOrBlank() && !shiftsModel.clockOut.isNullOrBlank()) {
                            AppConstant.CLOCKED_OUT_SHIFT
                        } //may be user have clocked out early
                        else {
                            AppConstant.ACTIVE_SHIFT
                        }

                    }
                }
            }
        }
        return AppConstant.OTHER_SHIFT
    }*/

    private fun verifyQRCode(data: String) {

        viewModel.verifyQRCode(data)

    }

    private fun callMarkAttendance(id: Int, type: Int) {
        val currentTime = Calendar.getInstance(Locale.getDefault())
        viewModel.markAttendance(
            id,
            "${currentTime[Calendar.HOUR_OF_DAY]}:${currentTime[Calendar.MINUTE]}",
            type,
            empEmail
        )

        var msg = when (type) {
            AppConstant.CLOCK_IN -> "Clock In"
            AppConstant.CLOCK_OUT -> "Clock Out"
            AppConstant.LUNCH_IN -> "Lunch In"
            else -> "Lunch Out"
        }
        notificationService.showNotification(requireContext(), "You have been successfully $msg")
//        sendNotifications("You have been successfully $msg")
    }

    private fun scheduleAcknowledgementTimer() {
        Log.i("AcknowledgementTimer", "Started")
        acknowledgementTimer.cancel()

        acknowledgementTimer = Timer()

        acknowledgementTimer.schedule(object : TimerTask() {
            override fun run() {
                runBlocking(Dispatchers.Main) {
                    Log.i("AcknowledgementTimer", "Executed")
                    try {
                        acknowledgementTimer.cancel()
                    } catch (e: Exception) {
                        Log.d("Error", e.message.toString())
                    }
                    if (viewModel.loading.value == true) {
                        viewModel.loading.value = false
                    }
                }
            }
        }, 1000L * 7L, 1000L * 7L)

    }

    private fun setVerificationType(shiftTypeValue: Int) {
        if (ChatServerNew.isDisconnected) {
            ChatServerNew.connectDevice()
            val handler = Handler()

// Define the delay in milliseconds (e.g., 3000 milliseconds or 3 seconds)
            val delayMillis: Long = 500

// Post the function to be executed after the delay
            handler.postDelayed({
                setVerificationType(shiftTypeValue)
            }, delayMillis)


        } else {

            val jsonObject = JSONObject()
            val empID = prefUtils.getIntData(
                requireActivity(),
                AppConstant.SharedPreferences.EMPLOYEE_ID
            )
            Log.e("Employee ID to write", "$empID")
            if (empID != null) {
                shiftType = shiftTypeValue

                jsonObject.put("empID", "${empID}_${shiftType}")
//                jsonObject.put("shift", shiftType.toString())
                //jsonObject.put("empName", empName.toString())
                this.scheduleAcknowledgementTimer()
                // var string= jsonObject.toString()
                ChatServerNew.sendMessage(jsonObject.toString())
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun userIsUnderRadious(message: String) {

        if (!geoFenceEnterd) {

            geoFenceEnterd = true
            binding.btnBeginYourShift.text = activity?.resources?.getString(R.string.scan_qr)
            setButtonActions()
            Toast.makeText(
                context,
                activity?.resources?.getString(R.string.location_verified),
                Toast.LENGTH_SHORT
            ).show()

            Log.e("tyrenkfd", "${geoFenceEnterd}//${shiftsList.isNotEmpty()}")
            if (geoFenceEnterd && shiftsList.isNotEmpty() && shiftsList[0].clockIn.isNullOrEmpty()) {
                updateUI(viewModel.shifts.value!! as ArrayList<ShiftsModels>)
            }
        }
    }

    private fun setButtonActions() {

        binding.btnWhosIn.setOnClickListener {
            val mainAct = activity as? MainActivity
            mainAct?.switchToEmployeeTab()
        }

        Log.d("BrainyUser", "Bind Button Click")
        //now we are redirecting user to googlemap to see the route for office location
        binding.btnBeginYourShift.setOnClickListener {
            Log.e("btnclk", "ok")
            if (geoFenceEnterd) {
                Log.e("btnclkgeo", "ok1")
                scan()

            } else {
                Log.e("btnclknrml", "ok1")
                val geoUri = "http://maps.google.com/maps?q=loc:${
                    activity?.let { it1 ->
                        PrefUtils().getStringData(
                            it1, AppConstant.SharedPreferences.Latitude
                        )
                    }
                }, ${
                    activity?.let { it1 ->
                        PrefUtils().getStringData(
                            it1,
                            AppConstant.SharedPreferences.Longitude
                        )
                    }
                } ${
                    activity?.let { it1 ->
                        PrefUtils().getStringData(
                            it1,
                            AppConstant.SharedPreferences.OfficeLocationName
                        )
                    }
                }"

                var intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
                activity?.startActivity(intent)
            }
            /*            if (shiftsList.isNotEmpty()) { //&& shiftsList[0].clockIn.isNullOrBlank()
                Log.d("BrainyUser", "Button Click")
//                    progressDialog.show()
                viewModel.loading.value = true
                setVerificationType(AppConstant.CLOCK_IN)


//                     viewModel.markAttendance(
//                         shiftsList[0].currentShiftId!!,
//                         "${currentTime[Calendar.HOUR_OF_DAY]}:${currentTime[Calendar.MINUTE]}",
//                         AppConstant.CLOCK_IN,
//                         empEmail
//                     )
            }*/
        }


        binding.btnTakeABreak.setOnClickListener {
//                progressDialog.show()
//            Toast.makeText(requireContext(), "Please show your face to Tab.", Toast.LENGTH_SHORT).show()
//            binding.btnTakeABreak.isEnabled =false
//            val currentTime = Calendar.getInstance(Locale.getDefault())
//            ChatServerNew.permissionCheck()
//            viewModel.loading.value = true
            /*if (PrefUtils().getIntData(
                    BrainyClockUserApp.getAppComponent().provideApplication(),
                    AppConstant.SharedPreferences.ApplicationType
                ) == 3
            ) {
                callMarkAttendance(
                    shiftsList[0].shiftId!!,
                    AppConstant.LUNCH_IN
                )
            } else {
                setVerificationType(AppConstant.LUNCH_IN)
            }*/

            viewModel.markAttendance(
                shiftsList[0].shiftId!!,
                getCurrentTime(),
                "lunchIn",
                shiftsList[0].eventId!!,
                shiftsList[0].position,
                shiftsList[0].locationId,
            )

            binding.btnTakeABreak.visibility=View.GONE
            binding.btnClockOut.visibility=View.VISIBLE
            binding.btnResumeShift.visibility=View.VISIBLE
        }

        binding.btnClockOut.setOnClickListener {
//                progressDialog.show()
//            Toast.makeText(requireContext(), "Please show your face to Tab.", Toast.LENGTH_SHORT).show()
//            binding.btnClockOut.isEnabled =false
//            val currentTime = Calendar.getInstance(Locale.getDefault())
//            ChatServerNew.permissionCheck()
//            viewModel.loading.value = true
//            if (PrefUtils().getIntData(
//                    BrainyClockUserApp.getAppComponent().provideApplication(),
//                    AppConstant.SharedPreferences.ApplicationType
//                ) == 3
//            ) {
//                callMarkAttendance(
//                    shiftsList[0].shiftId!!,
//                    AppConstant.CLOCK_OUT
//                )
//            } else {
//                setVerificationType(AppConstant.CLOCK_OUT)
//            }

            viewModel.markAttendance(
                shiftsList[0].shiftId!!,
                getCurrentTime(),
                "timeOut",
                shiftsList[0].eventId!!,
                shiftsList[0].locationId
            )
            binding.btnTakeABreak.visibility = View.GONE
            binding.btnClockOut.visibility = View.GONE
            binding.btnResumeShift.visibility = View.GONE
        }

        binding.btnResumeShift.setOnClickListener {
//                progressDialog.show()
//            Toast.makeText(requireContext(), "Please show your face to Tab.", Toast.LENGTH_SHORT).show()
//            binding.btnResumeShift.isEnabled =false
//            ChatServerNew.permissionCheck()
//            val currentTime = Calendar.getInstance(Locale.getDefault())
//            viewModel.loading.value = true
//            if (PrefUtils().getIntData(
//                    BrainyClockUserApp.getAppComponent().provideApplication(),
//                    AppConstant.SharedPreferences.ApplicationType
//                ) == 3
//            ) {
//                callMarkAttendance(
//                    shiftsList[0].shiftId!!,
//                    AppConstant.LUNCH_OUT
//                )
//            } else {
//                setVerificationType(AppConstant.LUNCH_OUT)
//            }
            viewModel.markAttendance(
                shiftsList[0].shiftId!!,
                getCurrentTime(),
                "lunchOut",
                shiftsList[0].eventId!!,
                shiftsList[0].position,
                shiftsList[0].locationId
            )
            binding.btnClockOut.visibility = View.VISIBLE
            binding.btnTakeABreak.visibility = View.VISIBLE
            binding.btnResumeShift.visibility = View.GONE
        }

        viewModel.attendance.observe(viewLifecycleOwner) {
            Log.e("----", "setButtonActions: here")
            if (it.success) {
                Log.e("Attendance ", it.message.toString())
                //reload list
                /*bleConnected = false
                binding.bleConnected = false*/
                if (name is Message.RemoteMessage) {
                    Log.e("TAG", "onViewCreated: Call")
                    /*DeviceConnectionState.Disconnected
                    bleConnected = false
                    binding.bleConnected = false*/
                }
                viewModel.callShiftApi()
            } else showError(it.message)
        }

        viewModel.requestOvertime.observe(viewLifecycleOwner) {
            if (it.success) {
                viewModel.callShiftApi()
            } else showError(it.message)
        }

        ChatServerNew.setConnectListerner(object : IChatServerActionListener {
            override fun onReceivedStatusMessage(data: String) {
                Log.i("BLEConnection", data)
                if (data == "FACE_MATCHED") {//AppConstant.CLOCK_IN
                    ChatServerNew.disconnectGatt()
                    runBlocking(Dispatchers.Main) {
                        viewModel.loading.value = false
                        try {
                            acknowledgementTimer.cancel()
                        } catch (e: Exception) {
                            Log.d("Error", e.message.toString())
                        }
                    }
                    val lastShiftType = shiftType
                    shiftType = -1
                    if (lastShiftType == -1) {
                        return
                    }
                    runBlocking(Dispatchers.Main) {
                        callMarkAttendance(
                            shiftsList[0].shiftId!!,
                            lastShiftType
                        )
                    }
                } else {
                    showError("Face not Matched")
                }
            }

            override fun onPeripheralConnectDataReceived(data: String) {
                showError(data)
            }

            override fun onPeripheralCharactersticDiscovered() {

            }
        })

        viewModel.postAttendanceModel.observe(viewLifecycleOwner) {
            viewModel.callShiftApi()
        }

    }

    private fun checkEmployeeInTablet() {
        /*ChatServer.sendMessage(
            prefUtils.getIntData(
                requireActivity(),
                AppConstant.SharedPreferences.EMPLOYEE_ID
            ).toString()
        )*/
    }

    private fun showOvertimeDialog(shiftId: Int?) {
        val dialog = Dialog(requireActivity(), R.style.Theme_Dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val dbinding =
            DialogRequestOvertimeBinding.inflate(
                LayoutInflater.from(requireActivity()),
                ConstraintLayout(requireActivity()),
                false
            )
        dialog.setContentView(dbinding.root)
        dialog.setCancelable(true)

        dbinding.tvCancel.setOnClickListener { dialog.dismiss() }
        dbinding.tvSubmit.setOnClickListener {
            dialog.dismiss()
            viewModel.sendOvertimeRequest(shiftId!!)
        }
        dialog.show()

    }

    private fun showBluetoothConnectionDialog() {
        val dialog = Dialog(requireActivity(), R.style.Theme_Dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val dbinding =
            DialogBluetoothConnectionBinding.inflate(
                LayoutInflater.from(requireActivity()),
                ConstraintLayout(requireActivity()),
                false
            )
        dialog.setContentView(dbinding.root)
        dialog.setCancelable(true)

        dbinding.tvCancel.setOnClickListener { dialog.dismiss() }
        dbinding.tvPair.setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(ClockInFragmentDirections.actionClockInFragmentToBluetoothFragment2())
        }

        dialog.show()
    }

}