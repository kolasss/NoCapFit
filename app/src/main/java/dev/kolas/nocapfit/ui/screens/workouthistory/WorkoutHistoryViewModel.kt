package dev.kolas.nocapfit.ui.screens.workouthistory

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kolas.nocapfit.data.db.relation.WorkoutWithExerciseNames
import dev.kolas.nocapfit.data.repository.WorkoutRepository
import dev.kolas.nocapfit.data.session.CurrentProfileHolder
import dev.kolas.nocapfit.ui.util.formatDateTime
import dev.kolas.nocapfit.ui.util.formatDuration
import dev.kolas.nocapfit.ui.util.formatMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class WorkoutHistoryItemUi(
    val id: Long,
    val title: String,
    val dateTimeText: String,
    val durationText: String,
    val exerciseSummary: String
)

@Immutable
data class WorkoutHistoryMonthGroup(
    val month: String,
    val workouts: List<WorkoutHistoryItemUi>
)

internal fun toHistoryItemUi(source: WorkoutWithExerciseNames): WorkoutHistoryItemUi {
    val workout = source.workout
    val durationMs = (workout.endTime ?: workout.startTime) - workout.startTime
    return WorkoutHistoryItemUi(
        id = workout.id,
        title = workout.programName ?: "Free Workout",
        dateTimeText = formatDateTime(workout.startTime),
        durationText = formatDuration(durationMs),
        exerciseSummary = source.exercises
            .sortedBy { it.orderIndex }
            .joinToString(", ") { it.exerciseName }
    )
}

internal fun groupByMonth(workouts: List<WorkoutWithExerciseNames>): List<WorkoutHistoryMonthGroup> =
    workouts
        .groupBy { formatMonth(it.workout.startTime) }
        .map { (month, items) -> WorkoutHistoryMonthGroup(month, items.map(::toHistoryItemUi)) }

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkoutHistoryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val currentProfileHolder: CurrentProfileHolder
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val historyGroups: StateFlow<List<WorkoutHistoryMonthGroup>> = currentProfileHolder.profileId
        .flatMapLatest { profileId ->
            if (profileId == null) {
                flowOf(emptyList())
            } else {
                workoutRepository.getFinishedWithExerciseNames(profileId)
            }
        }
        .map(::groupByMonth)
        .onEach { _isLoading.value = false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasActiveWorkout: StateFlow<Boolean> = workoutRepository.getActiveWorkoutFlow()
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        viewModelScope.launch { currentProfileHolder.ensureLoaded() }
    }
}
