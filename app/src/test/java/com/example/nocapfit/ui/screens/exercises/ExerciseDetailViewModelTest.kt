package com.example.nocapfit.ui.screens.exercises

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.nocapfit.MainDispatcherRule
import com.example.nocapfit.data.db.entity.Exercise
import com.example.nocapfit.data.repository.ExerciseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
        coEvery { exerciseRepository.getById(1L) } returns testExercise
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
    fun showEditDialog_setsStateTrue() = runTest {
        val viewModel = createViewModel()

        viewModel.showEditDialog()

        viewModel.showEditDialog.test { assertTrue(awaitItem()) }
    }

    @Test
    fun dismissEditDialog_setsStateFalse() = runTest {
        val viewModel = createViewModel()

        viewModel.showEditDialog()
        viewModel.dismissEditDialog()

        viewModel.showEditDialog.test { assertFalse(awaitItem()) }
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
    fun updateExercise_callsRepositoryAndUpdatesState() = runTest {
        val viewModel = createViewModel()
        val updated = testExercise.copy(name = "Incline Bench Press")

        viewModel.showEditDialog()
        viewModel.updateExercise(updated)

        coVerify { exerciseRepository.update(updated) }
        viewModel.exercise.test { assertEquals(updated, awaitItem()) }
        viewModel.showEditDialog.test { assertFalse(awaitItem()) }
    }

    @Test
    fun deleteExercise_callsRepositoryAndInvokesCallback() = runTest {
        val viewModel = createViewModel()
        var callbackInvoked = false

        viewModel.deleteExercise { callbackInvoked = true }

        coVerify { exerciseRepository.delete(testExercise) }
        assertTrue(callbackInvoked)
    }

    @Test
    fun exercise_returnsNullWhenNotFound() = runTest {
        coEvery { exerciseRepository.getById(99L) } returns null
        val savedStateHandle = SavedStateHandle(mapOf("exerciseId" to 99L))
        val viewModel = ExerciseDetailViewModel(exerciseRepository, savedStateHandle)

        viewModel.exercise.test {
            assertNull(awaitItem())
        }
    }
}
