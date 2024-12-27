package com.brainyclockuser.geofencing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    companion object {
        private  var instance: GeofenceBroadcastReceiver ?=null
        var  iGeofencingListner: IGeofenceListner? = null
        fun get(): GeofenceBroadcastReceiver {
            return instance!!
        }

        fun setConnectListerner(iGeofenceListner: IGeofenceListner) {
            iGeofencingListner=iGeofenceListner
        }
    }
    init {
        if (instance == null) {
            instance = this
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) }
        Log.e("geoferenceReceiver","okk")
        if (geofencingEvent == null) {
            Log.e("GeofenceReceiver", "GeofencingEvent is null")
            return
        }

        if (geofencingEvent.hasError()) {
            Log.e("GeofenceReceiver", "Error: ${geofencingEvent.errorCode}")
            return
        }

        val triggeringGeofences = geofencingEvent.triggeringGeofences
        if (triggeringGeofences == null || triggeringGeofences.isEmpty()) {
            Log.e("GeofenceReceiver", "No triggering geofences or triggeringGeofences is null")
            return
        }

        for (geofence in triggeringGeofences) {
            val requestId = geofence.requestId
            Log.d("GeofenceReceiver", "Triggering geofence: $requestId")
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        Log.e("geofencing","$geofenceTransition")
        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {

                if(iGeofencingListner != null) {
                    iGeofencingListner?.onGeofenceEnter()
                }
                Log.d("GeofenceReceiver", "Entered geofence")
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.d("GeofenceReceiver", "Exited geofence")
                if(iGeofencingListner != null) {
                    iGeofencingListner?.onGeofenceExit()
                }
            }
            else -> {
                Log.d("GeofenceReceiver", "Unknown transition")
            }
        }
    }

}
interface IGeofenceListner {

    fun onGeofenceEnter()

    fun onGeofenceExit()


}