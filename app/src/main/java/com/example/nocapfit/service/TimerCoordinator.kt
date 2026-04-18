package com.example.nocapfit.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.nocapfit.data.db.entity.ActiveTimer
import com.example.nocapfit.data.db.entity.TimerStatus
import com.example.nocapfit.data.repository.TimerRepository
import com.example.nocapfit.util.MILLIS_PER_SECOND
import com.example.nocapfit.util.TIMER_FINISHED_DISPLAY_MS
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val LONG_HIGH_BITS_SHIFT = 32

internal fun stableRequestCode(timerId: Long): Int =
    (timerId xor (timerId ushr LONG_HIGH_BITS_SHIFT)).toInt()

@Singleton
class TimerCoordinator @Inject constructor(
    private val timerRepository: TimerRepository,
    @param:ApplicationContext private val context: Context
) {

    sealed class TimerUiState {
        data object Idle : TimerUiState()
        data class Running(
            val timerId: Long,
            val endAtEpochMs: Long,
            val workoutId: Long,
            val workoutSetId: Long = 0L,
            val totalMs: Long = 0L
        ) : TimerUiState()
        data object Finished : TimerUiState()
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _timerState = MutableStateFlow<TimerUiState>(TimerUiState.Idle)
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()

    init {
        scope.launch {
            reconstructState()
        }
    }

    suspend fun startTimer(workoutId: Long, workoutSetId: Long, durationSeconds: Int) {
        // Cancel any existing running timer
        cancelTimer()

        val now = System.currentTimeMillis()
        val endAtEpochMs = now + (durationSeconds * MILLIS_PER_SECOND)

        val timer = ActiveTimer(
            workoutId = workoutId,
            workoutSetId = workoutSetId,
            startedAtEpochMs = now,
            endAtEpochMs = endAtEpochMs,
            status = TimerStatus.RUNNING,
            notificationId = NOTIFICATION_ID
        )
        val timerId = timerRepository.insert(timer)

        // Schedule alarm
        scheduleAlarm(timerId, endAtEpochMs)

        // Start foreground service
        val serviceIntent = Intent(context, RestTimerService::class.java).apply {
            putExtra(RestTimerService.EXTRA_TIMER_ID, timerId)
        }
        context.startForegroundService(serviceIntent)

        _timerState.value = TimerUiState.Running(
            timerId = timerId,
            endAtEpochMs = endAtEpochMs,
            workoutId = workoutId,
            workoutSetId = workoutSetId,
            totalMs = durationSeconds * MILLIS_PER_SECOND
        )
    }

    suspend fun cancelTimer() {
        val currentState = _timerState.value
        if (currentState is TimerUiState.Running) {
            timerRepository.cancelAllRunning()
            cancelAlarm(currentState.timerId)
            context.stopService(Intent(context, RestTimerService::class.java))
        }
        _timerState.value = TimerUiState.Idle
    }

    fun onTimerCompleted(timerId: Long) {
        val current = _timerState.value
        if (current !is TimerUiState.Running || current.timerId != timerId) return
        cancelAlarm(timerId)
        _timerState.value = TimerUiState.Finished
        scope.launch {
            delay(TIMER_FINISHED_DISPLAY_MS)
            if (_timerState.value is TimerUiState.Finished) {
                _timerState.value = TimerUiState.Idle
            }
        }
    }

    suspend fun reconstructState() {
        val running = timerRepository.getRunning()
        if (running != null) {
            if (running.endAtEpochMs > System.currentTimeMillis()) {
                _timerState.value = TimerUiState.Running(
                    timerId = running.id,
                    endAtEpochMs = running.endAtEpochMs,
                    workoutId = running.workoutId,
                    workoutSetId = running.workoutSetId,
                    totalMs = running.endAtEpochMs - running.startedAtEpochMs
                )
            } else {
                // Timer should have completed already
                timerRepository.completeTimer(running.id)
                _timerState.value = TimerUiState.Idle
            }
        } else {
            _timerState.value = TimerUiState.Idle
        }
    }

    private fun scheduleAlarm(timerId: Long, endAtEpochMs: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = createAlarmIntent(timerId)
        try {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(endAtEpochMs, null),
                    intent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    endAtEpochMs,
                    intent
                )
            }
        } catch (_: SecurityException) {
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    endAtEpochMs,
                    intent
                )
            } catch (_: SecurityException) {
                // Best effort
            }
        }
    }

    private fun cancelAlarm(timerId: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = createAlarmIntent(timerId)
            alarmManager.cancel(intent)
        } catch (_: Exception) {
            // Best effort — a stale alarm firing is harmless (completeTimer is idempotent).
        }
    }

    private fun createAlarmIntent(timerId: Long): PendingIntent {
        val intent = Intent(context, TimerCompletionReceiver::class.java).apply {
            putExtra(TimerCompletionReceiver.EXTRA_TIMER_ID, timerId)
        }
        return PendingIntent.getBroadcast(
            context,
            stableRequestCode(timerId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
