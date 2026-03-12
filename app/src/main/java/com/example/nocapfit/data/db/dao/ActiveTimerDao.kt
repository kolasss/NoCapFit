package com.example.nocapfit.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.nocapfit.data.db.entity.ActiveTimer
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveTimerDao {
    @Insert
    suspend fun insert(timer: ActiveTimer): Long

    @Update
    suspend fun update(timer: ActiveTimer)

    @Query("SELECT * FROM active_timers WHERE workoutId = :workoutId ORDER BY id DESC LIMIT 1")
    fun getByWorkoutId(workoutId: Long): Flow<ActiveTimer?>

    @Query("SELECT * FROM active_timers WHERE status = 'RUNNING' LIMIT 1")
    suspend fun getRunning(): ActiveTimer?

    @Query("UPDATE active_timers SET status = 'COMPLETED' WHERE id = :id AND status = 'RUNNING'")
    suspend fun completeIfRunning(id: Long): Int

    @Query("UPDATE active_timers SET status = 'CANCELLED' WHERE status = 'RUNNING'")
    suspend fun cancelAllRunning()

    @Query("DELETE FROM active_timers WHERE workoutId = :workoutId")
    suspend fun deleteByWorkoutId(workoutId: Long)
}
