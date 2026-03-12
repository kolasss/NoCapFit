package com.example.nocapfit.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "program_exercise_sets",
    foreignKeys = [
        ForeignKey(
            entity = ProgramExercise::class,
            parentColumns = ["id"],
            childColumns = ["programExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["programExerciseId"])]
)
data class ProgramExerciseSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programExerciseId: Long,
    val setIndex: Int,
    val weightThousandths: Int,
    val reps: Int,
    val restTimeSeconds: Int
)
