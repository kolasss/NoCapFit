package com.example.nocapfit.ui.screens.exercises

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.nocapfit.MainDispatcherRule
import com.example.nocapfit.data.db.entity.Exercise
import com.example.nocapfit.data.db.entity.Profile
import com.example.nocapfit.data.repository.ExerciseRepository
import com.example.nocapfit.data.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExerciseFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val exerciseRepository = mockk<ExerciseRepository>(relaxUnitFun = true)
    private val profileRepository = mockk<ProfileRepository>()

    private val testProfile = Profile(id = 1L, name = "Default")
    private val testExercise = Exercise(
        id = 5L,
        profileId = 1L,
        name = "Bench Press",
        description = "Chest exercise",
        tags = "chest, triceps"
    )

    private fun createViewModel(exerciseId: Long = -1L): ExerciseFormViewModel {
        coEvery { profileRepository.getDefault() } returns testProfile
        coEvery { exerciseRepository.getById(any()) } returns testExercise
        coEvery { exerciseRepository.insert(any()) } returns 10L
        val savedStateHandle = SavedStateHandle(mapOf("exerciseId" to exerciseId))
        return ExerciseFormViewModel(exerciseRepository, profileRepository, savedStateHandle)
    }

    @Test
    fun createMode_isEditModeIsFalse() = runTest {
        val viewModel = createViewModel()
        assertFalse(viewModel.isEditMode)
    }

    @Test
    fun createMode_fieldsStartEmpty() = runTest {
        val viewModel = createViewModel()
        assertEquals("", viewModel.name.value)
        assertEquals("", viewModel.description.value)
        assertEquals("", viewModel.tags.value)
    }

    @Test
    fun editMode_isEditModeIsTrue() = runTest {
        val viewModel = createViewModel(exerciseId = 5L)
        assertTrue(viewModel.isEditMode)
    }

    @Test
    fun editMode_loadsExerciseData() = runTest {
        val viewModel = createViewModel(exerciseId = 5L)

        viewModel.name.test {
            assertEquals("Bench Press", awaitItem())
        }
        assertEquals("Chest exercise", viewModel.description.value)
        assertEquals("chest, triceps", viewModel.tags.value)
    }

    @Test
    fun updateName_updatesState() = runTest {
        val viewModel = createViewModel()
        viewModel.updateName("Squat")
        assertEquals("Squat", viewModel.name.value)
    }

    @Test
    fun updateDescription_updatesState() = runTest {
        val viewModel = createViewModel()
        viewModel.updateDescription("Leg exercise")
        assertEquals("Leg exercise", viewModel.description.value)
    }

    @Test
    fun updateTags_updatesState() = runTest {
        val viewModel = createViewModel()
        viewModel.updateTags("legs, quads")
        assertEquals("legs, quads", viewModel.tags.value)
    }

    @Test
    fun save_createMode_insertsExercise() = runTest {
        val viewModel = createViewModel()
        viewModel.updateName("Deadlift")
        viewModel.updateDescription("  Back exercise  ")
        viewModel.updateTags("  back  ")
        var callbackInvoked = false

        viewModel.save { callbackInvoked = true }

        coVerify {
            exerciseRepository.insert(
                match {
                    it.name == "Deadlift" &&
                        it.description == "Back exercise" &&
                        it.tags == "back" &&
                        it.profileId == 1L
                }
            )
        }
        assertTrue(callbackInvoked)
    }

    @Test
    fun save_editMode_updatesExercise() = runTest {
        val viewModel = createViewModel(exerciseId = 5L)

        viewModel.name.test { awaitItem() }

        viewModel.updateName("Incline Bench")
        var callbackInvoked = false

        viewModel.save { callbackInvoked = true }

        coVerify {
            exerciseRepository.update(match { it.name == "Incline Bench" && it.id == 5L })
        }
        assertTrue(callbackInvoked)
    }

    @Test
    fun save_blankName_doesNotCallRepository() = runTest {
        val viewModel = createViewModel()
        viewModel.updateName("   ")
        var callbackInvoked = false

        viewModel.save { callbackInvoked = true }

        coVerify(exactly = 0) { exerciseRepository.insert(any()) }
        coVerify(exactly = 0) { exerciseRepository.update(any()) }
        assertFalse(callbackInvoked)
    }
}
