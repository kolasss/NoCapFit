package com.example.nocapfit.data.db.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "program_exercises",
    foreignKeys = [
        ForeignKey(
            entity = Program::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["programId"]), Index(value = ["exerciseId"])]
)
data class ProgramExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programId: Long,
    val exerciseId: Long,
    val orderIndex: Int
)
