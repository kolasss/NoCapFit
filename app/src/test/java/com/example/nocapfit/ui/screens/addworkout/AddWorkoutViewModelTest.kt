package com.example.nocapfit.ui.screens.addworkout

import app.cash.turbine.test
import com.example.nocapfit.MainDispatcherRule
import com.example.nocapfit.data.db.entity.Exercise
import com.example.nocapfit.data.db.entity.Profile
import com.example.nocapfit.data.db.entity.Program
import com.example.nocapfit.data.db.entity.ProgramExercise
import com.example.nocapfit.data.db.entity.ProgramExerciseSet
import com.example.nocapfit.data.db.relation.ProgramExerciseWithSets
import com.example.nocapfit.data.db.relation.ProgramWithExercises
import com.example.nocapfit.data.repository.ProfileRepository
import com.example.nocapfit.data.repository.ProgramRepository
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

class AddWorkoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val programRepository = mockk<ProgramRepository>(relaxUnitFun = true)
    private val workoutRepository = mockk<WorkoutRepository>(relaxUnitFun = true)
    private val profileRepository = mockk<ProfileRepository>()

    private val testProfile = Profile(id = 1L, name = "Default")

    private val testProgram = ProgramWithExercises(
        program = Program(id = 10L, profileId = 1L, name = "Push Day"),
        exercises = listOf(
            ProgramExerciseWithSets(
                programExercise = ProgramExercise(
                    id = 100L,
                    programId = 10L,
                    exerciseId = 1L,
                    orderIndex = 0
                ),
                exercise = Exercise(id = 1L, profileId = 1L, name = "Bench Press"),
                sets = listOf(
                    ProgramExerciseSet(
                        id = 1000L,
                        programExerciseId = 100L,
                        setIndex = 0,
                        weightThousandths = 60000,
                        reps = 10,
                        restTimeSeconds = 90
                    )
                )
            )
        )
    )

    private fun createViewModel(): AddWorkoutViewModel {
        coEvery { profileRepository.getDefault() } returns testProfile
        every { programRepository.getAllWithExercises(1L) } returns flowOf(listOf(testProgram))
        return AddWorkoutViewModel(programRepository, workoutRepository, profileRepository)
    }

    @Test
    fun programs_emitsProgramsAfterProfileLoads() = runTest {
        val viewModel = createViewModel()

        viewModel.programs.test {
            assertEquals(listOf(testProgram), awaitItem())
        }
    }

    @Test
    fun createEmptyWorkout_insertsWorkoutAndReturnsId() = runTest {
        coEvery { workoutRepository.insert(any()) } returns 42L
        val viewModel = createViewModel()

        val workoutId = viewModel.createEmptyWorkout()

        assertEquals(42L, workoutId)
        coVerify {
            workoutRepository.insert(
                match {
                    it.profileId == 1L && it.programName == null && it.endTime == null
                }
            )
        }
    }

    @Test(expected = IllegalStateException::class)
    fun createEmptyWorkout_throwsWhenProfileNotLoaded() = runTest {
        coEvery { profileRepository.getDefault() } returns null
        every { programRepository.getAllWithExercises(any()) } returns flowOf(emptyList())
        val viewModel = AddWorkoutViewModel(programRepository, workoutRepository, profileRepository)

        viewModel.createEmptyWorkout()
    }

    @Test
    fun createWorkoutFromProgram_insertsWorkoutExercisesAndSets() = runTest {
        coEvery { workoutRepository.insert(any()) } returns 42L
        coEvery { workoutRepository.insertWorkoutExercise(any()) } returns 200L
        coEvery { workoutRepository.insertWorkoutSet(any()) } returns 300L
        coEvery { programRepository.getProgramWithExercises(10L) } returns testProgram
        val viewModel = createViewModel()

        val workoutId = viewModel.createWorkoutFromProgram(10L)

        assertEquals(42L, workoutId)
        coVerify {
            workoutRepository.insert(match { it.programName == "Push Day" && it.profileId == 1L })
        }
        coVerify {
            workoutRepository.insertWorkoutExercise(
                match {
                    it.workoutId == 42L &&
                        it.exerciseName == "Bench Press" &&
                        it.exerciseId == 1L &&
                        it.orderIndex == 0
                }
            )
        }
        coVerify {
            workoutRepository.insertWorkoutSet(
                match {
                    it.workoutExerciseId == 200L &&
                        it.setIndex == 0 &&
                        it.weightThousandths == 60000 &&
                        it.reps == 10 &&
                        it.restTimeSeconds == 90 &&
                        !it.completed
                }
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun createWorkoutFromProgram_throwsWhenProgramNotFound() = runTest {
        coEvery { programRepository.getProgramWithExercises(999L) } returns null
        val viewModel = createViewModel()

        viewModel.createWorkoutFromProgram(999L)
    }
}
