package dev.kolas.nocapfit.ui.screens.settings

import android.net.Uri
import app.cash.turbine.test
import dev.kolas.nocapfit.MainDispatcherRule
import dev.kolas.nocapfit.data.backup.BackupManager
import dev.kolas.nocapfit.data.preferences.ThemeMode
import dev.kolas.nocapfit.data.preferences.ThemePreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val themePreferences = mockk<ThemePreferences>(relaxUnitFun = true)
    private val backupManager = mockk<BackupManager>(relaxUnitFun = true)

    private fun createViewModel(): SettingsViewModel {
        every { themePreferences.themeMode } returns flowOf(ThemeMode.SYSTEM)
        every { themePreferences.notificationSoundUri } returns flowOf(null)
        every { themePreferences.setCompletionSoundUri } returns flowOf(null)
        every { themePreferences.dynamicColor } returns flowOf(false)
        return SettingsViewModel(themePreferences, backupManager)
    }

    @Test
    fun themeMode_emitsValueFromPreferences() = runTest {
        every { themePreferences.themeMode } returns flowOf(ThemeMode.DARK)
        every { themePreferences.notificationSoundUri } returns flowOf(null)
        every { themePreferences.setCompletionSoundUri } returns flowOf(null)
        every { themePreferences.dynamicColor } returns flowOf(false)

        val viewModel = SettingsViewModel(themePreferences, backupManager)

        viewModel.themeMode.test {
            assertEquals(ThemeMode.DARK, awaitItem())
        }
    }

    @Test
    fun setThemeMode_callsPreferences() = runTest {
        val viewModel = createViewModel()
        viewModel.setThemeMode(ThemeMode.LIGHT)

        coVerify { themePreferences.setThemeMode(ThemeMode.LIGHT) }
    }

    @Test
    fun setDynamicColor_callsPreferences() = runTest {
        val viewModel = createViewModel()
        viewModel.setDynamicColor(true)

        coVerify { themePreferences.setDynamicColor(true) }
    }

    @Test
    fun exportDatabase_emitsSuccessOnCompletion() = runTest {
        val uri = mockk<Uri>()
        coEvery { backupManager.exportDatabase(uri) } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.exportDatabase(uri)

        val event = viewModel.backupEvent.value
        assertTrue(
            "Expected Success but got $event",
            event is BackupEvent.Success
        )
    }

    @Test
    fun exportDatabase_emitsErrorOnFailure() = runTest {
        val uri = mockk<Uri>()
        coEvery { backupManager.exportDatabase(uri) } returns Result.failure(IOException("Disk full"))

        val viewModel = createViewModel()
        viewModel.exportDatabase(uri)

        val event = viewModel.backupEvent.value
        assertTrue("Expected Error but got $event", event is BackupEvent.Error)
        assertEquals("Disk full", (event as BackupEvent.Error).message)
    }

    @Test
    fun importDatabase_emitsRestartRequiredOnSuccess() = runTest {
        val uri = mockk<Uri>()
        val bytes = "SQLite format 3\u0000".toByteArray()
        coEvery { backupManager.validateBackup(uri) } returns Result.success(bytes)

        val viewModel = createViewModel()
        viewModel.importDatabase(uri)

        assertEquals(BackupEvent.RestartRequired, viewModel.backupEvent.value)
    }

    @Test
    fun importDatabase_emitsErrorOnInvalidFile() = runTest {
        val uri = mockk<Uri>()
        coEvery { backupManager.validateBackup(uri) } returns
            Result.failure(IllegalArgumentException("Not a valid database file"))

        val viewModel = createViewModel()
        viewModel.importDatabase(uri)

        val event = viewModel.backupEvent.value
        assertTrue("Expected Error but got $event", event is BackupEvent.Error)
        assertEquals("Not a valid database file", (event as BackupEvent.Error).message)
    }

    @Test
    fun clearBackupEvent_resetsToIdle() = runTest {
        val uri = mockk<Uri>()
        coEvery { backupManager.exportDatabase(uri) } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.exportDatabase(uri)
        assertTrue(viewModel.backupEvent.value is BackupEvent.Success)

        viewModel.clearBackupEvent()
        assertEquals(BackupEvent.Idle, viewModel.backupEvent.value)
    }

    @Test
    fun hasActiveWorkout_delegatesToBackupManager() = runTest {
        coEvery { backupManager.hasActiveWorkout() } returns true

        val viewModel = createViewModel()
        val result = viewModel.hasActiveWorkout()

        assertEquals(true, result)
        coVerify { backupManager.hasActiveWorkout() }
    }

    @Test
    fun notificationSoundUri_emitsValueFromPreferences() = runTest {
        every { themePreferences.themeMode } returns flowOf(ThemeMode.SYSTEM)
        every { themePreferences.notificationSoundUri } returns flowOf("content://some/sound")
        every { themePreferences.setCompletionSoundUri } returns flowOf(null)
        every { themePreferences.dynamicColor } returns flowOf(false)

        val viewModel = SettingsViewModel(themePreferences, backupManager)

        viewModel.notificationSoundUri.test {
            assertEquals("content://some/sound", awaitItem())
        }
    }

    @Test
    fun setNotificationSoundUri_callsPreferences() = runTest {
        val viewModel = createViewModel()
        viewModel.setNotificationSoundUri("content://new/sound")

        coVerify { themePreferences.setNotificationSoundUri("content://new/sound") }
    }

    @Test
    fun setCompletionSoundUri_emitsValueFromPreferences() = runTest {
        every { themePreferences.themeMode } returns flowOf(ThemeMode.SYSTEM)
        every { themePreferences.notificationSoundUri } returns flowOf(null)
        every { themePreferences.setCompletionSoundUri } returns flowOf("content://set/sound")
        every { themePreferences.dynamicColor } returns flowOf(false)

        val viewModel = SettingsViewModel(themePreferences, backupManager)

        viewModel.setCompletionSoundUri.test {
            assertEquals("content://set/sound", awaitItem())
        }
    }

    @Test
    fun setSetCompletionSoundUri_callsPreferences() = runTest {
        val viewModel = createViewModel()
        viewModel.setSetCompletionSoundUri("content://new/set/sound")

        coVerify { themePreferences.setSetCompletionSoundUri("content://new/set/sound") }
    }
}
