package com.example.nocapfit.ui.screens.addworkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nocapfit.data.db.entity.Workout
import com.example.nocapfit.data.db.entity.WorkoutExercise
import com.example.nocapfit.data.db.entity.WorkoutSet
import com.example.nocapfit.data.db.relation.ProgramWithExercises
import com.example.nocapfit.data.repository.ProfileRepository
import com.example.nocapfit.data.repository.ProgramRepository
import com.example.nocapfit.data.repository.WorkoutRepository
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
class AddWorkoutViewModel @Inject constructor(
    private val programRepository: ProgramRepository,
    private val workoutRepository: WorkoutRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _profileId = MutableStateFlow<Long?>(null)

    val programs: StateFlow<List<ProgramWithExercises>> = _profileId
        .flatMapLatest { profileId ->
            if (profileId == null) {
                flowOf(emptyList())
            } else {
                programRepository.getAllWithExercises(profileId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val profile = profileRepository.getDefault()
            _profileId.value = profile?.id
        }
    }

    suspend fun createWorkoutFromProgram(programId: Long): Long {
        val profileId = checkNotNull(_profileId.value) { "Profile not loaded" }
        val programWithExercises = programRepository.getProgramWithExercises(programId)
            ?: throw IllegalArgumentException("Program not found: $programId")

        val workout = Workout(
            profileId = profileId,
            programName = programWithExercises.program.name,
            startTime = System.currentTimeMillis(),
            endTime = null
        )
        val workoutId = workoutRepository.insert(workout)

        for (programExerciseWithSets in programWithExercises.exercises) {
            val workoutExercise = WorkoutExercise(
                workoutId = workoutId,
                exerciseName = programExerciseWithSets.exercise.name,
                exerciseId = programExerciseWithSets.exercise.id,
                orderIndex = programExerciseWithSets.programExercise.orderIndex
            )
            val workoutExerciseId = workoutRepository.insertWorkoutExercise(workoutExercise)

            for (programSet in programExerciseWithSets.sets) {
                val workoutSet = WorkoutSet(
                    workoutExerciseId = workoutExerciseId,
                    setIndex = programSet.setIndex,
                    weightThousandths = programSet.weightThousandths,
                    reps = programSet.reps,
                    restTimeSeconds = programSet.restTimeSeconds,
                    completed = false
                )
                workoutRepository.insertWorkoutSet(workoutSet)
            }
        }

        return workoutId
    }

    suspend fun createEmptyWorkout(): Long {
        val profileId = checkNotNull(_profileId.value) { "Profile not loaded" }
        val workout = Workout(
            profileId = profileId,
            programName = null,
            startTime = System.currentTimeMillis(),
            endTime = null
        )
        return workoutRepository.insert(workout)
    }
}
