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

fun buildProgramExerciseRows(
    exercises: List<ExerciseEntry>,
    previousSets: PreviousSetLookup
): List<ProgramExerciseRow> {
    if (exercises.isEmpty()) return emptyList()
    val previousTextsByExercise = buildPreviousTextsByExercise(previousSets)
    return exercises.map { entry ->
        buildSingleProgramRow(entry, previousTextsByExercise[entry.exercise.id].orEmpty())
    }
}

private fun buildSingleProgramRow(
    entry: ExerciseEntry,
    previousTexts: Map<Int, String>
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
