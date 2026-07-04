package dev.kolas.nocapfit.data.db.dao

/**
 * One completed set of a given exercise in a finished workout. Set columns are null for
 * workouts that contain the exercise but have no completed sets (LEFT JOIN).
 */
data class ExerciseHistorySetRow(
    val workoutId: Long,
    val programName: String?,
    val startTime: Long,
    val setIndex: Int?,
    val weightThousandths: Int?,
    val reps: Int?
)
