package com.example.nocapfit.data.db.relation

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Relation
import com.example.nocapfit.data.db.entity.WorkoutExercise
import com.example.nocapfit.data.db.entity.WorkoutSet

@Immutable
data class WorkoutExerciseWithSets(
    @Embedded val workoutExercise: WorkoutExercise,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutExerciseId"
    )
    val sets: List<WorkoutSet>
)
