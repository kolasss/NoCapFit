package dev.kolas.nocapfit.ui.screens.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.kolas.nocapfit.data.preferences.ThemeMode
import dev.kolas.nocapfit.setThemedContent
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
                dynamicColor = false,
                onDynamicColorChange = {},
                notificationSoundUri = null,
                onTimerSoundClick = {},
                setCompletionSoundUri = null,
                onSetCompletionSoundClick = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0",
                osVersion = "Android 15 (SDK 36)"
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
                dynamicColor = false,
                onDynamicColorChange = {},
                notificationSoundUri = null,
                onTimerSoundClick = {},
                setCompletionSoundUri = null,
                onSetCompletionSoundClick = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0",
                osVersion = "Android 15 (SDK 36)"
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
                dynamicColor = false,
                onDynamicColorChange = {},
                notificationSoundUri = null,
                onTimerSoundClick = {},
                setCompletionSoundUri = null,
                onSetCompletionSoundClick = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0",
                osVersion = "Android 15 (SDK 36)"
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
                dynamicColor = false,
                onDynamicColorChange = {},
                notificationSoundUri = null,
                onTimerSoundClick = {},
                setCompletionSoundUri = null,
                onSetCompletionSoundClick = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0",
                osVersion = "Android 15 (SDK 36)"
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
                dynamicColor = false,
                onDynamicColorChange = {},
                notificationSoundUri = null,
                onTimerSoundClick = {},
                setCompletionSoundUri = null,
                onSetCompletionSoundClick = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0",
                osVersion = "Android 15 (SDK 36)"
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
                dynamicColor = false,
                onDynamicColorChange = {},
                notificationSoundUri = null,
                onTimerSoundClick = {},
                setCompletionSoundUri = null,
                onSetCompletionSoundClick = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0",
                osVersion = "Android 15 (SDK 36)"
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
                dynamicColor = false,
                onDynamicColorChange = {},
                notificationSoundUri = null,
                onTimerSoundClick = {},
                setCompletionSoundUri = null,
                onSetCompletionSoundClick = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0",
                osVersion = "Android 15 (SDK 36)"
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
                dynamicColor = false,
                onDynamicColorChange = {},
                notificationSoundUri = null,
                onTimerSoundClick = {},
                setCompletionSoundUri = null,
                onSetCompletionSoundClick = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0",
                osVersion = "Android 15 (SDK 36)"
            )
        }

        composeTestRule.onNodeWithText("About").assertIsDisplayed()
        composeTestRule.onNodeWithText("NoCapFit").assertIsDisplayed()
        composeTestRule.onNodeWithText("Version 1.0").assertIsDisplayed()
    }

    @Test
    fun rendersNotificationsSection() {
        composeTestRule.setThemedContent {
            SettingsContent(
                themeMode = ThemeMode.SYSTEM,
                onThemeModeChange = {},
                dynamicColor = false,
                onDynamicColorChange = {},
                notificationSoundUri = null,
                onTimerSoundClick = {},
                setCompletionSoundUri = null,
                onSetCompletionSoundClick = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0",
                osVersion = "Android 15 (SDK 36)"
            )
        }

        composeTestRule.onNodeWithText("Notifications").assertIsDisplayed()
        composeTestRule.onNodeWithText("Timer Sound").assertIsDisplayed()
    }

    @Test
    fun timerSound_showsDefaultWhenUriIsNull() {
        composeTestRule.setThemedContent {
            SettingsContent(
                themeMode = ThemeMode.SYSTEM,
                onThemeModeChange = {},
                dynamicColor = false,
                onDynamicColorChange = {},
                notificationSoundUri = null,
                onTimerSoundClick = {},
                setCompletionSoundUri = "",
                onSetCompletionSoundClick = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0",
                osVersion = "Android 15 (SDK 36)"
            )
        }

        composeTestRule.onNodeWithText("Default").assertIsDisplayed()
    }

    @Test
    fun dynamicColorOn_switchIsOn() {
        composeTestRule.setThemedContent {
            SettingsContent(
                themeMode = ThemeMode.SYSTEM,
                onThemeModeChange = {},
                dynamicColor = true,
                onDynamicColorChange = {},
                notificationSoundUri = null,
                onTimerSoundClick = {},
                setCompletionSoundUri = null,
                onSetCompletionSoundClick = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0",
                osVersion = "Android 15 (SDK 36)"
            )
        }

        composeTestRule.onNodeWithText("Dynamic Color").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dynamic_color_switch").assertIsOn()
    }

    @Test
    fun dynamicColorOff_switchIsOff() {
        composeTestRule.setThemedContent {
            SettingsContent(
                themeMode = ThemeMode.SYSTEM,
                onThemeModeChange = {},
                dynamicColor = false,
                onDynamicColorChange = {},
                notificationSoundUri = null,
                onTimerSoundClick = {},
                setCompletionSoundUri = null,
                onSetCompletionSoundClick = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0",
                osVersion = "Android 15 (SDK 36)"
            )
        }

        composeTestRule.onNodeWithTag("dynamic_color_switch").assertIsOff()
    }

    @Test
    fun timerSound_showsSilentWhenEmpty() {
        composeTestRule.setThemedContent {
            SettingsContent(
                themeMode = ThemeMode.SYSTEM,
                onThemeModeChange = {},
                dynamicColor = false,
                onDynamicColorChange = {},
                notificationSoundUri = "",
                onTimerSoundClick = {},
                setCompletionSoundUri = null,
                onSetCompletionSoundClick = {},
                isBackupInProgress = false,
                onExportClick = {},
                onImportClick = {},
                versionName = "1.0",
                osVersion = "Android 15 (SDK 36)"
            )
        }

        composeTestRule.onNodeWithText("Silent").assertIsDisplayed()
    }
}
