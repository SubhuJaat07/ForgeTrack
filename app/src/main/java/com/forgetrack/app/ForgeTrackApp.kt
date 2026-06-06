package com.forgetrack.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import com.forgetrack.app.data.local.UserPreferences
import com.forgetrack.app.service.UpdateService
import com.forgetrack.app.service.UpdateWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ForgeTrackApp : Application() {

    @Inject lateinit var userPreferences: UserPreferences

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()

        // Initialize version tracking so update check doesn't show false positives
        val updateService = UpdateService(this, userPreferences)
        CoroutineScope(Dispatchers.IO).launch {
            updateService.initializeVersionTracking()
        }

        // Schedule background update checks - runs every 6 hours
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
