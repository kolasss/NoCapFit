package com.example.nocapfit.ui.screens.programs

import androidx.lifecycle.SavedStateHandle
import com.example.nocapfit.MainDispatcherRule
import com.example.nocapfit.data.db.entity.Exercise
import com.example.nocapfit.data.db.entity.Profile
import com.example.nocapfit.data.repository.ExerciseRepository
import com.example.nocapfit.data.repository.ProfileRepository
import com.example.nocapfit.data.repository.ProgramRepository
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

    private val testProfile = Profile(id = 1L, name = "Default")
    private val testExercise = Exercise(id = 1L, profileId = 1L, name = "Bench Press")

    private fun createViewModel(programId: Long = -1L): ProgramFormViewModel {
        coEvery { profileRepository.getDefault() } returns testProfile
        every { exerciseRepository.getAllByProfile(1L) } returns flowOf(listOf(testExercise))

        val savedStateHandle = SavedStateHandle(
            if (programId > 0) mapOf("programId" to programId) else emptyMap()
        )
        return ProgramFormViewModel(programRepository, exerciseRepository, profileRepository, savedStateHandle)
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
    fun updateName_clearsError() {
        val viewModel = createViewModel()

        // Force a nameError by attempting to save with blank name
        viewModel.updateName("")
        // Now update with a valid name
        viewModel.updateName("My Program")

        assertNull(viewModel.uiState.value.nameError)
        assertEquals("My Program", viewModel.uiState.value.name)
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
    fun save_createMode_callsInsertAndReturnsTrue() = runTest {
        coEvery { programRepository.insert(any()) } returns 10L
        coEvery { programRepository.insertProgramExercise(any()) } returns 20L
        coEvery { programRepository.insertProgramExerciseSet(any()) } returns 30L

        val viewModel = createViewModel()
        viewModel.updateName("Push Day")
        viewModel.addExercise(testExercise)

        val result = viewModel.save()

        assertTrue(result)
        coVerify { programRepository.insert(match { it.name == "Push Day" }) }
        coVerify { programRepository.insertProgramExercise(any()) }
        coVerify { programRepository.insertProgramExerciseSet(any()) }
    }

    // --- Save success (edit) ---

    @Test
    fun save_editMode_callsUpdateAndDeleteExercises() = runTest {
        coEvery { programRepository.getProgramWithExercises(5L) } returns null
        coEvery { programRepository.insert(any()) } returns 5L
        coEvery { programRepository.insertProgramExercise(any()) } returns 20L
        coEvery { programRepository.insertProgramExerciseSet(any()) } returns 30L

        val viewModel = createViewModel(programId = 5L)
        viewModel.updateName("Updated Program")
        viewModel.addExercise(testExercise)

        val result = viewModel.save()

        assertTrue(result)
        coVerify { programRepository.update(match { it.id == 5L && it.name == "Updated Program" }) }
        coVerify { programRepository.deleteExercisesForProgram(5L) }
    }
}
