package com.example.nocapfit.ui.screens.exercises

import app.cash.turbine.test
import com.example.nocapfit.MainDispatcherRule
import com.example.nocapfit.data.db.entity.Exercise
import com.example.nocapfit.data.db.entity.Profile
import com.example.nocapfit.data.repository.ExerciseRepository
import com.example.nocapfit.data.repository.ProfileRepository
import io.mockk.coEvery
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

class ExerciseListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val exerciseRepository = mockk<ExerciseRepository>(relaxUnitFun = true)
    private val profileRepository = mockk<ProfileRepository>()

    private val testProfile = Profile(id = 1L, name = "Default")
    private val testExercises = listOf(
        Exercise(id = 1L, profileId = 1L, name = "Bench Press"),
        Exercise(id = 2L, profileId = 1L, name = "Squat")
    )

    private fun createViewModel(): ExerciseListViewModel {
        coEvery { profileRepository.getDefault() } returns testProfile
        every { exerciseRepository.getAllByProfile(1L) } returns flowOf(testExercises)
        every { exerciseRepository.searchByName(any(), any()) } returns flowOf(emptyList())
        return ExerciseListViewModel(exerciseRepository, profileRepository)
    }

    @Test
    fun exercises_loadFromRepository() = runTest {
        val viewModel = createViewModel()

        viewModel.exercises.test {
            assertEquals(testExercises, awaitItem())
        }
    }

    @Test
    fun searchQuery_triggersSearchByName() = runTest {
        val searchResults = listOf(testExercises[0])

        val viewModel = createViewModel()
        // Override the catch-all mock after createViewModel sets it up
        every { exerciseRepository.searchByName(1L, "bench") } returns flowOf(searchResults)

        viewModel.exercises.test {
            // Initial emission (all exercises)
            awaitItem()

            viewModel.updateSearchQuery("bench")
            assertEquals(searchResults, awaitItem())
        }
    }

    @Test
    fun showAddDialog_setsStateTrue() = runTest {
        val viewModel = createViewModel()

        viewModel.showAddDialog()

        viewModel.showAddDialog.test {
            assertTrue(awaitItem())
        }
    }

    @Test
    fun dismissAddDialog_setsStateFalse() = runTest {
        val viewModel = createViewModel()

        viewModel.showAddDialog()
        viewModel.dismissAddDialog()

        viewModel.showAddDialog.test {
            assertFalse(awaitItem())
        }
    }

    @Test
    fun showEditDialog_setsExerciseAndFlag() = runTest {
        val viewModel = createViewModel()
        val exercise = testExercises[0]

        viewModel.showEditDialog(exercise)

        viewModel.showEditDialog.test { assertTrue(awaitItem()) }
        viewModel.selectedExercise.test { assertEquals(exercise, awaitItem()) }
    }

    @Test
    fun dismissEditDialog_clearsState() = runTest {
        val viewModel = createViewModel()

        viewModel.showEditDialog(testExercises[0])
        viewModel.dismissEditDialog()

        viewModel.showEditDialog.test { assertFalse(awaitItem()) }
        viewModel.selectedExercise.test { assertNull(awaitItem()) }
    }

    @Test
    fun addExercise_callsRepositoryAndDismisses() = runTest {
        coEvery { exerciseRepository.insert(any()) } returns 3L
        val viewModel = createViewModel()

        viewModel.showAddDialog()
        viewModel.addExercise("Deadlift", "Compound lift", "back")

        coVerify {
            exerciseRepository.insert(
                match {
                    it.name == "Deadlift" && it.description == "Compound lift" && it.tags == "back"
                }
            )
        }
        viewModel.showAddDialog.test { assertFalse(awaitItem()) }
    }

    @Test
    fun deleteExercise_callsRepository() = runTest {
        val viewModel = createViewModel()

        viewModel.deleteExercise(testExercises[0])

        coVerify { exerciseRepository.delete(testExercises[0]) }
    }
}
