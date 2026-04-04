package com.example.nocapfit.ui.screens.workout

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.nocapfit.MainDispatcherRule
import com.example.nocapfit.data.db.entity.Workout
import com.example.nocapfit.data.db.entity.WorkoutExercise
import com.example.nocapfit.data.db.entity.WorkoutSet
import com.example.nocapfit.data.db.relation.WorkoutExerciseWithSets
import com.example.nocapfit.data.db.relation.WorkoutWithExercises
import com.example.nocapfit.data.repository.ExerciseRepository
import com.example.nocapfit.data.repository.ProfileRepository
import com.example.nocapfit.data.repository.TimerRepository
import com.example.nocapfit.data.repository.WorkoutRepository
import com.example.nocapfit.service.TimerCoordinator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WorkoutInProgressViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val workoutRepository = mockk<WorkoutRepository>(relaxUnitFun = true)
    private val timerRepository = mockk<TimerRepository>(relaxUnitFun = true)
    private val exerciseRepository = mockk<ExerciseRepository>(relaxUnitFun = true) {
        coEvery { getAllByProfile(any()) } returns flowOf(emptyList())
    }
    private val profileRepository = mockk<ProfileRepository>(relaxUnitFun = true) {
        coEvery { getDefault() } returns null
    }
    private val timerCoordinator = mockk<TimerCoordinator>(relaxUnitFun = true) {
        coEvery { timerState } returns MutableStateFlow(TimerCoordinator.TimerUiState.Idle)
        coEvery { reconstructState() } returns Unit
    }

    private val testWorkout = Workout(id = 1L, profileId = 1L, startTime = 1000L)
    private val testSet = WorkoutSet(
        id = 10L,
        workoutExerciseId = 100L,
        setIndex = 0,
        weightThousandths = 50000,
        reps = 8,
        restTimeSeconds = 60
    )
    private val testExercise1 = WorkoutExerciseWithSets(
        workoutExercise = WorkoutExercise(
            id = 100L,
            workoutId = 1L,
            exerciseName = "Bench Press",
            orderIndex = 0
        ),
        sets = listOf(testSet)
    )
    private val testExercise2 = WorkoutExerciseWithSets(
        workoutExercise = WorkoutExercise(
            id = 200L,
            workoutId = 1L,
            exerciseName = "Squat",
            orderIndex = 1
        ),
        sets = listOf(
            WorkoutSet(
                id = 20L,
                workoutExerciseId = 200L,
                setIndex = 0,
                weightThousandths = 80000,
                reps = 5,
                restTimeSeconds = 90
            )
        )
    )
    private val testWorkoutWithExercises = WorkoutWithExercises(
        workout = testWorkout,
        exercises = listOf(testExercise1, testExercise2)
    )

    private fun createViewModel(): WorkoutInProgressViewModel {
        coEvery { workoutRepository.getWithExercisesFlow(1L) } returns MutableStateFlow(testWorkoutWithExercises)
        coEvery { workoutRepository.getWithExercises(1L) } returns testWorkoutWithExercises
        val savedStateHandle = SavedStateHandle(mapOf("workoutId" to 1L))
        return WorkoutInProgressViewModel(
            workoutRepository,
            timerRepository,
            exerciseRepository,
            profileRepository,
            savedStateHandle,
            timerCoordinator
        )
    }

    @Test
    fun completeSet_marksCompletedAndStartsTimer() = runTest {
        val viewModel = createViewModel()

        viewModel.workout.test { awaitItem() }
        viewModel.completeSet(10L, 60)

        coVerify {
            workoutRepository.updateWorkoutSet(match { it.id == 10L && it.completed })
        }
        coVerify {
            timerCoordinator.startTimer(workoutId = 1L, workoutSetId = 10L, durationSeconds = 60)
        }
    }

    @Test
    fun revertSet_revertsCompletedFlagAndCancelsMatchingTimer() = runTest {
        val runningState = TimerCoordinator.TimerUiState.Running(
            timerId = 1L,
            endAtEpochMs = System.currentTimeMillis() + 30_000L,
            workoutId = 1L,
            workoutSetId = 10L,
            totalMs = 60_000L
        )
        coEvery { timerCoordinator.timerState } returns MutableStateFlow(runningState)

        val viewModel = createViewModel()

        viewModel.workout.test { awaitItem() }
        viewModel.revertSet(10L)

        coVerify {
            workoutRepository.updateWorkoutSet(match { it.id == 10L && !it.completed })
        }
        coVerify { timerCoordinator.cancelTimer() }
    }

    @Test
    fun revertSet_doesNotCancelTimerForDifferentSet() = runTest {
        val runningState = TimerCoordinator.TimerUiState.Running(
            timerId = 1L,
            endAtEpochMs = System.currentTimeMillis() + 30_000L,
            workoutId = 1L,
            workoutSetId = 99L,
            totalMs = 60_000L
        )
        coEvery { timerCoordinator.timerState } returns MutableStateFlow(runningState)

        val viewModel = createViewModel()

        viewModel.workout.test { awaitItem() }
        viewModel.revertSet(10L)

        coVerify {
            workoutRepository.updateWorkoutSet(match { it.id == 10L && !it.completed })
        }
        coVerify(exactly = 0) { timerCoordinator.cancelTimer() }
    }

    @Test
    fun addSet_copiesLastSetValues() = runTest {
        coEvery { workoutRepository.getSetsForExercise(100L) } returns listOf(testSet)
        coEvery { workoutRepository.insertWorkoutSet(any()) } returns 11L

        val viewModel = createViewModel()
        viewModel.addSet(100L)

        coVerify {
            workoutRepository.insertWorkoutSet(
                match {
                    it.weightThousandths == 50000 && it.reps == 8 && it.restTimeSeconds == 60 && it.setIndex == 1
                }
            )
        }
    }

    @Test
    fun addSet_defaultsWhenEmpty() = runTest {
        coEvery { workoutRepository.getSetsForExercise(100L) } returns emptyList()
        coEvery { workoutRepository.insertWorkoutSet(any()) } returns 11L

        val viewModel = createViewModel()
        viewModel.addSet(100L)

        coVerify {
            workoutRepository.insertWorkoutSet(
                match {
                    it.weightThousandths == 0 && it.reps == 0 && it.restTimeSeconds == 60 && it.setIndex == 0
                }
            )
        }
    }

    @Test
    fun finishWorkout_setsEndTimeAndCancelsTimer() = runTest {
        val viewModel = createViewModel()

        viewModel.workout.test { awaitItem() }
        val result = viewModel.finishWorkout()

        assertTrue(result)
        coVerify {
            workoutRepository.update(match { it.id == 1L && it.endTime != null })
        }
        coVerify { timerCoordinator.cancelTimer() }
    }

    @Test
    fun cancelWorkout_deletesWorkoutAndTimers() = runTest {
        val viewModel = createViewModel()

        viewModel.workout.test { awaitItem() }
        viewModel.cancelWorkout()

        coVerify { timerCoordinator.cancelTimer() }
        coVerify { timerRepository.deleteByWorkoutId(1L) }
        coVerify { workoutRepository.delete(testWorkout) }
    }

    @Test
    fun moveExercise_swapsOrderIndexes() = runTest {
        val viewModel = createViewModel()

        viewModel.workout.test { awaitItem() }
        viewModel.moveExercise(100L, 1)

        coVerify {
            workoutRepository.swapExerciseOrder(
                match { it.id == 100L && it.orderIndex == 1 },
                match { it.id == 200L && it.orderIndex == 0 }
            )
        }
    }

    @Test
    fun moveExercise_outOfBounds_doesNothing() = runTest {
        val viewModel = createViewModel()

        viewModel.workout.test { awaitItem() }
        viewModel.moveExercise(100L, -1)

        coVerify(exactly = 0) { workoutRepository.swapExerciseOrder(any(), any()) }
    }

    @Test
    fun previousSets_loadsFromPreviousWorkout() = runTest {
        val workoutWithProgram = WorkoutWithExercises(
            workout = Workout(id = 1L, profileId = 1L, startTime = 1000L, programId = 5L),
            exercises = listOf(
                WorkoutExerciseWithSets(
                    workoutExercise = WorkoutExercise(
                        id = 100L,
                        workoutId = 1L,
                        exerciseName = "Bench Press",
                        exerciseId = 1L,
                        orderIndex = 0
                    ),
                    sets = listOf(testSet)
                )
            )
        )
        val previousWorkout = WorkoutWithExercises(
            workout = Workout(id = 99L, profileId = 1L, startTime = 500L, endTime = 900L, programId = 5L),
            exercises = listOf(
                WorkoutExerciseWithSets(
                    workoutExercise = WorkoutExercise(
                        id = 300L,
                        workoutId = 99L,
                        exerciseName = "Bench Press",
                        exerciseId = 1L,
                        orderIndex = 0
                    ),
                    sets = listOf(
                        WorkoutSet(
                            id = 30L,
                            workoutExerciseId = 300L,
                            setIndex = 0,
                            weightThousandths = 60000,
                            reps = 10,
                            restTimeSeconds = 60,
                            completed = true
                        )
                    )
                )
            )
        )
        coEvery { workoutRepository.getWithExercisesFlow(1L) } returns MutableStateFlow(workoutWithProgram)
        coEvery { workoutRepository.getWithExercises(1L) } returns workoutWithProgram
        coEvery { workoutRepository.getLastFinishedByProgramId(5L, 1L) } returns previousWorkout
        val savedStateHandle = SavedStateHandle(mapOf("workoutId" to 1L))
        val viewModel = WorkoutInProgressViewModel(
            workoutRepository,
            timerRepository,
            exerciseRepository,
            profileRepository,
            savedStateHandle,
            timerCoordinator
        )

        val prev = viewModel.previousSets.value
        val key = 1L to 0
        assertEquals(60000, prev[key]?.weightThousandths)
        assertEquals(10, prev[key]?.reps)
    }

    @Test
    fun previousSets_emptyForFreeWorkout() = runTest {
        val viewModel = createViewModel()

        assertTrue(viewModel.previousSets.value.isEmpty())
    }
}
