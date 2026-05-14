package dev.kolas.nocapfit.ui.model

import androidx.compose.runtime.Immutable
import dev.kolas.nocapfit.data.db.entity.WorkoutExercise
import dev.kolas.nocapfit.data.db.entity.WorkoutSet
import dev.kolas.nocapfit.data.db.relation.WorkoutExerciseWithSets

@Immutable
data class WorkoutExerciseRow(
    val workoutExercise: WorkoutExercise,
    val sets: List<SetUiModel>,
    val setsById: Map<Long, WorkoutSet>
)

/**
 * Stateful builder that caches per-exercise rows so that unchanged exercises reuse the same
 * [WorkoutExerciseRow] instance across emissions. Hold one instance per ViewModel.
 *
 * Cache hit condition: same source [WorkoutExerciseWithSets] value AND same per-exercise
 * [PreviousTextsForExercise]. On hit, the returned `WorkoutExerciseRow` is reference-equal to the
 * one returned previously — important for Compose item-skipping in `LazyColumn`.
 */
class WorkoutExerciseRowBuilder {
    private var lastPreviousSets: PreviousSetLookup? = null
    private var lastPreviousTextsByExercise: PreviousTextsByExercise = PreviousTextsByExercise.Empty
    private var lastEntries: Map<Long, CacheEntry> = emptyMap()

    private data class CacheEntry(
        val source: WorkoutExerciseWithSets,
        val previousTexts: PreviousTextsForExercise,
        val row: WorkoutExerciseRow
    )

    fun build(
        exercises: List<WorkoutExerciseWithSets>,
        previousSets: PreviousSetLookup
    ): List<WorkoutExerciseRow> {
        if (exercises.isEmpty()) {
            lastEntries = emptyMap()
            return emptyList()
        }
        val previousTextsByExercise = if (previousSets === lastPreviousSets) {
            lastPreviousTextsByExercise
        } else {
            buildPreviousTextsByExercise(previousSets).also {
                lastPreviousSets = previousSets
                lastPreviousTextsByExercise = it
            }
        }
        val newEntries = mutableMapOf<Long, CacheEntry>()
        val result = exercises
            .sortedBy { it.workoutExercise.orderIndex }
            .map { we ->
                val exId = we.workoutExercise.exerciseId
                val previousTexts = if (exId != null) {
                    previousTextsByExercise.forExercise(exId)
                } else {
                    PreviousTextsForExercise.Empty
                }
                val key = we.workoutExercise.id
                val cached = lastEntries[key]
                val row = if (cached != null &&
                    cached.source == we &&
                    cached.previousTexts == previousTexts
                ) {
                    cached.row
                } else {
                    buildSingleWorkoutRow(we, previousTexts)
                }
                newEntries[key] = CacheEntry(we, previousTexts, row)
                row
            }
        lastEntries = newEntries
        return result
    }
}

/** Stateless one-shot builder, primarily for tests. */
fun buildWorkoutExerciseRows(
    exercises: List<WorkoutExerciseWithSets>,
    previousSets: PreviousSetLookup
): List<WorkoutExerciseRow> = WorkoutExerciseRowBuilder().build(exercises, previousSets)

private fun buildSingleWorkoutRow(
    we: WorkoutExerciseWithSets,
    previousTexts: PreviousTextsForExercise
): WorkoutExerciseRow {
    val sortedSets = we.sets.sortedBy { it.setIndex }
    return WorkoutExerciseRow(
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
