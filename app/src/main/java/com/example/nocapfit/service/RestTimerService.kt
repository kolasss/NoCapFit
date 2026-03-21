package com.example.nocapfit.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.nocapfit.R
import com.example.nocapfit.data.repository.TimerRepository
import com.example.nocapfit.util.MILLIS_PER_SECOND
import com.example.nocapfit.util.SECONDS_PER_MINUTE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RestTimerService : Service() {

    @Inject
    lateinit var timerRepository: TimerRepository

    @Inject
    lateinit var timerCoordinator: TimerCoordinator

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var timerId: Long = -1L
    private var endAtEpochMs: Long = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        timerId = intent?.getLongExtra(EXTRA_TIMER_ID, -1L) ?: -1L
        if (timerId == -1L) {
            stopSelf()
            return START_NOT_STICKY
        }

        serviceScope.launch {
            val timer = timerRepository.getRunning()
            if (timer == null || timer.id != timerId) {
                stopSelf()
                return@launch
            }
            endAtEpochMs = timer.endAtEpochMs

            startForeground(NOTIFICATION_ID, buildNotification(endAtEpochMs - System.currentTimeMillis()))

            val notificationManager = getSystemService(NotificationManager::class.java)
            while (true) {
                val remaining = endAtEpochMs - System.currentTimeMillis()
                if (remaining <= 0) {
                    val completed = timerRepository.completeTimer(timerId)
                    if (completed) {
                        timerCoordinator.onTimerCompleted(timerId)
                    }
                    stopSelf()
                    return@launch
                }

                notificationManager.notify(NOTIFICATION_ID, buildNotification(remaining))
                delay(MILLIS_PER_SECOND)
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun buildNotification(remainingMs: Long): Notification {
        val totalSeconds = (remainingMs / MILLIS_PER_SECOND).coerceAtLeast(0)
        val minutes = totalSeconds / SECONDS_PER_MINUTE
        val seconds = totalSeconds % SECONDS_PER_MINUTE
        val timeText = "%d:%02d".format(minutes, seconds)

        val cancelIntent = Intent(ACTION_CANCEL_TIMER).apply {
            setPackage(packageName)
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Rest Timer")
            .setContentText(timeText)
            .setOngoing(true)
            .setSilent(true)
            .setShowWhen(false)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Cancel",
                cancelPendingIntent
            )
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    companion object {
        const val EXTRA_TIMER_ID = "timer_id"
        const val CHANNEL_ID = TimerRepository.TIMER_CHANNEL_ID
        const val NOTIFICATION_ID = TimerCoordinator.NOTIFICATION_ID
        const val ACTION_CANCEL_TIMER = "com.example.nocapfit.CANCEL_TIMER"
    }
}
