package dev.kolas.nocapfit.ui.screens.workoutedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kolas.nocapfit.data.db.entity.Exercise
import dev.kolas.nocapfit.data.db.entity.WorkoutExercise
import dev.kolas.nocapfit.data.db.entity.WorkoutSet
import dev.kolas.nocapfit.data.db.relation.WorkoutExerciseWithSets
import dev.kolas.nocapfit.data.db.relation.WorkoutWithExercises
import dev.kolas.nocapfit.data.repository.ExerciseRepository
import dev.kolas.nocapfit.data.repository.WorkoutRepository
import dev.kolas.nocapfit.data.session.CurrentProfileHolder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkoutEditViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    currentProfileHolder: CurrentProfileHolder,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workoutId: Long = checkNotNull(savedStateHandle["workoutId"])

    private val _workout = MutableStateFlow<WorkoutWithExercises?>(null)
    val workout: StateFlow<WorkoutWithExercises?> = _workout.asStateFlow()

    val availableExercises: StateFlow<List<Exercise>> = currentProfileHolder.profileId
        .flatMapLatest { profileId ->
            if (profileId == null) flowOf(emptyList()) else exerciseRepository.getAllByProfile(profileId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _programName = MutableStateFlow("")
    val programName: StateFlow<String> = _programName.asStateFlow()

    private var nextTempId = -1L

    init {
        viewModelScope.launch {
            currentProfileHolder.ensureLoaded()
            val data = workoutRepository.getWithExercises(workoutId)
            _workout.value = data
            _programName.value = data?.workout?.programName ?: ""
        }
    }

    fun updateProgramName(name: String) {
        _programName.value = name
    }

    fun updateSet(workoutSet: WorkoutSet) {
        updateSnapshot { data ->
            data.copy(
                exercises = data.exercises.map { ex ->
                    ex.copy(sets = ex.sets.map { if (it.id == workoutSet.id) workoutSet else it })
                }
            )
        }
    }

    fun toggleSetCompleted(workoutSet: WorkoutSet) {
        updateSet(workoutSet.copy(completed = !workoutSet.completed))
    }

    fun addSet(workoutExerciseId: Long) {
        updateSnapshot { data ->
            data.copy(
                exercises = data.exercises.map { ex ->
                    if (ex.workoutExercise.id != workoutExerciseId) return@map ex
                    val lastSet = ex.sets.maxByOrNull { it.setIndex }
                    val newSet = WorkoutSet(
                        id = nextTempId--,
                        workoutExerciseId = workoutExerciseId,
                        setIndex = (lastSet?.setIndex ?: -1) + 1,
                        weightThousandths = lastSet?.weightThousandths ?: 0,
                        reps = lastSet?.reps ?: 0,
                        restTimeSeconds = lastSet?.restTimeSeconds ?: 60,
                        completed = false
                    )
                    ex.copy(sets = ex.sets + newSet)
                }
            )
        }
    }

    fun removeExercise(workoutExerciseId: Long) {
        updateSnapshot { data ->
            data.copy(
                exercises = data.exercises.filter { it.workoutExercise.id != workoutExerciseId }
            )
        }
    }

    fun moveExercise(workoutExerciseId: Long, direction: Int) {
        updateSnapshot { data ->
            val sorted = data.exercises.sortedBy { it.workoutExercise.orderIndex }
            val currentIndex = sorted.indexOfFirst { it.workoutExercise.id == workoutExerciseId }
            val targetIndex = currentIndex + direction
            if (currentIndex < 0 || targetIndex < 0 || targetIndex >= sorted.size) return@updateSnapshot data
            val currentOrder = sorted[currentIndex].workoutExercise.orderIndex
            val targetOrder = sorted[targetIndex].workoutExercise.orderIndex
            data.copy(
                exercises = data.exercises.map { ex ->
                    when (ex.workoutExercise.id) {
                        sorted[currentIndex].workoutExercise.id ->
                            ex.copy(workoutExercise = ex.workoutExercise.copy(orderIndex = targetOrder))
                        sorted[targetIndex].workoutExercise.id ->
                            ex.copy(workoutExercise = ex.workoutExercise.copy(orderIndex = currentOrder))
                        else -> ex
                    }
                }
            )
        }
    }

    fun addExercise(exerciseId: Long, exerciseName: String) {
        updateSnapshot { data ->
            val maxOrder = data.exercises.maxOfOrNull { it.workoutExercise.orderIndex } ?: -1
            val weId = nextTempId--
            val newExercise = WorkoutExerciseWithSets(
                workoutExercise = WorkoutExercise(
                    id = weId,
                    workoutId = workoutId,
                    exerciseName = exerciseName,
                    exerciseId = exerciseId,
                    orderIndex = maxOrder + 1
                ),
                sets = listOf(
                    WorkoutSet(
                        id = nextTempId--,
                        workoutExerciseId = weId,
                        setIndex = 0,
                        weightThousandths = 0,
                        reps = 0,
                        restTimeSeconds = 60,
                        completed = false
                    )
                )
            )
            data.copy(exercises = data.exercises + newExercise)
        }
    }

    fun save(onSaved: () -> Unit) {
        val data = _workout.value ?: return
        val newName = _programName.value.trim().ifEmpty { null }
        val updatedWorkout = data.workout.copy(programName = newName)
        val exercisesWithSets = data.exercises.map { ex ->
            val we = ex.workoutExercise.copy(id = 0)
            val sets = ex.sets.map { it.copy(id = 0, workoutExerciseId = 0) }
            we to sets
        }
        viewModelScope.launch {
            workoutRepository.saveWorkoutEdits(updatedWorkout, exercisesWithSets)
            onSaved()
        }
    }

    private inline fun updateSnapshot(transform: (WorkoutWithExercises) -> WorkoutWithExercises) {
        val current = _workout.value ?: return
        _workout.value = transform(current)
    }
}
