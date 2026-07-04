package dev.kolas.nocapfit.ui.screens.exercises

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kolas.nocapfit.data.db.dao.ExerciseHistorySetRow
import dev.kolas.nocapfit.data.db.entity.Exercise
import dev.kolas.nocapfit.data.repository.ExerciseRepository
import dev.kolas.nocapfit.data.repository.WorkoutRepository
import dev.kolas.nocapfit.ui.util.formatDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class ExerciseHistorySetUi(
    val setNumber: Int,
    val weightThousandths: Int,
    val reps: Int
)

@Immutable
data class ExerciseHistoryEntryUi(
    val workoutId: Long,
    val title: String,
    val dateTimeText: String,
    val sets: List<ExerciseHistorySetUi>
)

internal fun groupExerciseHistory(rows: List<ExerciseHistorySetRow>): List<ExerciseHistoryEntryUi> =
    rows.groupBy { it.workoutId }.map { (workoutId, workoutRows) ->
        val first = workoutRows.first()
        // The exercise can appear more than once in a workout (supersets), so per-instance
        // setIndex values may repeat. Rows arrive ordered by exercise position, then setIndex —
        // number them sequentially across the whole workout.
        val sets = workoutRows.filter {
            it.setIndex != null && it.weightThousandths != null && it.reps != null
        }
        ExerciseHistoryEntryUi(
            workoutId = workoutId,
            title = first.programName ?: "Free Workout",
            dateTimeText = formatDateTime(first.startTime),
            sets = sets.mapIndexed { index, row ->
                ExerciseHistorySetUi(
                    setNumber = index + 1,
                    weightThousandths = checkNotNull(row.weightThousandths),
                    reps = checkNotNull(row.reps)
                )
            }
        )
    }

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    workoutRepository: WorkoutRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val exerciseId: Long = checkNotNull(savedStateHandle["exerciseId"])

    val exercise: StateFlow<Exercise?> = exerciseRepository
        .getByIdFlow(exerciseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val exerciseHistory: StateFlow<List<ExerciseHistoryEntryUi>> = workoutRepository
        .getExerciseHistory(exerciseId)
        .map(::groupExerciseHistory)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun selectTab(index: Int) { _selectedTab.value = index }

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation: StateFlow<Boolean> = _showDeleteConfirmation.asStateFlow()

    fun showDeleteConfirmation() {
        _showDeleteConfirmation.value = true
    }

    fun dismissDeleteConfirmation() {
        _showDeleteConfirmation.value = false
    }

    fun deleteExercise(onDeleted: () -> Unit) {
        val exercise = exercise.value ?: return
        viewModelScope.launch {
            exerciseRepository.delete(exercise)
            onDeleted()
        }
    }
}
