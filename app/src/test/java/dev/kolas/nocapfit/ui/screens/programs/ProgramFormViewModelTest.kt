package dev.kolas.nocapfit.ui.screens.programs

import androidx.lifecycle.SavedStateHandle
import dev.kolas.nocapfit.MainDispatcherRule
import dev.kolas.nocapfit.data.db.entity.Exercise
import dev.kolas.nocapfit.data.db.entity.Profile
import dev.kolas.nocapfit.data.repository.ExerciseRepository
import dev.kolas.nocapfit.data.repository.ProfileRepository
import dev.kolas.nocapfit.data.repository.ProgramRepository
import dev.kolas.nocapfit.data.repository.WorkoutRepository
import dev.kolas.nocapfit.data.session.CurrentProfileHolder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProgramFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val programRepository = mockk<ProgramRepository>(relaxUnitFun = true)
    private val exerciseRepository = mockk<ExerciseRepository>()
    private val profileRepository = mockk<ProfileRepository>()
    private val workoutRepository = mockk<WorkoutRepository>(relaxUnitFun = true)

    private val testProfile = Profile(id = 1L, name = "Default")
    private val testExercise = Exercise(id = 1L, profileId = 1L, name = "Bench Press")

    private fun createViewModel(programId: Long = -1L): ProgramFormViewModel {
        coEvery { profileRepository.getDefault() } returns testProfile
        every { exerciseRepository.getAllByProfile(1L) } returns flowOf(listOf(testExercise))
        coEvery { workoutRepository.getLastFinishedByExerciseId(any()) } returns null

        val savedStateHandle = SavedStateHandle(
            if (programId > 0) mapOf("programId" to programId) else emptyMap()
        )
        return ProgramFormViewModel(
            programRepository,
            exerciseRepository,
            CurrentProfileHolder(profileRepository),
            workoutRepository,
            savedStateHandle
        )
    }

    // --- Form state ---

    @Test
    fun addExercise_addsToList() {
        val viewModel = createViewModel()

        viewModel.addExercise(testExercise)

        val state = viewModel.uiState.value
        assertEquals(1, state.exercises.size)
        assertEquals(testExercise, state.exercises[0].exercise)
    }

    @Test
    fun removeExercise_removesFromList() {
        val viewModel = createViewModel()

        viewModel.addExercise(testExercise)
        viewModel.removeExercise(0)

        assertEquals(0, viewModel.uiState.value.exercises.size)
    }

    @Test
    fun addSet_addsSetToExercise() {
        val viewModel = createViewModel()

        viewModel.addExercise(testExercise)
        viewModel.addSet(0)

        assertEquals(2, viewModel.uiState.value.exercises[0].sets.size)
    }

    @Test
    fun removeSet_blockedAtOne() {
        val viewModel = createViewModel()

        viewModel.addExercise(testExercise)
        // Exercise starts with 1 set; removing should be blocked
        viewModel.removeSet(0, 0)

        assertEquals(1, viewModel.uiState.value.exercises[0].sets.size)
    }

    @Test
    fun removeSet_removesWhenMoreThanOne() {
        val viewModel = createViewModel()

        viewModel.addExercise(testExercise)
        viewModel.addSet(0)
        assertEquals(2, viewModel.uiState.value.exercises[0].sets.size)

        viewModel.removeSet(0, 1)
        assertEquals(1, viewModel.uiState.value.exercises[0].sets.size)
    }

    @Test
    fun moveExercise_swapsPositions() {
        val viewModel = createViewModel()
        val exercise2 = Exercise(id = 2L, profileId = 1L, name = "Squat")

        viewModel.addExercise(testExercise)
        viewModel.addExercise(exercise2)
        viewModel.moveExercise(0, 1)

        val exercises = viewModel.uiState.value.exercises
        assertEquals("Squat", exercises[0].exercise.name)
        assertEquals("Bench Press", exercises[1].exercise.name)
    }

    @Test
    fun moveExercise_outOfBounds_doesNothing() {
        val viewModel = createViewModel()

        viewModel.addExercise(testExercise)
        viewModel.moveExercise(0, 1)

        assertEquals(1, viewModel.uiState.value.exercises.size)
        assertEquals("Bench Press", viewModel.uiState.value.exercises[0].exercise.name)
    }

    @Test
    fun updateName_clearsError() {
        val viewModel = createViewModel()

        // Force a nameError by attempting to save with blank name
        viewModel.updateName("")
        // Now update with a valid name
        viewModel.updateName("My Program")

        assertNull(viewModel.uiState.value.nameError)
        assertEquals("My Program", viewModel.uiState.value.name)
    }

    // --- updateSet ---

    @Test
    fun updateSet_updatesSetEntry() {
        val viewModel = createViewModel()
        viewModel.addExercise(testExercise)

        val updated = SetEntry(weight = "100", reps = "8", restTimeSeconds = "0130")
        viewModel.updateSet(0, 0, updated)

        val set = viewModel.uiState.value.exercises[0].sets[0]
        assertEquals("100", set.weight)
        assertEquals("8", set.reps)
        assertEquals("0130", set.restTimeSeconds)
    }

    // --- setRestTimeForAll ---

    @Test
    fun setRestTimeForAll_updatesAllSetsInExercise() {
        val viewModel = createViewModel()
        viewModel.addExercise(testExercise)
        viewModel.addSet(0)
        viewModel.addSet(0)
        assertEquals(3, viewModel.uiState.value.exercises[0].sets.size)

        viewModel.setRestTimeForAll(0, "0200")

        val sets = viewModel.uiState.value.exercises[0].sets
        sets.forEach { assertEquals("0200", it.restTimeSeconds) }
    }

    @Test
    fun setRestTimeForAll_doesNotAffectOtherExercises() {
        val viewModel = createViewModel()
        val exercise2 = Exercise(id = 2L, profileId = 1L, name = "Squat")
        viewModel.addExercise(testExercise)
        viewModel.addExercise(exercise2)

        viewModel.setRestTimeForAll(0, "0130")

        val exercises = viewModel.uiState.value.exercises
        assertEquals("0130", exercises[0].sets[0].restTimeSeconds)
        assertEquals("100", exercises[1].sets[0].restTimeSeconds)
    }

    @Test
    fun setRestTimeForAll_emptyDigits_clearsRestTime() {
        val viewModel = createViewModel()
        viewModel.addExercise(testExercise)
        viewModel.updateSet(0, 0, SetEntry(weight = "", reps = "", restTimeSeconds = "0200"))

        viewModel.setRestTimeForAll(0, "")

        assertEquals("", viewModel.uiState.value.exercises[0].sets[0].restTimeSeconds)
    }

    // --- Save validation ---

    @Test
    fun save_blankName_setsError() = runTest {
        val viewModel = createViewModel()

        viewModel.addExercise(testExercise)
        val result = viewModel.save()

        assertFalse(result)
        assertNotNull(viewModel.uiState.value.nameError)
    }

    @Test
    fun save_emptyExercises_setsError() = runTest {
        val viewModel = createViewModel()

        viewModel.updateName("My Program")
        val result = viewModel.save()

        assertFalse(result)
        assertNotNull(viewModel.uiState.value.nameError)
    }

    // --- Save success (create) ---

    @Test
    fun save_createMode_callsSaveProgramWithExercises() = runTest {
        coEvery { programRepository.saveProgramWithExercises(any(), any(), any()) } returns 10L

        val viewModel = createViewModel()
        viewModel.updateName("Push Day")
        viewModel.addExercise(testExercise)

        val result = viewModel.save()

        assertTrue(result)
        coVerify {
            programRepository.saveProgramWithExercises(
                match { it.name == "Push Day" && it.id == 0L },
                eq(false),
                match { it.size == 1 }
            )
        }
    }

    // --- Save success (edit) ---

    @Test
    fun save_editMode_callsSaveProgramWithExercisesAsUpdate() = runTest {
        coEvery { programRepository.getProgramWithExercises(5L) } returns null
        coEvery { programRepository.saveProgramWithExercises(any(), any(), any()) } returns 5L

        val viewModel = createViewModel(programId = 5L)
        viewModel.updateName("Updated Program")
        viewModel.addExercise(testExercise)

        val result = viewModel.save()

        assertTrue(result)
        coVerify {
            programRepository.saveProgramWithExercises(
                match { it.id == 5L && it.name == "Updated Program" },
                eq(true),
                match { it.size == 1 }
            )
        }
    }
}
