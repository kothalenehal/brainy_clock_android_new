package com.brainyclockuser.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.brainyclockuser.R
import com.brainyclockuser.ui.MainActivity

class NotificationService : JobService() {

    companion object {

        const val NOTIFICATION_CHANNEL_ID = "BrainyClockNotificationChannel"
        const val NOTIFICATION_ID = 1
        private var instance: NotificationService? = null


        @Synchronized
        fun getInstance(): NotificationService {
            if (instance == null) {
                instance = NotificationService()
            }
            return instance!!
        }
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        // Perform your periodic task here
//        showNotification()

        // Return true to indicate that the job is still running
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        // Return true to reschedule the job if it fails
        return true
    }

     public fun showNotification(context: Context,msg:String) {
        // Create an explicit intent for the main activity
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create a notification
        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Shift Notification")
            .setContentText("Your notification message here.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Create a notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Brainyclock App Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Show the notification
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
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
            notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }
}