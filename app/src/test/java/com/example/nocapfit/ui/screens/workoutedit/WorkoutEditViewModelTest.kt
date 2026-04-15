package com.example.nocapfit.ui.screens.workoutedit

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.nocapfit.MainDispatcherRule
import com.example.nocapfit.data.db.entity.Profile
import com.example.nocapfit.data.db.entity.Workout
import com.example.nocapfit.data.db.entity.WorkoutExercise
import com.example.nocapfit.data.db.entity.WorkoutSet
import com.example.nocapfit.data.db.relation.WorkoutExerciseWithSets
import com.example.nocapfit.data.db.relation.WorkoutWithExercises
import com.example.nocapfit.data.repository.ExerciseRepository
import com.example.nocapfit.data.repository.ProfileRepository
import com.example.nocapfit.data.repository.WorkoutRepository
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

class WorkoutEditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val workoutRepository = mockk<WorkoutRepository>(relaxUnitFun = true)
    private val exerciseRepository = mockk<ExerciseRepository>(relaxUnitFun = true)
    private val profileRepository = mockk<ProfileRepository>()

    private val testProfile = Profile(id = 1L, name = "Default")
    private val testWorkout = Workout(
        id = 10L,
        profileId = 1L,
        programName = "Push Day",
        startTime = 1000L,
        endTime = 2000L
    )
    private val testSet = WorkoutSet(
        id = 100L,
        workoutExerciseId = 50L,
        setIndex = 0,
        weightThousandths = 60000,
        reps = 10,
        restTimeSeconds = 90,
        completed = true
    )
    private val testExerciseWithSets = WorkoutExerciseWithSets(
        workoutExercise = WorkoutExercise(
            id = 50L,
            workoutId = 10L,
            exerciseName = "Bench Press",
            exerciseId = 1L,
            orderIndex = 0
        ),
        sets = listOf(testSet)
    )
    private val testExerciseWithSets2 = WorkoutExerciseWithSets(
        workoutExercise = WorkoutExercise(
            id = 51L,
            workoutId = 10L,
            exerciseName = "Squat",
            exerciseId = 2L,
            orderIndex = 1
        ),
        sets = listOf(
            WorkoutSet(
                id = 101L,
                workoutExerciseId = 51L,
                setIndex = 0,
                weightThousandths = 80000,
                reps = 8,
                restTimeSeconds = 120,
                completed = false
            )
        )
    )
    private val testData = WorkoutWithExercises(
        workout = testWorkout,
        exercises = listOf(testExerciseWithSets, testExerciseWithSets2)
    )

    private fun createViewModel(): WorkoutEditViewModel {
        coEvery { profileRepository.getDefault() } returns testProfile
        coEvery { workoutRepository.getWithExercises(10L) } returns testData
        every { exerciseRepository.getAllByProfile(1L) } returns flowOf(emptyList())
        val savedStateHandle = SavedStateHandle(mapOf("workoutId" to 10L))
        return WorkoutEditViewModel(
            workoutRepository,
            exerciseRepository,
            profileRepository,
            savedStateHandle
        )
    }

    @Test
    fun workout_loadsFromRepository() = runTest {
        val viewModel = createViewModel()

        viewModel.workout.test {
            assertEquals(testData, awaitItem())
        }
    }

    @Test
    fun programName_initializesFromWorkout() = runTest {
        val viewModel = createViewModel()

        viewModel.programName.test {
            assertEquals("Push Day", awaitItem())
        }
    }

    @Test
    fun updateProgramName_updatesState() = runTest {
        val viewModel = createViewModel()

        viewModel.programName.test { awaitItem() }

        viewModel.updateProgramName("Pull Day")
        assertEquals("Pull Day", viewModel.programName.value)
    }

    @Test
    fun updateSet_mutatesSnapshot() = runTest {
        val viewModel = createViewModel()
        viewModel.workout.test { awaitItem() }

        val updatedSet = testSet.copy(weightThousandths = 70000)
        viewModel.updateSet(updatedSet)

        val set = viewModel.workout.value!!.exercises[0].sets[0]
        assertEquals(70000, set.weightThousandths)
        coVerify(exactly = 0) { workoutRepository.updateWorkoutSet(any()) }
    }

    @Test
    fun toggleSetCompleted_flipsFlag() = runTest {
        val viewModel = createViewModel()
        viewModel.workout.test { awaitItem() }

        viewModel.toggleSetCompleted(testSet)

        val set = viewModel.workout.value!!.exercises[0].sets[0]
        assertFalse(set.completed)
    }

    @Test
    fun addSet_appendsToExercise() = runTest {
        val viewModel = createViewModel()
        viewModel.workout.test { awaitItem() }

        viewModel.addSet(50L)

        val sets = viewModel.workout.value!!.exercises[0].sets
        assertEquals(2, sets.size)
        val newSet = sets[1]
        assertEquals(1, newSet.setIndex)
        assertEquals(60000, newSet.weightThousandths)
        assertEquals(10, newSet.reps)
        assertFalse(newSet.completed)
    }

    @Test
    fun addSet_usesDefaultsWhenNoExistingSets() = runTest {
        val emptyExercise = testExerciseWithSets.copy(sets = emptyList())
        val data = testData.copy(exercises = listOf(emptyExercise))
        coEvery { profileRepository.getDefault() } returns testProfile
        coEvery { workoutRepository.getWithExercises(10L) } returns data
        every { exerciseRepository.getAllByProfile(1L) } returns flowOf(emptyList())
        val savedStateHandle = SavedStateHandle(mapOf("workoutId" to 10L))
        val viewModel = WorkoutEditViewModel(
            workoutRepository,
            exerciseRepository,
            profileRepository,
            savedStateHandle
        )
        viewModel.workout.test { awaitItem() }

        viewModel.addSet(50L)

        val sets = viewModel.workout.value!!.exercises[0].sets
        assertEquals(1, sets.size)
        assertEquals(0, sets[0].setIndex)
        assertEquals(0, sets[0].weightThousandths)
        assertEquals(60, sets[0].restTimeSeconds)
    }

    @Test
    fun removeExercise_removesFromSnapshot() = runTest {
        val viewModel = createViewModel()
        viewModel.workout.test { awaitItem() }

        viewModel.removeExercise(50L)

        val exercises = viewModel.workout.value!!.exercises
        assertEquals(1, exercises.size)
        assertEquals("Squat", exercises[0].workoutExercise.exerciseName)
    }

    @Test
    fun addExercise_appendsToSnapshot() = runTest {
        val viewModel = createViewModel()
        viewModel.workout.test { awaitItem() }

        viewModel.addExercise(3L, "Deadlift")

        val exercises = viewModel.workout.value!!.exercises
        assertEquals(3, exercises.size)
        val added = exercises[2]
        assertEquals("Deadlift", added.workoutExercise.exerciseName)
        assertEquals(3L, added.workoutExercise.exerciseId)
        assertEquals(2, added.workoutExercise.orderIndex)
        assertEquals(1, added.sets.size)
    }

    @Test
    fun moveExercise_swapsOrderIndexes() = runTest {
        val viewModel = createViewModel()
        viewModel.workout.test { awaitItem() }

        viewModel.moveExercise(50L, 1)

        val exercises = viewModel.workout.value!!.exercises
        val bench = exercises.first { it.workoutExercise.exerciseName == "Bench Press" }
        val squat = exercises.first { it.workoutExercise.exerciseName == "Squat" }
        assertEquals(1, bench.workoutExercise.orderIndex)
        assertEquals(0, squat.workoutExercise.orderIndex)
    }

    @Test
    fun moveExercise_outOfBounds_doesNothing() = runTest {
        val viewModel = createViewModel()
        viewModel.workout.test { awaitItem() }

        viewModel.moveExercise(50L, -1)

        val bench = viewModel.workout.value!!.exercises
            .first { it.workoutExercise.exerciseName == "Bench Press" }
        assertEquals(0, bench.workoutExercise.orderIndex)
    }

    @Test
    fun save_persistsSnapshotToRepository() = runTest {
        val viewModel = createViewModel()
        viewModel.workout.test { awaitItem() }

        viewModel.updateProgramName("Pull Day")
        viewModel.updateSet(testSet.copy(weightThousandths = 70000))

        var saved = false
        viewModel.save { saved = true }

        coVerify {
            workoutRepository.saveWorkoutEdits(
                match { it.id == 10L && it.programName == "Pull Day" },
                match { exercises ->
                    exercises.size == 2 &&
                        exercises[0].first.id == 0L &&
                        exercises[0].second[0].id == 0L &&
                        exercises[0].second[0].weightThousandths == 70000
                }
            )
        }
        assertTrue(saved)
    }

    @Test
    fun editsDoNotTouchRepository() = runTest {
        val viewModel = createViewModel()
        viewModel.workout.test { awaitItem() }

        viewModel.updateSet(testSet.copy(weightThousandths = 70000))
        viewModel.toggleSetCompleted(testSet)
        viewModel.addSet(50L)
        viewModel.removeExercise(51L)
        viewModel.addExercise(3L, "Deadlift")

        coVerify(exactly = 0) { workoutRepository.updateWorkoutSet(any()) }
        coVerify(exactly = 0) { workoutRepository.insertWorkoutSet(any()) }
        coVerify(exactly = 0) { workoutRepository.deleteWorkoutExercise(any()) }
        coVerify(exactly = 0) { workoutRepository.insertWorkoutExercise(any()) }
    }

    @Test
    fun save_doesNothingWhenNoWorkout() = runTest {
        coEvery { profileRepository.getDefault() } returns testProfile
        coEvery { workoutRepository.getWithExercises(99L) } returns null
        every { exerciseRepository.getAllByProfile(1L) } returns flowOf(emptyList())
        val savedStateHandle = SavedStateHandle(mapOf("workoutId" to 99L))
        val viewModel = WorkoutEditViewModel(
            workoutRepository,
            exerciseRepository,
            profileRepository,
            savedStateHandle
        )

        assertNull(viewModel.workout.value)

        var saved = false
        viewModel.save { saved = true }

        coVerify(exactly = 0) { workoutRepository.saveWorkoutEdits(any(), any()) }
        assertFalse(saved)
    }
}
