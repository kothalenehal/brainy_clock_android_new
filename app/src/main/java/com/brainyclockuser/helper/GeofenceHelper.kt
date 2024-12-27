package com.brainyclockuser.helper

import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest

object GeofenceHelper {
    fun getGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }
}