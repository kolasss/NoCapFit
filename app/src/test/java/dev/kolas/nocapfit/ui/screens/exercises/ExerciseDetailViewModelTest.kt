package dev.kolas.nocapfit.ui.screens.exercises

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import dev.kolas.nocapfit.MainDispatcherRule
import dev.kolas.nocapfit.data.db.dao.ExerciseHistorySetRow
import dev.kolas.nocapfit.data.db.entity.Exercise
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
        every { workoutRepository.getExerciseHistory(1L) } returns flowOf(emptyList())
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
    fun exerciseHistory_groupsRowsByWorkout() = runTest {
        val rows = listOf(
            ExerciseHistorySetRow(
                workoutId = 10L,
                programName = "Push Day",
                startTime = 1000L,
                setIndex = 0,
                weightThousandths = 60000,
                reps = 10
            ),
            ExerciseHistorySetRow(
                workoutId = 10L,
                programName = "Push Day",
                startTime = 1000L,
                setIndex = 1,
                weightThousandths = 65000,
                reps = 8
            ),
            // Workout with the exercise but no completed sets (LEFT JOIN null row)
            ExerciseHistorySetRow(
                workoutId = 11L,
                programName = null,
                startTime = 2000L,
                setIndex = null,
                weightThousandths = null,
                reps = null
            )
        )
        every { exerciseRepository.getByIdFlow(1L) } returns flowOf(testExercise)
        every { workoutRepository.getExerciseHistory(1L) } returns flowOf(rows)
        val savedStateHandle = SavedStateHandle(mapOf("exerciseId" to 1L))
        val viewModel = ExerciseDetailViewModel(exerciseRepository, workoutRepository, savedStateHandle)

        viewModel.exerciseHistory.test {
            val entries = awaitItem()
            assertEquals(2, entries.size)
            assertEquals("Push Day", entries[0].title)
            assertEquals(2, entries[0].sets.size)
            assertEquals(60000, entries[0].sets[0].weightThousandths)
            assertEquals(listOf(1, 2), entries[0].sets.map { it.setNumber })
            assertEquals("Free Workout", entries[1].title)
            assertTrue(entries[1].sets.isEmpty())
        }
    }

    @Test
    fun exerciseHistory_numbersSetsSequentiallyWhenExerciseAppearsTwiceInWorkout() {
        // Same exercise added twice to one workout (superset): per-instance setIndex repeats.
        // Rows arrive in query order (exercise position, then setIndex).
        val rows = listOf(
            ExerciseHistorySetRow(
                workoutId = 10L,
                programName = "Push Day",
                startTime = 1000L,
                setIndex = 0,
                weightThousandths = 60000,
                reps = 10
            ),
            ExerciseHistorySetRow(
                workoutId = 10L,
                programName = "Push Day",
                startTime = 1000L,
                setIndex = 1,
                weightThousandths = 65000,
                reps = 8
            ),
            ExerciseHistorySetRow(
                workoutId = 10L,
                programName = "Push Day",
                startTime = 1000L,
                setIndex = 0,
                weightThousandths = 40000,
                reps = 12
            ),
            ExerciseHistorySetRow(
                workoutId = 10L,
                programName = "Push Day",
                startTime = 1000L,
                setIndex = 1,
                weightThousandths = 45000,
                reps = 10
            )
        )

        val entries = groupExerciseHistory(rows)

        assertEquals(1, entries.size)
        assertEquals(listOf(1, 2, 3, 4), entries.single().sets.map { it.setNumber })
        assertEquals(
            listOf(60000, 65000, 40000, 45000),
            entries.single().sets.map { it.weightThousandths }
        )
    }

    @Test
    fun exerciseHistory_emptyWhenNoWorkouts() = runTest {
        val viewModel = createViewModel()

        viewModel.exerciseHistory.test {
            assertEquals(emptyList<ExerciseHistoryEntryUi>(), awaitItem())
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
        every { workoutRepository.getExerciseHistory(99L) } returns flowOf(emptyList())
        val savedStateHandle = SavedStateHandle(mapOf("exerciseId" to 99L))
        val viewModel = ExerciseDetailViewModel(exerciseRepository, workoutRepository, savedStateHandle)

        viewModel.exercise.test {
            assertNull(awaitItem())
        }
    }
}
