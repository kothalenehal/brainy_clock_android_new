package com.brainyclockuser.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.brainyclockuser.BrainyClockUserApp
import com.brainyclockuser.utils.PrefUtils


class UninstallReceiver : BroadcastReceiver() {
    var prefUtils: PrefUtils = BrainyClockUserApp.getAppComponent().providePrefUtil()
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_PACKAGE_REMOVED == intent.action) {
            val packageName = intent.data!!.schemeSpecificPart
            if (context.packageName == packageName) {
                // Uninstallation of this app detected, clear SharedPreferences
                clearSharedPreferences(context)
            }
        }
    }

    private fun clearSharedPreferences(context: Context) {
        prefUtils.clearData(context)
    }
}