package dev.kolas.nocapfit.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.kolas.nocapfit.data.repository.TimerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimerCompletionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var timerRepository: TimerRepository

    @Inject
    lateinit var timerCoordinator: TimerCoordinator

    @Inject
    lateinit var timerNotifier: TimerNotifier

    override fun onReceive(context: Context, intent: Intent) {
        val timerId = intent.getLongExtra(EXTRA_TIMER_ID, -1L)
        if (timerId == -1L) return

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            try {
                val completed = timerRepository.completeTimer(timerId)
                if (completed) {
                    timerNotifier.notifyCompletion()
                    timerCoordinator.onTimerCompleted(timerId)
                    // Don't stopService here — the service's polling loop will reach
                    // `remaining <= 0` within milliseconds and stop itself after calling
                    // stopForeground(STOP_FOREGROUND_DETACH). Forcing a stop here would
                    // cancel the service's coroutine mid-`delay` and wipe the notification.
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_TIMER_ID = "timer_id"
    }
}
