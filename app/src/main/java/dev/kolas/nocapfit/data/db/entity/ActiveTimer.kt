package dev.kolas.nocapfit.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A currently running rest timer. Row exists = timer is running; completing or cancelling a
 * timer deletes its row, so the table holds at most one row in practice.
 */
@Entity(tableName = "active_timers")
data class ActiveTimer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val workoutSetId: Long,
    val startedAtEpochMs: Long,
    val endAtEpochMs: Long
)
