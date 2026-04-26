package dev.kolas.nocapfit.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TimerStatus {
    RUNNING, COMPLETED, CANCELLED
}

@Entity(tableName = "active_timers")
data class ActiveTimer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val workoutSetId: Long,
    val startedAtEpochMs: Long,
    val endAtEpochMs: Long,
    val status: TimerStatus,
    val notificationId: Int
)
