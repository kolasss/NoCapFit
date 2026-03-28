package com.example.nocapfit.service

import android.content.Context
import com.example.nocapfit.data.db.entity.ActiveTimer
import com.example.nocapfit.data.db.entity.TimerStatus
import com.example.nocapfit.data.repository.TimerRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimerCoordinatorTest {

    private val testDispatcher = StandardTestDispatcher()
    private val timerRepository = mockk<TimerRepository>(relaxUnitFun = true)
    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createCoordinator(): TimerCoordinator {
        return TimerCoordinator(timerRepository, context)
    }

    @Test
    fun reconstructState_noRunningTimer_idle() = runTest(testDispatcher) {
        coEvery { timerRepository.getRunning() } returns null

        val coordinator = createCoordinator()
        advanceUntilIdle()

        assertEquals(TimerCoordinator.TimerUiState.Idle, coordinator.timerState.value)
    }

    @Test
    fun reconstructState_runningNotExpired_running() = runTest(testDispatcher) {
        val futureEnd = System.currentTimeMillis() + 30_000L
        val timer = ActiveTimer(
            id = 1L,
            workoutId = 10L,
            workoutSetId = 20L,
            startedAtEpochMs = System.currentTimeMillis(),
            endAtEpochMs = futureEnd,
            status = TimerStatus.RUNNING,
            notificationId = 1001
        )
        coEvery { timerRepository.getRunning() } returns timer

        val coordinator = createCoordinator()
        advanceUntilIdle()

        val state = coordinator.timerState.value
        assertTrue("Expected Running but got $state", state is TimerCoordinator.TimerUiState.Running)
        val running = state as TimerCoordinator.TimerUiState.Running
        assertEquals(1L, running.timerId)
        assertEquals(10L, running.workoutId)
    }

    @Test
    fun reconstructState_expired_completesTimerAndIdle() = runTest(testDispatcher) {
        val pastEnd = System.currentTimeMillis() - 5000L
        val timer = ActiveTimer(
            id = 1L,
            workoutId = 10L,
            workoutSetId = 20L,
            startedAtEpochMs = System.currentTimeMillis() - 60_000L,
            endAtEpochMs = pastEnd,
            status = TimerStatus.RUNNING,
            notificationId = 1001
        )
        coEvery { timerRepository.getRunning() } returns timer
        coEvery { timerRepository.completeTimer(1L) } returns true

        val coordinator = createCoordinator()
        advanceUntilIdle()

        assertEquals(TimerCoordinator.TimerUiState.Idle, coordinator.timerState.value)
        coVerify { timerRepository.completeTimer(1L) }
    }

    @Test
    fun onTimerCompleted_transitionsToFinishedThenIdle() = runTest(testDispatcher) {
        val futureEnd = System.currentTimeMillis() + 30_000L
        val timer = ActiveTimer(
            id = 1L,
            workoutId = 10L,
            workoutSetId = 20L,
            startedAtEpochMs = System.currentTimeMillis(),
            endAtEpochMs = futureEnd,
            status = TimerStatus.RUNNING,
            notificationId = 1001
        )
        coEvery { timerRepository.getRunning() } returns timer

        val coordinator = createCoordinator()
        advanceUntilIdle()

        assertTrue(coordinator.timerState.value is TimerCoordinator.TimerUiState.Running)

        coordinator.onTimerCompleted(1L)
        assertEquals(TimerCoordinator.TimerUiState.Finished, coordinator.timerState.value)

        advanceTimeBy(2100)
        assertEquals(TimerCoordinator.TimerUiState.Idle, coordinator.timerState.value)
    }

    @Test
    fun cancelTimer_fromIdle_setsIdle() = runTest(testDispatcher) {
        coEvery { timerRepository.getRunning() } returns null

        val coordinator = createCoordinator()
        advanceUntilIdle()

        coordinator.cancelTimer()
        advanceUntilIdle()

        assertEquals(TimerCoordinator.TimerUiState.Idle, coordinator.timerState.value)
    }

    @Test
    fun reconstructState_runningTimer_setsCorrectWorkoutSetId() = runTest(testDispatcher) {
        val futureEnd = System.currentTimeMillis() + 30_000L
        val timer = ActiveTimer(
            id = 3L,
            workoutId = 10L,
            workoutSetId = 42L,
            startedAtEpochMs = System.currentTimeMillis(),
            endAtEpochMs = futureEnd,
            status = TimerStatus.RUNNING,
            notificationId = 1001
        )
        coEvery { timerRepository.getRunning() } returns timer

        val coordinator = createCoordinator()
        advanceUntilIdle()

        val state = coordinator.timerState.value as TimerCoordinator.TimerUiState.Running
        assertEquals(42L, state.workoutSetId)
        assertEquals(3L, state.timerId)
    }

    @Test
    fun onTimerCompleted_whileAlreadyIdle_staysIdle() = runTest(testDispatcher) {
        coEvery { timerRepository.getRunning() } returns null

        val coordinator = createCoordinator()
        advanceUntilIdle()

        assertEquals(TimerCoordinator.TimerUiState.Idle, coordinator.timerState.value)

        // Completing a timer while Idle is ignored (no matching Running state)
        coordinator.onTimerCompleted(99L)
        assertEquals(TimerCoordinator.TimerUiState.Idle, coordinator.timerState.value)
    }

    @Test
    fun onTimerCompleted_mismatchedTimerId_ignored() = runTest(testDispatcher) {
        val futureEnd = System.currentTimeMillis() + 30_000L
        val timer = ActiveTimer(
            id = 1L,
            workoutId = 10L,
            workoutSetId = 20L,
            startedAtEpochMs = System.currentTimeMillis(),
            endAtEpochMs = futureEnd,
            status = TimerStatus.RUNNING,
            notificationId = 1001
        )
        coEvery { timerRepository.getRunning() } returns timer

        val coordinator = createCoordinator()
        advanceUntilIdle()

        assertTrue(coordinator.timerState.value is TimerCoordinator.TimerUiState.Running)

        // Completing with wrong timerId is ignored
        coordinator.onTimerCompleted(999L)
        assertTrue(coordinator.timerState.value is TimerCoordinator.TimerUiState.Running)
    }

    @Test
    fun reconstructState_expiredTimer_doesNotSetRunning() = runTest(testDispatcher) {
        val pastEnd = System.currentTimeMillis() - 1000L
        val timer = ActiveTimer(
            id = 2L,
            workoutId = 10L,
            workoutSetId = 20L,
            startedAtEpochMs = System.currentTimeMillis() - 60_000L,
            endAtEpochMs = pastEnd,
            status = TimerStatus.RUNNING,
            notificationId = 1001
        )
        coEvery { timerRepository.getRunning() } returns timer
        coEvery { timerRepository.completeTimer(2L) } returns true

        val coordinator = createCoordinator()
        advanceUntilIdle()

        // Should be Idle, not Running — expired timers are completed immediately
        assertEquals(TimerCoordinator.TimerUiState.Idle, coordinator.timerState.value)
        coVerify { timerRepository.completeTimer(2L) }
    }

    @Test
    fun stableRequestCode_differentForIdsWithSameLower32Bits() {
        val id1 = 1L
        val id2 = 1L + Int.MAX_VALUE.toLong() + 1L
        assertNotEquals(stableRequestCode(id1), stableRequestCode(id2))
    }

    @Test
    fun stableRequestCode_consistentForSameId() {
        val id = 123456789L
        assertEquals(stableRequestCode(id), stableRequestCode(id))
    }
}
