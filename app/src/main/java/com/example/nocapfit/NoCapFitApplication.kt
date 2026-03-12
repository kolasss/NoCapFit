package com.example.nocapfit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.example.nocapfit.data.repository.TimerRepository
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NoCapFitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            TimerRepository.TIMER_CHANNEL_ID,
            "Rest Timer",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for workout rest timer"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
