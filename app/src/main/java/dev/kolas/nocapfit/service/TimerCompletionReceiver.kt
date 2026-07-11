package dev.kolas.nocapfit.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backstop completion path. The foreground service normally completes the timer and deletes
 * its row; this alarm only wins if the service didn't get there (process death, or CPU sleep
 * delaying the service's delay) — otherwise the row is already gone and this is a no-op.
 */
@AndroidEntryPoint
class TimerCompletionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var timerCoordinator: TimerCoordinator

    override fun onReceive(context: Context, intent: Intent) {
        val timerId = intent.getLongExtra(EXTRA_TIMER_ID, -1L)
        if (timerId == -1L) return

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            try {
                timerCoordinator.completeIfRunning(timerId)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_TIMER_ID = "timer_id"
    }
}
