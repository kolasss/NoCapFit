package com.example.nocapfit.ui.screens.exercises

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.nocapfit.MainDispatcherRule
import com.example.nocapfit.data.db.entity.Exercise
import com.example.nocapfit.data.repository.ExerciseRepository
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

class ExerciseDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val exerciseRepository = mockk<ExerciseRepository>(relaxUnitFun = true)

    private val testExercise = Exercise(
        id = 1L,
        profileId = 1L,
        name = "Bench Press",
        description = "**Setup:** Lie on bench\n\n**Execution:** Press up",
        tags = "Chest, Pectoralis Major, Triceps"
    )

    private fun createViewModel(): ExerciseDetailViewModel {
        every { exerciseRepository.getByIdFlow(1L) } returns flowOf(testExercise)
        val savedStateHandle = SavedStateHandle(mapOf("exerciseId" to 1L))
        return ExerciseDetailViewModel(exerciseRepository, savedStateHandle)
    }

    @Test
    fun exercise_loadsFromRepository() = runTest {
        val viewModel = createViewModel()

        viewModel.exercise.test {
            assertEquals(testExercise, awaitItem())
        }
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
    fun deleteExercise_callsRepositoryAndInvokesCallback() = runTest {
        val viewModel = createViewModel()
        var callbackInvoked = false

        // Wait for exercise to load before deleting
        viewModel.exercise.test {
            assertEquals(testExercise, awaitItem())
        }

        viewModel.deleteExercise { callbackInvoked = true }

        coVerify { exerciseRepository.delete(testExercise) }
        assertTrue(callbackInvoked)
    }

    @Test
    fun exercise_returnsNullWhenNotFound() = runTest {
        every { exerciseRepository.getByIdFlow(99L) } returns flowOf(null)
        val savedStateHandle = SavedStateHandle(mapOf("exerciseId" to 99L))
        val viewModel = ExerciseDetailViewModel(exerciseRepository, savedStateHandle)

        viewModel.exercise.test {
            assertNull(awaitItem())
        }
    }
}
