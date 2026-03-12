package com.example.nocapfit.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.nocapfit.data.repository.TimerRepository
import dagger.hilt.android.AndroidEntryPoint
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

    override fun onReceive(context: Context, intent: Intent) {
        val timerId = intent.getLongExtra(EXTRA_TIMER_ID, -1L)
        if (timerId == -1L) return

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            try {
                val completed = timerRepository.completeTimer(timerId)
                if (completed) {
                    timerCoordinator.onTimerCompleted(timerId)
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
