package com.example.nocapfit.data.repository

import com.example.nocapfit.data.db.dao.ActiveTimerDao
import com.example.nocapfit.data.db.entity.ActiveTimer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerRepository @Inject constructor(
    private val activeTimerDao: ActiveTimerDao
) {
    suspend fun insert(timer: ActiveTimer): Long = activeTimerDao.insert(timer)
    suspend fun update(timer: ActiveTimer) = activeTimerDao.update(timer)
    suspend fun getRunning(): ActiveTimer? = activeTimerDao.getRunning()
    suspend fun cancelAllRunning() = activeTimerDao.cancelAllRunning()
    suspend fun deleteByWorkoutId(workoutId: Long) = activeTimerDao.deleteByWorkoutId(workoutId)

    suspend fun completeTimer(timerId: Long): Boolean =
        activeTimerDao.completeIfRunning(timerId) != 0
}
