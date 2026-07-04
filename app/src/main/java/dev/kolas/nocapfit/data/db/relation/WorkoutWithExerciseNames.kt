package dev.kolas.nocapfit.data.db.relation

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Relation
import dev.kolas.nocapfit.data.db.entity.Workout
import dev.kolas.nocapfit.data.db.entity.WorkoutExercise

/**
 * Sets-free projection for list screens. Unlike [WorkoutWithExercises], observing this relation
 * does not re-emit on `workout_sets` changes.
 */
@Immutable
data class WorkoutWithExerciseNames(
    @Embedded val workout: Workout,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val exercises: List<WorkoutExercise>
)
