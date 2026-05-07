package dev.kolas.nocapfit.data.db.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "workouts",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["programId"])
    ]
)
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val programName: String? = null,
    val programId: Long? = null,
    val startTime: Long,
    val endTime: Long? = null
)
