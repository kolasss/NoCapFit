package dev.kolas.nocapfit.service

import dev.kolas.nocapfit.data.repository.TimerRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerCompletionHandler @Inject constructor(
    private val timerRepository: TimerRepository,
    private val timerNotifier: TimerNotifier,
    private val timerCoordinator: TimerCoordinator
) {
    // The AlarmManager alarm can race the foreground service's polling loop. `completeTimer`
    // returns true only for the winner (atomic UPDATE with rowsAffected guard), so the side
    // effects fire exactly once.
    suspend fun completeIfRunning(timerId: Long) {
        if (timerRepository.completeTimer(timerId)) {
            timerNotifier.notifyCompletion()
            timerCoordinator.onTimerCompleted(timerId)
        }
    }
}
