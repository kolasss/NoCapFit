package dev.kolas.nocapfit.ui.screens.exercises

import app.cash.turbine.test
import dev.kolas.nocapfit.MainDispatcherRule
import dev.kolas.nocapfit.data.db.entity.Exercise
import dev.kolas.nocapfit.data.db.entity.Profile
import dev.kolas.nocapfit.data.repository.ExerciseRepository
import dev.kolas.nocapfit.data.repository.ProfileRepository
import dev.kolas.nocapfit.data.session.CurrentProfileHolder
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
        return ExerciseListViewModel(exerciseRepository, CurrentProfileHolder(profileRepository))
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
        every { exerciseRepository.searchByName(1L, "bench") } returns flowOf(searchResults)

        viewModel.exercises.test {
            awaitItem()

            viewModel.updateSearchQuery("bench")
            assertEquals(searchResults, awaitItem())
        }
    }
}
