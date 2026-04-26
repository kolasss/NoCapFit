package dev.kolas.nocapfit.ui.screens.addworkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kolas.nocapfit.data.db.entity.Workout
import dev.kolas.nocapfit.data.db.entity.WorkoutExercise
import dev.kolas.nocapfit.data.db.entity.WorkoutSet
import dev.kolas.nocapfit.data.db.relation.ProgramWithExercises
import dev.kolas.nocapfit.data.repository.ProgramRepository
import dev.kolas.nocapfit.data.repository.WorkoutRepository
import dev.kolas.nocapfit.data.session.CurrentProfileHolder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddWorkoutViewModel @Inject constructor(
    private val programRepository: ProgramRepository,
    private val workoutRepository: WorkoutRepository,
    private val currentProfileHolder: CurrentProfileHolder
) : ViewModel() {

    val profileLoaded: StateFlow<Boolean> = currentProfileHolder.profileId
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val lastWorkoutTimes: StateFlow<Map<Long, Long>> = workoutRepository
        .getLastWorkoutTimeByProgram()
        .map { list -> list.associate { it.programId to it.lastTime } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val unsortedPrograms = currentProfileHolder.profileId
        .flatMapLatest { profileId ->
            if (profileId == null) {
                flowOf(emptyList())
            } else {
                programRepository.getAllWithExercises(profileId)
            }
        }

    val programs: StateFlow<List<ProgramWithExercises>> = combine(
        unsortedPrograms,
        lastWorkoutTimes
    ) { programs, times ->
        programs.sortedByDescending { times[it.program.id] ?: 0L }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch { currentProfileHolder.ensureLoaded() }
    }

    suspend fun getActiveWorkoutId(): Long? = workoutRepository.getActiveWorkout()?.id

    suspend fun createWorkoutFromProgram(programId: Long): Long {
        val profileId = checkNotNull(currentProfileHolder.profileId.value) { "Profile not loaded" }
        val programWithExercises = programRepository.getProgramWithExercises(programId)
            ?: throw IllegalArgumentException("Program not found: $programId")

        val workout = Workout(
            profileId = profileId,
            programName = programWithExercises.program.name,
            programId = programWithExercises.program.id,
            startTime = System.currentTimeMillis(),
            endTime = null
        )
        val previousSets = buildPreviousSetsMap(programWithExercises.program.id)
        val exercisesWithSets = programWithExercises.exercises.map { peWithSets ->
            val we = WorkoutExercise(
                workoutId = 0L,
                exerciseName = peWithSets.exercise.name,
                exerciseId = peWithSets.exercise.id,
                orderIndex = peWithSets.programExercise.orderIndex
            )
            val sets = peWithSets.sets.map { programSet ->
                val prev = previousSets[peWithSets.exercise.id to programSet.setIndex]
                WorkoutSet(
                    workoutExerciseId = 0L,
                    setIndex = programSet.setIndex,
                    weightThousandths = prev?.first ?: programSet.weightThousandths,
                    reps = prev?.second ?: programSet.reps,
                    restTimeSeconds = programSet.restTimeSeconds,
                    completed = false
                )
            }
            we to sets
        }
        return workoutRepository.insertWorkoutWithExercises(workout, exercisesWithSets)
    }

    private suspend fun buildPreviousSetsMap(
        programId: Long
    ): Map<Pair<Long, Int>, Pair<Int, Int>> {
        val previous = workoutRepository.getLastFinishedByProgramId(programId, excludeWorkoutId = 0)
            ?: return emptyMap()
        val map = mutableMapOf<Pair<Long, Int>, Pair<Int, Int>>()
        for (exercise in previous.exercises) {
            val exId = exercise.workoutExercise.exerciseId ?: continue
            for (set in exercise.sets.filter { it.completed }) {
                map[exId to set.setIndex] = set.weightThousandths to set.reps
            }
        }
        return map
    }

    suspend fun createEmptyWorkout(): Long {
        val profileId = checkNotNull(currentProfileHolder.profileId.value) { "Profile not loaded" }
        val workout = Workout(
            profileId = profileId,
            programName = null,
            startTime = System.currentTimeMillis(),
            endTime = null
        )
        return workoutRepository.insert(workout)
    }
}
