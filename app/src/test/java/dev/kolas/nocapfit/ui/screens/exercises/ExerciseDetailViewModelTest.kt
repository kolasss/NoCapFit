package dev.kolas.nocapfit.ui.screens.exercises

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import dev.kolas.nocapfit.MainDispatcherRule
import dev.kolas.nocapfit.data.db.entity.Exercise
import dev.kolas.nocapfit.data.db.entity.Workout
import dev.kolas.nocapfit.data.db.relation.WorkoutWithExercises
import dev.kolas.nocapfit.data.repository.ExerciseRepository
import dev.kolas.nocapfit.data.repository.WorkoutRepository
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
    private val workoutRepository = mockk<WorkoutRepository>(relaxUnitFun = true)

    private val testExercise = Exercise(
        id = 1L,
        profileId = 1L,
        name = "Bench Press",
        description = "**Setup:** Lie on bench\n\n**Execution:** Press up",
        tags = "Chest, Pectoralis Major, Triceps"
    )

    private fun createViewModel(): ExerciseDetailViewModel {
        every { exerciseRepository.getByIdFlow(1L) } returns flowOf(testExercise)
        every { workoutRepository.getFinishedByExerciseId(1L) } returns flowOf(emptyList())
        val savedStateHandle = SavedStateHandle(mapOf("exerciseId" to 1L))
        return ExerciseDetailViewModel(exerciseRepository, workoutRepository, savedStateHandle)
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
    fun exerciseHistory_loadsFromRepository() = runTest {
        val testHistory = listOf(
            WorkoutWithExercises(
                workout = Workout(id = 10L, profileId = 1L, startTime = 1000L, endTime = 2000L),
                exercises = emptyList()
            )
        )
        every { exerciseRepository.getByIdFlow(1L) } returns flowOf(testExercise)
        every { workoutRepository.getFinishedByExerciseId(1L) } returns flowOf(testHistory)
        val savedStateHandle = SavedStateHandle(mapOf("exerciseId" to 1L))
        val viewModel = ExerciseDetailViewModel(exerciseRepository, workoutRepository, savedStateHandle)

        viewModel.exerciseHistory.test {
            assertEquals(testHistory, awaitItem())
        }
    }

    @Test
    fun exerciseHistory_emptyWhenNoWorkouts() = runTest {
        val viewModel = createViewModel()

        viewModel.exerciseHistory.test {
            assertEquals(emptyList<WorkoutWithExercises>(), awaitItem())
        }
    }

    @Test
    fun selectedTab_defaultsToZero() = runTest {
        val viewModel = createViewModel()
        assertEquals(0, viewModel.selectedTab.value)
    }

    @Test
    fun selectTab_updatesState() = runTest {
        val viewModel = createViewModel()

        viewModel.selectTab(1)

        viewModel.selectedTab.test { assertEquals(1, awaitItem()) }
    }

    @Test
    fun selectTab_persistsAcrossAccess() = runTest {
        val viewModel = createViewModel()

        viewModel.selectTab(1)
        assertEquals(1, viewModel.selectedTab.value)

        viewModel.selectTab(0)
        assertEquals(0, viewModel.selectedTab.value)
    }

    @Test
    fun exercise_returnsNullWhenNotFound() = runTest {
        every { exerciseRepository.getByIdFlow(99L) } returns flowOf(null)
        every { workoutRepository.getFinishedByExerciseId(99L) } returns flowOf(emptyList())
        val savedStateHandle = SavedStateHandle(mapOf("exerciseId" to 99L))
        val viewModel = ExerciseDetailViewModel(exerciseRepository, workoutRepository, savedStateHandle)

        viewModel.exercise.test {
            assertNull(awaitItem())
        }
    }
}
