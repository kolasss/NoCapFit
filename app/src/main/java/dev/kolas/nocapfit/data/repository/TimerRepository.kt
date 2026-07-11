package dev.kolas.nocapfit.data.repository

import dev.kolas.nocapfit.data.db.dao.ActiveTimerDao
import dev.kolas.nocapfit.data.db.entity.ActiveTimer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerRepository @Inject constructor(
    private val activeTimerDao: ActiveTimerDao
) {
    suspend fun insert(timer: ActiveTimer): Long = activeTimerDao.insert(timer)
    suspend fun getRunning(): ActiveTimer? = activeTimerDao.getRunning()
    suspend fun cancelAllRunning() = activeTimerDao.deleteAll()

    suspend fun completeTimer(timerId: Long): Boolean =
        activeTimerDao.deleteIfRunning(timerId) != 0
}
