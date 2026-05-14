package dev.kolas.nocapfit.ui.model

import androidx.compose.runtime.Immutable
import dev.kolas.nocapfit.data.db.entity.WorkoutExercise
import dev.kolas.nocapfit.data.db.entity.WorkoutSet

@Immutable
data class WorkoutExerciseRow(
    val workoutExercise: WorkoutExercise,
    val sets: List<SetUiModel>,
    val setsById: Map<Long, WorkoutSet>
)

fun buildWorkoutExerciseRows(
    exercises: List<dev.kolas.nocapfit.data.db.relation.WorkoutExerciseWithSets>,
    previousSets: PreviousSetLookup
): List<WorkoutExerciseRow> {
    if (exercises.isEmpty()) return emptyList()
    val previousTextsByExercise = buildPreviousTextsByExercise(previousSets)
    return exercises
        .sortedBy { it.workoutExercise.orderIndex }
        .map { we ->
            val exId = we.workoutExercise.exerciseId
            val previousTexts = if (exId != null) {
                previousTextsByExercise.forExercise(exId)
            } else {
                PreviousTextsForExercise.Empty
            }
            val sortedSets = we.sets.sortedBy { it.setIndex }
            WorkoutExerciseRow(
                workoutExercise = we.workoutExercise,
                sets = sortedSets.map { ws ->
                    SetUiModel(
                        id = ws.id,
                        setIndex = ws.setIndex,
                        weightThousandths = ws.weightThousandths,
                        reps = ws.reps,
                        restTimeSeconds = ws.restTimeSeconds,
                        completed = ws.completed,
                        previousText = previousTexts[ws.setIndex]
                    )
                },
                setsById = sortedSets.associateBy { it.id }
            )
        }
}
