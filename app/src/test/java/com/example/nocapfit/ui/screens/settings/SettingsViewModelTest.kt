package com.example.nocapfit.ui.screens.settings

import app.cash.turbine.test
import com.example.nocapfit.MainDispatcherRule
import com.example.nocapfit.data.preferences.ThemeMode
import com.example.nocapfit.data.preferences.ThemePreferences
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val themePreferences = mockk<ThemePreferences>(relaxUnitFun = true)

    @Test
    fun themeMode_emitsValueFromPreferences() = runTest {
        every { themePreferences.themeMode } returns flowOf(ThemeMode.DARK)

        val viewModel = SettingsViewModel(themePreferences)

        viewModel.themeMode.test {
            assertEquals(ThemeMode.DARK, awaitItem())
        }
    }

    @Test
    fun setThemeMode_callsPreferences() = runTest {
        every { themePreferences.themeMode } returns flowOf(ThemeMode.SYSTEM)

        val viewModel = SettingsViewModel(themePreferences)
        viewModel.setThemeMode(ThemeMode.LIGHT)

        coVerify { themePreferences.setThemeMode(ThemeMode.LIGHT) }
    }
}
