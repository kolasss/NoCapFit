package dev.kolas.nocapfit.ui.model

import androidx.compose.runtime.Immutable
import dev.kolas.nocapfit.ui.components.parseMmSsToSeconds
import dev.kolas.nocapfit.ui.screens.programs.ExerciseEntry
import dev.kolas.nocapfit.ui.util.parseWeight

@Immutable
data class ProgramExerciseRow(
    val exerciseEntry: ExerciseEntry,
    val sets: List<SetUiModel>
)

/**
 * Stateful builder that caches per-exercise rows so a keystroke that changes one [ExerciseEntry]
 * does not re-parse strings for every other exercise in the program. Hold one instance per
 * ViewModel.
 */
class ProgramExerciseRowBuilder {
    private var lastPreviousSets: PreviousSetLookup? = null
    private var lastPreviousTextsByExercise: PreviousTextsByExercise = PreviousTextsByExercise.Empty
    private var lastEntries: Map<Long, CacheEntry> = emptyMap()

    private data class CacheEntry(
        val source: ExerciseEntry,
        val previousTexts: PreviousTextsForExercise,
        val row: ProgramExerciseRow
    )

    fun build(
        exercises: List<ExerciseEntry>,
        previousSets: PreviousSetLookup
    ): List<ProgramExerciseRow> {
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
        val result = exercises.map { entry ->
            val previousTexts = previousTextsByExercise.forExercise(entry.exercise.id)
            val key = entry.exercise.id
            val cached = lastEntries[key]
            val row = if (cached != null &&
                cached.source == entry &&
                cached.previousTexts == previousTexts
            ) {
                cached.row
            } else {
                buildSingleProgramRow(entry, previousTexts)
            }
            newEntries[key] = CacheEntry(entry, previousTexts, row)
            row
        }
        lastEntries = newEntries
        return result
    }
}

/** Stateless one-shot builder, primarily for tests. */
fun buildProgramExerciseRows(
    exercises: List<ExerciseEntry>,
    previousSets: PreviousSetLookup
): List<ProgramExerciseRow> = ProgramExerciseRowBuilder().build(exercises, previousSets)

private fun buildSingleProgramRow(
    entry: ExerciseEntry,
    previousTexts: PreviousTextsForExercise
): ProgramExerciseRow =
    ProgramExerciseRow(
        exerciseEntry = entry,
        sets = entry.sets.mapIndexed { setIndex, setEntry ->
            SetUiModel(
                id = setIndex.toLong(),
                setIndex = setIndex,
                weightThousandths = parseWeight(setEntry.weight),
                reps = setEntry.reps.toIntOrNull() ?: 0,
                restTimeSeconds = parseMmSsToSeconds(setEntry.restTimeSeconds),
                completed = false,
                previousText = previousTexts[setIndex]
            )
        }
    )
