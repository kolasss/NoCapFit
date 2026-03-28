package com.example.nocapfit.ui.screens.workout

import androidx.lifecycle.SavedStateHandle
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
    private val testWorkoutWithExercises = WorkoutWithExercises(
        workout = testWorkout,
        exercises = listOf(
            WorkoutExerciseWithSets(
                workoutExercise = WorkoutExercise(
                    id = 100L,
                    workoutId = 1L,
                    exerciseName = "Bench Press",
                    orderIndex = 0
                ),
                sets = listOf(testSet)
            )
        )
    )

    private fun createViewModel(): WorkoutInProgressViewModel {
        coEvery { workoutRepository.getWithExercisesFlow(1L) } returns MutableStateFlow(testWorkoutWithExercises)
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

        viewModel.cancelWorkout()

        coVerify { timerCoordinator.cancelTimer() }
        coVerify { timerRepository.deleteByWorkoutId(1L) }
        coVerify { workoutRepository.delete(testWorkout) }
    }
}
