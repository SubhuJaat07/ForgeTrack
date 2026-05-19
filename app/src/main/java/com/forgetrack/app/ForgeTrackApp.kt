package com.forgetrack.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.forgetrack.app.service.UpdateWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ForgeTrackApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()

        // Schedule background update checks - runs every 6 hours
        // This enables auto-update: user downloads APK once, future updates happen automatically
        UpdateWorker.schedule(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    UpdateWorker.CHANNEL_ID,
                    UpdateWorker.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications about ForgeTrack app updates"
                    enableVibration(true)
                }
            )

            val manager = getSystemService(NotificationManager::class.java)
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }
}
