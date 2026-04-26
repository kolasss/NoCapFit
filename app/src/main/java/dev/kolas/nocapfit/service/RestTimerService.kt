package dev.kolas.nocapfit.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import dev.kolas.nocapfit.R
import dev.kolas.nocapfit.data.repository.TimerRepository
import dev.kolas.nocapfit.util.MILLIS_PER_SECOND
import dev.kolas.nocapfit.util.ceilSecondsFromMs
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

    @Inject
    lateinit var timerNotifier: TimerNotifier

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var timerId: Long = -1L
    private var startAtEpochMs: Long = 0L
    private var endAtEpochMs: Long = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CANCEL_TIMER) {
            serviceScope.launch {
                timerCoordinator.cancelTimer()
                stopSelf()
            }
            return START_NOT_STICKY
        }

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
            startAtEpochMs = timer.startedAtEpochMs
            endAtEpochMs = timer.endAtEpochMs

            startForeground(
                NOTIFICATION_ID,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )

            val notificationManager = getSystemService(NotificationManager::class.java)
            // Notify-then-delay so the iteration after remaining reaches 0 always posts a final
            // notification with elapsed == totalSeconds (progress bar fills 100%).
            while (true) {
                notificationManager.notify(NOTIFICATION_ID, buildNotification())
                val remaining = endAtEpochMs - System.currentTimeMillis()
                if (remaining <= 0) break
                delay(remaining.coerceAtMost(MILLIS_PER_SECOND))
            }
            // Detach the notification from the service before posting the completion update.
            // Otherwise stopSelf removes the notification, wiping "Rest Complete!" with it.
            stopForeground(STOP_FOREGROUND_DETACH)
            // The AlarmManager alarm can be delayed by Doze / background restrictions, so the
            // foreground service races the receiver to complete the timer. `completeIfRunning`
            // is atomic — only the winning caller gets rowsAffected=1 and fires the side effects.
            val completed = timerRepository.completeTimer(timerId)
            if (completed) {
                timerNotifier.notifyCompletion()
                timerCoordinator.onTimerCompleted(timerId)
            }
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun buildNotification(): Notification {
        val totalSeconds = ((endAtEpochMs - startAtEpochMs) / MILLIS_PER_SECOND).toInt()
        val remainingMs = (endAtEpochMs - System.currentTimeMillis()).coerceAtLeast(0)
        val remainingSeconds = ceilSecondsFromMs(remainingMs)
        val elapsedSeconds = (totalSeconds - remainingSeconds).coerceIn(0, totalSeconds)

        val cancelIntent = Intent(this, RestTimerService::class.java).apply {
            action = ACTION_CANCEL_TIMER
        }
        val cancelPendingIntent = PendingIntent.getService(
            this,
            0,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_foreground)
            .setContentTitle("Rest Timer")
            .setProgress(totalSeconds, elapsedSeconds, false)
            .setOngoing(true)
            .apply {
                if (Build.VERSION.SDK_INT_FULL >= Build.VERSION_CODES_FULL.BAKLAVA_1) {
                    setRequestPromotedOngoing(true)
                }
            }
            // Chronometer formats as floor(delta/1000); offset by (1s - 1ms) so it displays ceil.
            .setWhen(endAtEpochMs + MILLIS_PER_SECOND - 1)
            .setUsesChronometer(remainingSeconds > 0)
            .setChronometerCountDown(true)
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.logo_foreground),
                    "Skip",
                    cancelPendingIntent
                ).build()
            )
            .build()
    }

    companion object {
        const val EXTRA_TIMER_ID = "timer_id"
        const val CHANNEL_ID = TimerNotifier.TIMER_CHANNEL_ID
        const val NOTIFICATION_ID = TimerCoordinator.NOTIFICATION_ID
        const val ACTION_CANCEL_TIMER = "dev.kolas.nocapfit.CANCEL_TIMER"
    }
}
