package com.example.nocapfit.data.repository

import android.content.Context
import android.media.RingtoneManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.nocapfit.R
import com.example.nocapfit.data.db.dao.ActiveTimerDao
import com.example.nocapfit.data.db.entity.ActiveTimer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerRepository @Inject constructor(
    private val activeTimerDao: ActiveTimerDao,
    @ApplicationContext private val context: Context
) {
    suspend fun insert(timer: ActiveTimer): Long = activeTimerDao.insert(timer)
    suspend fun update(timer: ActiveTimer) = activeTimerDao.update(timer)
    fun getByWorkoutId(workoutId: Long): Flow<ActiveTimer?> = activeTimerDao.getByWorkoutId(workoutId)
    suspend fun getRunning(): ActiveTimer? = activeTimerDao.getRunning()
    suspend fun cancelAllRunning() = activeTimerDao.cancelAllRunning()
    suspend fun deleteByWorkoutId(workoutId: Long) = activeTimerDao.deleteByWorkoutId(workoutId)

    suspend fun completeTimer(timerId: Long): Boolean {
        val rowsAffected = activeTimerDao.completeIfRunning(timerId)
        if (rowsAffected == 0) return false

        // Play sound
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone?.play()
        } catch (_: Exception) { }

        // Vibrate
        try {
            val vibrator = context.getSystemService(Vibrator::class.java)
            vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) { }

        // Update notification
        try {
            val notification = NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Rest Complete!")
                .setContentText("Time to start your next set")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(COMPLETION_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) { }

        return true
    }

    companion object {
        const val TIMER_CHANNEL_ID = "rest_timer_channel"
        const val COMPLETION_NOTIFICATION_ID = 9999
    }
}
