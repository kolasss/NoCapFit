package dev.kolas.nocapfit.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.kolas.nocapfit.data.db.entity.ActiveTimer
import dev.kolas.nocapfit.data.repository.TimerRepository
import dev.kolas.nocapfit.util.MILLIS_PER_SECOND
import dev.kolas.nocapfit.util.TIMER_FINISHED_DISPLAY_MS
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
import kotlin.time.Duration.Companion.milliseconds

private const val LONG_HIGH_BITS_SHIFT = 32

internal fun stableRequestCode(timerId: Long): Int =
    (timerId xor (timerId ushr LONG_HIGH_BITS_SHIFT)).toInt()

@Singleton
class TimerCoordinator @Inject constructor(
    private val timerRepository: TimerRepository,
    private val timerNotifier: TimerNotifier,
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
            endAtEpochMs = endAtEpochMs
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
            cancelAlarm(currentState.timerId)
        }
        // Cancel by the DB row's id too: if UI state is out of sync (e.g. after process
        // restart), cancelling only by state would delete the row but leave its alarm
        // scheduled — a harmless but unnecessary wakeup later.
        timerRepository.getRunning()?.let { row ->
            if ((currentState as? TimerUiState.Running)?.timerId != row.id) {
                cancelAlarm(row.id)
            }
        }
        // Delete unconditionally so a stale row can't survive a cancel.
        timerRepository.cancelAllRunning()
        context.stopService(Intent(context, RestTimerService::class.java))
        _timerState.value = TimerUiState.Idle
    }

    /**
     * Deletes the timer row and fires completion side effects (sound, vibration, notification).
     * Called by both the foreground service (primary owner) and the AlarmManager backstop;
     * the row delete is atomic, so only the first caller runs the side effects.
     */
    suspend fun completeIfRunning(timerId: Long) {
        if (timerRepository.completeTimer(timerId)) {
            timerNotifier.notifyCompletion()
            onTimerCompleted(timerId)
        }
    }

    fun onTimerCompleted(timerId: Long) {
        val current = _timerState.value
        if (current !is TimerUiState.Running || current.timerId != timerId) return
        cancelAlarm(timerId)
        _timerState.value = TimerUiState.Finished
        scope.launch {
            delay(TIMER_FINISHED_DISPLAY_MS.milliseconds)
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

    // The foreground service owns completion; this alarm is only a backstop for process death
    // (and CPU sleep delaying the service's delay), so the inexact-while-idle tier is enough.
    // Exact alarms would need the SCHEDULE_EXACT_ALARM permission flow for no practical gain.
    private fun scheduleAlarm(timerId: Long, endAtEpochMs: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            endAtEpochMs,
            createAlarmIntent(timerId)
        )
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
}
