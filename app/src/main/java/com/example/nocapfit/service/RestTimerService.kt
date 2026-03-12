package com.example.nocapfit.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.nocapfit.R
import com.example.nocapfit.data.repository.TimerRepository
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

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

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

            // Update notification every second
            while (true) {
                val remaining = endAtEpochMs - System.currentTimeMillis()
                if (remaining <= 0) {
                    // Timer completed
                    val completed = timerRepository.completeTimer(timerId)
                    if (completed) {
                        timerCoordinator.onTimerCompleted(timerId)
                    }
                    stopSelf()
                    return@launch
                }

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, buildNotification(remaining))

                delay(1000)
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun buildNotification(remainingMs: Long): Notification {
        val totalSeconds = (remainingMs / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val timeText = "Rest: %d:%02d".format(minutes, seconds)

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
            .setContentTitle(timeText)
            .setContentText("Rest timer running")
            .setOngoing(true)
            .setSilent(true)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Cancel",
                cancelPendingIntent
            )
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Rest Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows rest timer countdown"
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val EXTRA_TIMER_ID = "timer_id"
        const val CHANNEL_ID = TimerRepository.TIMER_CHANNEL_ID
        const val NOTIFICATION_ID = TimerCoordinator.NOTIFICATION_ID
        const val ACTION_CANCEL_TIMER = "com.example.nocapfit.CANCEL_TIMER"
    }
}
