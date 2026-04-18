package com.example.nocapfit.ui.screens.programs

import app.cash.turbine.test
import com.example.nocapfit.MainDispatcherRule
import com.example.nocapfit.data.db.entity.Profile
import com.example.nocapfit.data.db.entity.Program
import com.example.nocapfit.data.db.relation.ProgramWithExercises
import com.example.nocapfit.data.repository.ProfileRepository
import com.example.nocapfit.data.repository.ProgramRepository
import com.example.nocapfit.data.session.CurrentProfileHolder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProgramListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val programRepository = mockk<ProgramRepository>(relaxUnitFun = true)
    private val profileRepository = mockk<ProfileRepository>()

    private val testProfile = Profile(id = 1L, name = "Default")
    private val testPrograms = listOf(
        ProgramWithExercises(
            program = Program(id = 1L, profileId = 1L, name = "Push Day"),
            exercises = emptyList()
        ),
        ProgramWithExercises(
            program = Program(id = 2L, profileId = 1L, name = "Pull Day"),
            exercises = emptyList()
        )
    )

    private fun createViewModel(): ProgramListViewModel {
        coEvery { profileRepository.getDefault() } returns testProfile
        every { programRepository.getAllWithExercises(1L) } returns flowOf(testPrograms)
        return ProgramListViewModel(programRepository, CurrentProfileHolder(profileRepository))
    }

    @Test
    fun programs_emitsProgramsAfterProfileLoads() = runTest {
        val viewModel = createViewModel()

        viewModel.programs.test {
            assertEquals(testPrograms, awaitItem())
        }
    }

    @Test
    fun deleteProgram_callsRepositoryDelete() = runTest {
        val viewModel = createViewModel()

        viewModel.deleteProgram(testPrograms[0])

        coVerify { programRepository.delete(testPrograms[0].program) }
    }

    @Test
    fun copyProgram_callsRepositoryCopy() = runTest {
        coEvery { programRepository.copyProgram(1L) } returns 3L
        val viewModel = createViewModel()

        viewModel.copyProgram(1L)

        coVerify { programRepository.copyProgram(1L) }
    }
}
