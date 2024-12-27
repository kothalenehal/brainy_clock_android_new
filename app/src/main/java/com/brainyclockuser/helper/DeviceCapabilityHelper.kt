package com.brainyclockuser.helper

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast

import androidx.biometric.BiometricManager
import com.brainyclockuser.geofencing.GeofenceBroadcastReceiver
import com.brainyclockuser.ui.clockin.ClockInFragment

object DeviceCapabilityHelper {

    fun checkBiometricSupport(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> return true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(context, "No biometric features available on this device.", Toast.LENGTH_SHORT).show()
                return false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(context, "Biometric not set up. Please enroll your biometric.", Toast.LENGTH_LONG).show()
                // Optionally, direct user to security settings
                context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                return false
            }
        }
        return false
    }
}