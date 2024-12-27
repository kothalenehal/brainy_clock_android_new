package com.brainyclockuser.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.brainyclockuser.BrainyClockUserApp
import com.brainyclockuser.R
import com.brainyclockuser.base.BaseActivity
import com.brainyclockuser.base.model.BaseModel
import com.brainyclockuser.databinding.ActivityMainBinding
import com.brainyclockuser.geofencing.GeofenceBroadcastReceiver
import com.brainyclockuser.geofencing.IGeofenceListner
import com.brainyclockuser.service.NotificationService
import com.brainyclockuser.ui.clockin.EmployeeDetailsFragment
import com.brainyclockuser.ui.clockin.MainClockInFragment
import com.brainyclockuser.ui.settings.MainSettingsFragment
import com.brainyclockuser.ui.settings.bleNew.ChatServerNew
import com.brainyclockuser.ui.shifts.MainShiftsFragment
import com.brainyclockuser.utils.AppConstant
import com.brainyclockuser.utils.PrefUtils
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus


@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    val notification: MutableLiveData<BaseModel> = MutableLiveData()

    private var isLocationPermissionGranted = false
    private var isLocationPermissionGranted1 = false
    private var isLocationPermissionGranted2 = false
    private var isLocationPermissionGranted3 = false
    private var isBluetoothPermissionGranted = false
    private var isBluetoothPermissionGranted1 = false

    private lateinit var listener: IGeofenceListner


    val REQUEST_CODE_BLUETOOTH = 1
    val REQUEST_CODE_BLUETOOTH2 = 5
    val REQUEST_CDOE_LOCATION = 6
    val REQUEST_CDOE_LOCATION1 = 6
    val REQUEST_CDOE_LOCATION2 = 7
    val REQUEST_CDOE_LOCATION3 = 8
    val REQUEST_CODE = 123 // Use any unique request code

    private val GEOFENCEACTION = "com.google.android.gms.location.Geofence.ACTION_RECEIVE_GEOFENCE"

    private val geofenceBroadcastReceiver = GeofenceBroadcastReceiver()
    private lateinit var locationCallback: LocationCallback
    private val RADIUS = 100  // Radius in meters

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = GEOFENCEACTION
        // addGeofences() and removeGeofences().
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    fun permissionCheck(perms: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, perms) == PackageManager.PERMISSION_GRANTED
    }

    //    @RequiresApi(Build.VERSION_CODES.S)
    val permission = arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION
    )

    val locationPermission = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private fun requestPermission() {

        var permisionRequest: MutableList<String> = ArrayList()

        isBluetoothPermissionGranted = permissionCheck(permission[0])
        if (!isBluetoothPermissionGranted) {
            permisionRequest.add(permission[0])
        }
        isBluetoothPermissionGranted1 = permissionCheck(permission[1])
        if (!isBluetoothPermissionGranted1) {
            permisionRequest.add(permission[1])
        }
        isLocationPermissionGranted = permissionCheck(locationPermission[0])
        if (!isLocationPermissionGranted) {
            permisionRequest.add(locationPermission[0])
        }
        isLocationPermissionGranted1 = permissionCheck(locationPermission[1])
        if (!isLocationPermissionGranted1) {
            permisionRequest.add(locationPermission[1])
        } else {
            isLocationPermissionGranted2 = permissionCheck(permission[4])
            if (!isLocationPermissionGranted2) {
                Toast.makeText(
                    this@MainActivity,
                    "You need to allow location permission all the time",
                    Toast.LENGTH_SHORT
                ).show()
                permisionRequest.add(permission[4])
            }
        }

        isLocationPermissionGranted3 = permissionCheck(permission[5])
        if (!isLocationPermissionGranted3) {
            permisionRequest.add(permission[5])
        }

        if (permisionRequest.isNotEmpty())
            permissionLauncher.launch(permisionRequest.toTypedArray())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.e("TAG", "onRequestPermissionsResult: ${grantResults}")
    }

    fun requestDeviceLocationSettings() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(true)
            .setMinUpdateIntervalMillis(100)
            .setMaxUpdateDelayMillis(4000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            startApp()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        this@MainActivity,
                        100
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.

                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100) {
            startApp()
        }
        if (requestCode == 200) {
            requestDeviceLocationSettings()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionData ->
                isBluetoothPermissionGranted =
                    permissionData[permission[0]] ?: isBluetoothPermissionGranted
                isBluetoothPermissionGranted1 =
                    permissionData[permission[1]] ?: isBluetoothPermissionGranted1
                isLocationPermissionGranted =
                    permissionData[permission[2]] ?: isLocationPermissionGranted
                isLocationPermissionGranted1 =
                    permissionData[permission[3]] ?: isLocationPermissionGranted1
                isLocationPermissionGranted2 =
                    permissionData[permission[4]] ?: isLocationPermissionGranted2
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    isLocationPermissionGranted3 =
                        permissionData[permission[5]] ?: isLocationPermissionGranted3
                } else isLocationPermissionGranted3 = true

                if (isLocationPermissionGranted && isLocationPermissionGranted1 && isLocationPermissionGranted2 && isLocationPermissionGranted3) {
                    val bluetoothManager: BluetoothManager =
                        getSystemService(BluetoothManager::class.java)
                    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()
                    if (bluetoothAdapter?.isEnabled == false) {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

                        if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
//                            return
                            requestPermission()
                        }
                        startActivityForResult(enableBtIntent, 200)
                    } else {
                        requestDeviceLocationSettings()
                    }
                } else {
                    requestPermission()
                }
            }

        geofencingClient = LocationServices.getGeofencingClient(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        Log.e("mainact", "ok")
        requestPermission()
//        startApp()


        //sendNotifications("fdsfdsfsd")
    }

    private fun sendNotifications(msg: String) {

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create a notification
        val notificationBuilder = NotificationCompat.Builder(
            applicationContext,
            NotificationService.NOTIFICATION_CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Create a notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationService.NOTIFICATION_CHANNEL_ID,
                "Brainyclock App Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Show the notification
        with(NotificationManagerCompat.from(applicationContext)) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(NotificationService.NOTIFICATION_ID, notificationBuilder.build())
        }

    }

    private fun setupLocationCallback(userLat: Double, userLong: Double) {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    Log.e("callbckloc", "${location.latitude}" + "//" + "${location.longitude}")
                    Log.e("callbcklocuser", "${userLat}" + "//" + "${userLong}")
                    val distance23 =
                        distance(location.latitude, location.longitude, userLat, userLong)
//                  val distance = calculateDistance(location.latitude, location.longitude,userLat,userLong)

                    val locationA = Location("point A")
                    locationA.latitude = userLat // Latitude of Point A

                    locationA.longitude = userLong // Longitude of Point A

                    val locationB = Location("point B")
                    locationB.latitude = location.latitude // Latitude of Point B

                    locationB.longitude = location.longitude // Longitude of Point B

                    val distance1 = locationA.distanceTo(locationB)
                    Log.e("loptre23", "${distance23}")
                    Log.e("loptre", "${distance1}")
                    val results = FloatArray(3)
                    Location.distanceBetween(
                        userLat,
                        userLong,
                        location.latitude,
                        location.longitude,
                        results
                    )
                    val distance = results[0]

                    if (distance < RADIUS) {
//                  Toast.makeText(this@MainActivity,"You are within 100m radius from the point!",Toast.LENGTH_SHORT).show()
                        EventBus.getDefault().post("Location")
                    } else {
//                        Toast.makeText(this@MainActivity,"You are out of range!",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
//        EventBus.getDefault().register(this)

    }


    private fun getLocationAndCheckRadius() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                111
            )
            return
        }
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000  // Update location every 8 seconds
            fastestInterval = 3000  // Update location at most every 3 seconds
            priority =
                LocationRequest.PRIORITY_HIGH_ACCURACY  // Use GPS and networks for high accuracy
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationRequest?.let {
                fusedLocationClient.requestLocationUpdates(
                    it,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }
    }

    private fun calculateDistance(
        lat: Double,
        lon: Double,
        userLat: Double,
        userLong: Double
    ): Float {
        val result = FloatArray(1)
        Location.distanceBetween(lat, lon, userLat, userLong, result)
        return result[0]
    }


    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val theta = lon1 - lon2
        var dist = (Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + (Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta))))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist = dist * 60 * 1.1515
        return dist
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }

    @SuppressLint("MissingPermission")
    fun startApp() {
        var allowedType = //3
            PrefUtils().getIntData(
                BrainyClockUserApp.getAppComponent().provideApplication(),
                AppConstant.SharedPreferences.ApplicationType
            )
        Log.e("usrtype", "${allowedType}")
        if (allowedType == 3) {
//37.4220936//-122.083922
//          var latitude = "37.4220936"
            var latitude = //"25.6985"
                prefUtils.getStringData(
                    this,
                    AppConstant.SharedPreferences.Latitude
                )
//          var longitude = "-122.083922"
            var longitude = //"83.6055"
                prefUtils.getStringData(
                    this,
                    AppConstant.SharedPreferences.Longitude
                )

            var geoRadious =
                prefUtils.getIntData(this, AppConstant.SharedPreferences.GeofenceRadious) ?: 100
            Log.e("Long", "${longitude}")
            Log.e("Lat", "${latitude}//${geoRadious}")
            if (latitude != null) {
                if (longitude != null) {
                    setupLocationCallback(latitude.toDouble(), longitude.toDouble())
                }
            }
            startLocationUpdates()

            val geofence = Geofence.Builder()
                .setRequestId("simple_geofence123")
                .setCircularRegion(
                    latitude!!.toDouble(),
                    longitude!!.toDouble(),
                    geoRadious!!.toFloat()
                ) // Lat, Long, Radius in meters
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(0)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()

            // Create a GeofencingRequest
            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            val pendingIntent = geofencePendingIntent

            geofencingClient.addGeofences(geofencingRequest, pendingIntent)?.run {
                addOnSuccessListener {
                    // Geofences added successfully
                    val filter = IntentFilter(GEOFENCEACTION)
                    if (Build.VERSION.SDK_INT >= 34 && getApplicationInfo().targetSdkVersion >= 34) {
                        Log.e("sdkint34", "ok")
                        registerReceiver(
                            geofenceBroadcastReceiver,
                            filter,
                            Context.RECEIVER_EXPORTED
                        )
                    } else {
                        Log.e("registrelse", "ok")
                        registerReceiver(geofenceBroadcastReceiver, filter)
                    }
//                    registerReceiver(geofenceBroadcastReceiver, filter)
//                    Toast.makeText(applicationContext, "success", Toast.LENGTH_SHORT).show()

                }
                addOnFailureListener {
                    it.printStackTrace()
                    // Failed to add geofences
                    Toast.makeText(
                        applicationContext,
                        "Failed to fetch Location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        } else if (!AppConstant.IsConnect) {
//            showError("Connect to tablet device first")
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        //new


        binding.bottomNavigationView.itemIconTintList = null

        val applicationType =
            prefUtils.getIntData(this, AppConstant.SharedPreferences.ApplicationType)
        binding.bottomNavigationView.menu.getItem(2)
            .setVisible(applicationType == AppConstant.EMPLOYEE_LIST_USER_TYPE)

        if (applicationType == 5) {
            binding.bottomNavigationView.menu.getItem(0)
                .setIcon(getDrawable(R.drawable.menu_schedule_state))
            binding.bottomNavigationView.menu.getItem(0).setTitle(getString(R.string.schedule))
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_shifts -> {
//                    throw RuntimeException("Test Crash")
                    replaceFragment(MainShiftsFragment(), R.id.flFragment, false)
                }

                R.id.menu_clockin -> {
                    replaceFragment(MainClockInFragment(), R.id.flFragment, false)
                }

                R.id.menu_settings -> {
                    replaceFragment(MainSettingsFragment(), R.id.flFragment, false)
                }

                R.id.menu_employees -> {
                    // Handle menu_employees fragment if the tab is shown
                    replaceFragment(EmployeeDetailsFragment(), R.id.flFragment, false)
                }
            }
            true
        }

        binding.bottomNavigationView.selectedItemId = R.id.menu_shifts

        ChatServerNew.setAppContext(this.application)

        ChatServerNew.bleStartAdvertising()
    }

    public fun switchToEmployeeTab() {
        binding.bottomNavigationView.selectedItemId = R.id.menu_employees;
    }


    override fun onDestroy() {
        super.onDestroy()

        try {
            unregisterReceiver(geofenceBroadcastReceiver)
        } catch (e: Exception) {

        }
    }
}


