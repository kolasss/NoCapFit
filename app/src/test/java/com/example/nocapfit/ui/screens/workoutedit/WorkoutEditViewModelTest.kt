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
    private val testData = WorkoutWithExercises(
        workout = testWorkout,
        exercises = listOf(testExerciseWithSets)
    )

    private fun createViewModel(): WorkoutEditViewModel {
        coEvery { profileRepository.getDefault() } returns testProfile
        every { workoutRepository.getWithExercisesFlow(10L) } returns flowOf(testData)
        coEvery { workoutRepository.getWithExercises(10L) } returns testData
        every { exerciseRepository.getAllByProfile(1L) } returns flowOf(emptyList())
        coEvery { workoutRepository.insertWorkoutExercise(any()) } returns 60L
        coEvery { workoutRepository.insertWorkoutSet(any()) } returns 200L
        val savedStateHandle = SavedStateHandle(mapOf("workoutId" to 10L))
        return WorkoutEditViewModel(
            workoutRepository, exerciseRepository, profileRepository, savedStateHandle
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
    fun saveProgramName_callsRepositoryUpdate() = runTest {
        val viewModel = createViewModel()

        viewModel.workout.test { awaitItem() }
        viewModel.programName.test { awaitItem() }

        viewModel.updateProgramName("Pull Day")
        viewModel.saveProgramName()

        coVerify { workoutRepository.update(match { it.programName == "Pull Day" }) }
    }

    @Test
    fun saveProgramName_skipsWhenUnchanged() = runTest {
        val viewModel = createViewModel()

        viewModel.workout.test { awaitItem() }
        viewModel.programName.test { awaitItem() }

        viewModel.saveProgramName()

        coVerify(exactly = 0) { workoutRepository.update(any()) }
    }

    @Test
    fun updateSet_delegatesToRepository() = runTest {
        val viewModel = createViewModel()
        val updatedSet = testSet.copy(weightThousandths = 70000)

        viewModel.updateSet(updatedSet)

        coVerify { workoutRepository.updateWorkoutSet(updatedSet) }
    }

    @Test
    fun toggleSetCompleted_flipsCompletedFlag() = runTest {
        val viewModel = createViewModel()

        viewModel.toggleSetCompleted(testSet)

        coVerify {
            workoutRepository.updateWorkoutSet(match { it.id == 100L && !it.completed })
        }
    }

    @Test
    fun addSet_copiesLastSetDefaults() = runTest {
        coEvery { workoutRepository.getSetsForExercise(50L) } returns listOf(testSet)
        val viewModel = createViewModel()

        viewModel.addSet(50L)

        coVerify {
            workoutRepository.insertWorkoutSet(
                match {
                    it.workoutExerciseId == 50L &&
                        it.setIndex == 1 &&
                        it.weightThousandths == 60000 &&
                        it.reps == 10 &&
                        it.restTimeSeconds == 90 &&
                        !it.completed
                }
            )
        }
    }

    @Test
    fun addSet_usesDefaultsWhenNoExistingSets() = runTest {
        coEvery { workoutRepository.getSetsForExercise(50L) } returns emptyList()
        val viewModel = createViewModel()

        viewModel.addSet(50L)

        coVerify {
            workoutRepository.insertWorkoutSet(
                match {
                    it.setIndex == 0 &&
                        it.weightThousandths == 0 &&
                        it.reps == 0 &&
                        it.restTimeSeconds == 60
                }
            )
        }
    }

    @Test
    fun removeExercise_delegatesToRepository() = runTest {
        val viewModel = createViewModel()

        viewModel.removeExercise(50L)

        coVerify { workoutRepository.deleteWorkoutExercise(50L) }
    }

    @Test
    fun addExercise_insertsExerciseAndDefaultSet() = runTest {
        val viewModel = createViewModel()

        viewModel.workout.test { awaitItem() }

        viewModel.addExercise(2L, "Squat")

        coVerify {
            workoutRepository.insertWorkoutExercise(
                match {
                    it.workoutId == 10L &&
                        it.exerciseName == "Squat" &&
                        it.exerciseId == 2L &&
                        it.orderIndex == 1
                }
            )
        }
        coVerify {
            workoutRepository.insertWorkoutSet(
                match {
                    it.workoutExerciseId == 60L &&
                        it.setIndex == 0 &&
                        it.weightThousandths == 0
                }
            )
        }
    }
}
