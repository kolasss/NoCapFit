package com.example.nocapfit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.example.nocapfit.service.TimerNotifier
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NoCapFitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            TimerNotifier.TIMER_CHANNEL_ID,
            "Rest Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notifications for workout rest timer"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
