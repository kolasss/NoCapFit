package com.example.nocapfit.ui.screens.workoutdetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.nocapfit.MainDispatcherRule
import com.example.nocapfit.data.db.entity.Workout
import com.example.nocapfit.data.db.relation.WorkoutWithExercises
import com.example.nocapfit.data.repository.WorkoutRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WorkoutDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val workoutRepository = mockk<WorkoutRepository>(relaxUnitFun = true)

    private val testWorkout = Workout(
        id = 1L,
        profileId = 1L,
        programName = "Push Day",
        startTime = 1000L,
        endTime = 2000L
    )
    private val testData = WorkoutWithExercises(
        workout = testWorkout,
        exercises = emptyList()
    )

    private fun createViewModel(workoutId: Long = 1L): WorkoutDetailViewModel {
        every { workoutRepository.getWithExercisesFlow(workoutId) } returns flowOf(testData)
        val savedStateHandle = SavedStateHandle(mapOf("workoutId" to workoutId))
        return WorkoutDetailViewModel(workoutRepository, savedStateHandle)
    }

    @Test
    fun workoutWithExercises_loadsFromRepository() = runTest {
        val viewModel = createViewModel()

        viewModel.workoutWithExercises.test {
            assertEquals(testData, awaitItem())
        }
    }

    @Test
    fun workoutWithExercises_returnsNullWhenNotFound() = runTest {
        every { workoutRepository.getWithExercisesFlow(99L) } returns flowOf(null)
        val savedStateHandle = SavedStateHandle(mapOf("workoutId" to 99L))
        val viewModel = WorkoutDetailViewModel(workoutRepository, savedStateHandle)

        assertNull(viewModel.workoutWithExercises.value)
    }

    @Test
    fun showDeleteConfirmation_setsStateTrue() = runTest {
        val viewModel = createViewModel()

        viewModel.showDeleteConfirmation()

        viewModel.showDeleteConfirmation.test { assertTrue(awaitItem()) }
    }

    @Test
    fun dismissDeleteConfirmation_setsStateFalse() = runTest {
        val viewModel = createViewModel()

        viewModel.showDeleteConfirmation()
        viewModel.dismissDeleteConfirmation()

        viewModel.showDeleteConfirmation.test { assertFalse(awaitItem()) }
    }

    @Test
    fun deleteWorkout_callsRepositoryAndInvokesCallback() = runTest {
        val viewModel = createViewModel()

        viewModel.workoutWithExercises.test { awaitItem() }

        var callbackInvoked = false
        viewModel.deleteWorkout { callbackInvoked = true }

        coVerify { workoutRepository.delete(testWorkout) }
        assertTrue(callbackInvoked)
    }

    @Test
    fun deleteWorkout_doesNothingWhenNoWorkout() = runTest {
        every { workoutRepository.getWithExercisesFlow(99L) } returns flowOf(null)
        val viewModel = createViewModel(workoutId = 99L)

        var callbackInvoked = false
        viewModel.deleteWorkout { callbackInvoked = true }

        coVerify(exactly = 0) { workoutRepository.delete(any()) }
        assertFalse(callbackInvoked)
    }
}
