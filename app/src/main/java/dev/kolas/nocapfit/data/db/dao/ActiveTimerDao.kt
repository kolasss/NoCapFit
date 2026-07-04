package dev.kolas.nocapfit.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.kolas.nocapfit.data.db.entity.ActiveTimer

@Dao
interface ActiveTimerDao {
    @Insert
    suspend fun insert(timer: ActiveTimer): Long

    @Query("SELECT * FROM active_timers LIMIT 1")
    suspend fun getRunning(): ActiveTimer?

    /**
     * Deletes the timer row, returning the number of rows removed. Both completion paths
     * (foreground service delay and the AlarmManager backstop) call this; the returned
     * rowcount makes the first caller the single winner, so side effects fire exactly once.
     */
    @Query("DELETE FROM active_timers WHERE id = :id")
    suspend fun deleteIfRunning(id: Long): Int

    @Query("DELETE FROM active_timers")
    suspend fun deleteAll()
}
