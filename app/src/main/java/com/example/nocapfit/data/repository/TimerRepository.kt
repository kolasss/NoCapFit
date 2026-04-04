package com.example.nocapfit.data.repository

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.net.toUri
import com.example.nocapfit.R
import com.example.nocapfit.data.db.dao.ActiveTimerDao
import com.example.nocapfit.data.db.entity.ActiveTimer
import com.example.nocapfit.data.preferences.ThemePreferences
import com.example.nocapfit.util.NOTIFICATION_SOUND_SILENT
import com.example.nocapfit.util.VIBRATION_DURATION_MS
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerRepository @Inject constructor(
    private val activeTimerDao: ActiveTimerDao,
    private val themePreferences: ThemePreferences,
    @param:ApplicationContext private val context: Context
) {
    suspend fun insert(timer: ActiveTimer): Long = activeTimerDao.insert(timer)
    suspend fun update(timer: ActiveTimer) = activeTimerDao.update(timer)
    suspend fun getRunning(): ActiveTimer? = activeTimerDao.getRunning()
    suspend fun cancelAllRunning() = activeTimerDao.cancelAllRunning()
    suspend fun deleteByWorkoutId(workoutId: Long) = activeTimerDao.deleteByWorkoutId(workoutId)

    suspend fun completeTimer(timerId: Long): Boolean {
        val rowsAffected = activeTimerDao.completeIfRunning(timerId)
        if (rowsAffected == 0) return false

        // Play sound (empty string = silent)
        try {
            val savedUri = themePreferences.notificationSoundUri.first()
            if (savedUri != NOTIFICATION_SOUND_SILENT) {
                val uri = savedUri?.toUri() ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                if (uri != null) {
                    RingtoneManager.getRingtone(context, uri)?.play()
                }
            }
        } catch (_: Exception) { }

        // Vibrate
        try {
            val vibrator = context.getSystemService(Vibrator::class.java)
            vibrator?.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) { }

        // Update notification
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

        return true
    }

    companion object {
        const val TIMER_CHANNEL_ID = "rest_timer_channel"
        const val TIMER_NOTIFICATION_ID = 1001
    }
}
