package com.example.nocapfit.ui.screens.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.nocapfit.data.preferences.ThemeMode
import com.example.nocapfit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersAppearanceSection() {
        composeTestRule.setThemedContent {
            SettingsContent(
                themeMode = ThemeMode.SYSTEM,
                onThemeModeChange = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0"
            )
        }

        composeTestRule.onNodeWithText("Appearance").assertIsDisplayed()
    }

    @Test
    fun rendersThemeLabel() {
        composeTestRule.setThemedContent {
            SettingsContent(
                themeMode = ThemeMode.SYSTEM,
                onThemeModeChange = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0"
            )
        }

        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
    }

    @Test
    fun rendersAllThemeOptions() {
        composeTestRule.setThemedContent {
            SettingsContent(
                themeMode = ThemeMode.SYSTEM,
                onThemeModeChange = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0"
            )
        }

        composeTestRule.onNodeWithText("Light").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dark").assertIsDisplayed()
        composeTestRule.onNodeWithText("System").assertIsDisplayed()
    }

    @Test
    fun systemMode_systemButtonSelected() {
        composeTestRule.setThemedContent {
            SettingsContent(
                themeMode = ThemeMode.SYSTEM,
                onThemeModeChange = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0"
            )
        }

        composeTestRule.onNodeWithText("System").assertIsSelected()
    }

    @Test
    fun lightMode_lightButtonSelected() {
        composeTestRule.setThemedContent {
            SettingsContent(
                themeMode = ThemeMode.LIGHT,
                onThemeModeChange = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0"
            )
        }

        composeTestRule.onNodeWithText("Light").assertIsSelected()
    }

    @Test
    fun darkMode_darkButtonSelected() {
        composeTestRule.setThemedContent {
            SettingsContent(
                themeMode = ThemeMode.DARK,
                onThemeModeChange = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0"
            )
        }

        composeTestRule.onNodeWithText("Dark").assertIsSelected()
    }

    @Test
    fun clickingDarkButton_triggersCallback() {
        var selectedMode: ThemeMode? = null
        composeTestRule.setThemedContent {
            SettingsContent(
                themeMode = ThemeMode.LIGHT,
                onThemeModeChange = { selectedMode = it },
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0"
            )
        }

        composeTestRule.onNodeWithText("Dark").performClick()
        assertEquals(ThemeMode.DARK, selectedMode)
    }

    @Test
    fun rendersAboutSection() {
        composeTestRule.setThemedContent {
            SettingsContent(
                themeMode = ThemeMode.SYSTEM,
                onThemeModeChange = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0"
            )
        }

        composeTestRule.onNodeWithText("About").assertIsDisplayed()
        composeTestRule.onNodeWithText("NoCapFit").assertIsDisplayed()
        composeTestRule.onNodeWithText("Version 1.0").assertIsDisplayed()
    }
}
