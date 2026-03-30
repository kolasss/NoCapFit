package com.example.nocapfit.ui.screens.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nocapfit.data.db.entity.Exercise
import com.example.nocapfit.data.db.entity.WorkoutExercise
import com.example.nocapfit.data.db.entity.WorkoutSet
import com.example.nocapfit.data.db.relation.WorkoutWithExercises
import com.example.nocapfit.data.repository.ExerciseRepository
import com.example.nocapfit.data.repository.ProfileRepository
import com.example.nocapfit.data.repository.TimerRepository
import com.example.nocapfit.data.repository.WorkoutRepository
import com.example.nocapfit.service.TimerCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkoutInProgressViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val timerRepository: TimerRepository,
    private val exerciseRepository: ExerciseRepository,
    private val profileRepository: ProfileRepository,
    savedStateHandle: SavedStateHandle,
    private val timerCoordinator: TimerCoordinator
) : ViewModel() {

    val workoutId: Long = savedStateHandle.get<Long>("workoutId") ?: -1L

    val workout: StateFlow<WorkoutWithExercises?> = workoutRepository
        .getWithExercisesFlow(workoutId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val timerState: StateFlow<TimerCoordinator.TimerUiState> = timerCoordinator.timerState

    private val _profileId = MutableStateFlow<Long?>(null)

    val availableExercises: StateFlow<List<Exercise>> = _profileId.flatMapLatest { profileId ->
        if (profileId == null) {
            flowOf(emptyList())
        } else {
            exerciseRepository.getAllByProfile(profileId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val profile = profileRepository.getDefault()
            _profileId.value = profile?.id
            timerCoordinator.reconstructState()
        }
    }

    fun completeSet(workoutSetId: Long, restTimeSeconds: Int) {
        viewModelScope.launch {
            val workoutData = workout.value ?: return@launch
            val set = findSet(workoutSetId) ?: return@launch
            workoutRepository.updateWorkoutSet(set.copy(completed = true))
            if (restTimeSeconds > 0) {
                timerCoordinator.startTimer(
                    workoutId = workoutData.workout.id,
                    workoutSetId = workoutSetId,
                    durationSeconds = restTimeSeconds
                )
            }
        }
    }

    fun revertSet(workoutSetId: Long) {
        viewModelScope.launch {
            val set = findSet(workoutSetId) ?: return@launch
            workoutRepository.updateWorkoutSet(set.copy(completed = false))

            val currentTimer = timerCoordinator.timerState.value
            if (currentTimer is TimerCoordinator.TimerUiState.Running &&
                currentTimer.workoutSetId == workoutSetId
            ) {
                timerCoordinator.cancelTimer()
            }
        }
    }

    fun updateSet(workoutSet: WorkoutSet) {
        viewModelScope.launch {
            workoutRepository.updateWorkoutSet(workoutSet)
        }
    }

    fun addSet(workoutExerciseId: Long) {
        viewModelScope.launch {
            val existingSets = workoutRepository.getSetsForExercise(workoutExerciseId)
            val lastSet = existingSets.maxByOrNull { it.setIndex }
            val newIndex = (lastSet?.setIndex ?: -1) + 1
            val newSet = WorkoutSet(
                workoutExerciseId = workoutExerciseId,
                setIndex = newIndex,
                weightThousandths = lastSet?.weightThousandths ?: 0,
                reps = lastSet?.reps ?: 0,
                restTimeSeconds = lastSet?.restTimeSeconds ?: 60,
                completed = false
            )
            workoutRepository.insertWorkoutSet(newSet)
        }
    }

    fun addExerciseFromDb(exerciseId: Long, exerciseName: String) {
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

    fun removeExercise(workoutExerciseId: Long) {
        viewModelScope.launch {
            workoutRepository.deleteWorkoutExercise(workoutExerciseId)
        }
    }

    fun finishWorkout(): Boolean {
        val workoutData = workout.value ?: return false
        viewModelScope.launch {
            workoutRepository.update(
                workoutData.workout.copy(endTime = System.currentTimeMillis())
            )
            timerCoordinator.cancelTimer()
        }
        return true
    }

    fun cancelWorkout() {
        viewModelScope.launch {
            val workoutData = workout.value ?: return@launch
            timerCoordinator.cancelTimer()
            timerRepository.deleteByWorkoutId(workoutId)
            workoutRepository.delete(workoutData.workout)
        }
    }

    fun cancelTimer() {
        viewModelScope.launch {
            timerCoordinator.cancelTimer()
        }
    }

    private fun findSet(workoutSetId: Long): WorkoutSet? {
        return workout.value?.exercises
            ?.flatMap { it.sets }
            ?.find { it.id == workoutSetId }
    }
}
