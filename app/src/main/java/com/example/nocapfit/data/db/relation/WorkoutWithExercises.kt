package com.example.nocapfit.data.db.relation

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Relation
import com.example.nocapfit.data.db.entity.Workout
import com.example.nocapfit.data.db.entity.WorkoutExercise

@Immutable
data class WorkoutWithExercises(
    @Embedded val workout: Workout,
    @Relation(
        entity = WorkoutExercise::class,
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val exercises: List<WorkoutExerciseWithSets>
)
