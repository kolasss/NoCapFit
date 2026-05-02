package dev.kolas.nocapfit.service

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.kolas.nocapfit.R
import dev.kolas.nocapfit.data.preferences.ThemePreferences
import dev.kolas.nocapfit.util.VIBRATION_DURATION_MS
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val themePreferences: ThemePreferences,
    private val ringtonePlayer: RingtonePlayer
) {
    suspend fun notifyCompletion() {
        playSound()
        vibrate()
        postCompletionNotification()
    }

    private suspend fun playSound() {
        ringtonePlayer.play(themePreferences.notificationSoundUri.first())
    }

    private fun vibrate() {
        try {
            val vibrator = context.getSystemService(Vibrator::class.java)
            vibrator?.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) { }
    }

    private fun postCompletionNotification() {
        try {
            val notification = Notification.Builder(context, TIMER_CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_foreground)
                .setContentTitle("Rest Complete!")
                .setContentText("Time to start your next set")
                .setAutoCancel(true)
                .build()
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.notify(TIMER_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) { }
    }

    companion object {
        const val TIMER_CHANNEL_ID = "rest_timer_channel"
        const val TIMER_NOTIFICATION_ID = 1001
    }
}
