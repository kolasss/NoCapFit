package com.example.nocapfit.ui.screens.workoutedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nocapfit.data.db.entity.Exercise
import com.example.nocapfit.data.db.entity.WorkoutExercise
import com.example.nocapfit.data.db.entity.WorkoutSet
import com.example.nocapfit.data.db.relation.WorkoutWithExercises
import com.example.nocapfit.data.repository.ExerciseRepository
import com.example.nocapfit.data.repository.ProfileRepository
import com.example.nocapfit.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val profileRepository: ProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workoutId: Long = checkNotNull(savedStateHandle["workoutId"])

    val workout: StateFlow<WorkoutWithExercises?> = workoutRepository
        .getWithExercisesFlow(workoutId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _profileId = MutableStateFlow<Long?>(null)

    val availableExercises: StateFlow<List<Exercise>> = _profileId.flatMapLatest { profileId ->
        if (profileId == null) flowOf(emptyList()) else exerciseRepository.getAllByProfile(profileId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _programName = MutableStateFlow("")
    val programName: StateFlow<String> = _programName.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = profileRepository.getDefault()
            _profileId.value = profile?.id
            val data = workoutRepository.getWithExercises(workoutId)
            _programName.value = data?.workout?.programName ?: ""
        }
    }

    fun updateProgramName(name: String) {
        _programName.value = name
    }

    fun saveProgramName() {
        val currentWorkout = workout.value?.workout ?: return
        val newName = _programName.value.trim().ifEmpty { null }
        if (newName == currentWorkout.programName) return
        viewModelScope.launch {
            workoutRepository.update(currentWorkout.copy(programName = newName))
        }
    }

    fun updateSet(workoutSet: WorkoutSet) {
        viewModelScope.launch {
            workoutRepository.updateWorkoutSet(workoutSet)
        }
    }

    fun toggleSetCompleted(workoutSet: WorkoutSet) {
        viewModelScope.launch {
            workoutRepository.updateWorkoutSet(workoutSet.copy(completed = !workoutSet.completed))
        }
    }

    fun addSet(workoutExerciseId: Long) {
        viewModelScope.launch {
            val existingSets = workoutRepository.getSetsForExercise(workoutExerciseId)
            val lastSet = existingSets.maxByOrNull { it.setIndex }
            val newIndex = (lastSet?.setIndex ?: -1) + 1
            workoutRepository.insertWorkoutSet(
                WorkoutSet(
                    workoutExerciseId = workoutExerciseId,
                    setIndex = newIndex,
                    weightThousandths = lastSet?.weightThousandths ?: 0,
                    reps = lastSet?.reps ?: 0,
                    restTimeSeconds = lastSet?.restTimeSeconds ?: 60,
                    completed = false
                )
            )
        }
    }

    fun removeExercise(workoutExerciseId: Long) {
        viewModelScope.launch {
            workoutRepository.deleteWorkoutExercise(workoutExerciseId)
        }
    }

    fun moveExercise(workoutExerciseId: Long, direction: Int) {
        val exercises = workout.value?.exercises?.sortedBy { it.workoutExercise.orderIndex } ?: return
        val currentIndex = exercises.indexOfFirst { it.workoutExercise.id == workoutExerciseId }
        val targetIndex = currentIndex + direction
        if (currentIndex < 0 || targetIndex < 0 || targetIndex >= exercises.size) return
        val current = exercises[currentIndex].workoutExercise
        val target = exercises[targetIndex].workoutExercise
        viewModelScope.launch {
            workoutRepository.updateWorkoutExercise(current.copy(orderIndex = target.orderIndex))
            workoutRepository.updateWorkoutExercise(target.copy(orderIndex = current.orderIndex))
        }
    }

    fun addExercise(exerciseId: Long, exerciseName: String) {
        viewModelScope.launch {
            val workoutData = workout.value ?: return@launch
            val maxOrder = workoutData.exercises.maxOfOrNull { it.workoutExercise.orderIndex } ?: -1
            val weId = workoutRepository.insertWorkoutExercise(
                WorkoutExercise(
                    workoutId = workoutId,
                    exerciseName = exerciseName,
                    exerciseId = exerciseId,
                    orderIndex = maxOrder + 1
                )
            )
            workoutRepository.insertWorkoutSet(
                WorkoutSet(
                    workoutExerciseId = weId,
                    setIndex = 0,
                    weightThousandths = 0,
                    reps = 0,
                    restTimeSeconds = 60,
                    completed = false
                )
            )
        }
    }
}
