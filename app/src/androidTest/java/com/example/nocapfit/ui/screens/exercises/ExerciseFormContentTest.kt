package com.example.nocapfit.ui.screens.exercises

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.example.nocapfit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ExerciseFormContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersAllFields() {
        composeTestRule.setThemedContent {
            ExerciseFormContent(
                name = "",
                description = "",
                tags = "",
                onNameChange = {},
                onDescriptionChange = {},
                onTagsChange = {}
            )
        }

        composeTestRule.onNodeWithText("Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Description").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tags (comma separated)").assertIsDisplayed()
    }

    @Test
    fun displaysPrePopulatedValues() {
        composeTestRule.setThemedContent {
            ExerciseFormContent(
                name = "Bench Press",
                description = "Chest exercise",
                tags = "chest, triceps",
                onNameChange = {},
                onDescriptionChange = {},
                onTagsChange = {}
            )
        }

        composeTestRule.onNodeWithText("Bench Press").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chest exercise").assertIsDisplayed()
        composeTestRule.onNodeWithText("chest, triceps").assertIsDisplayed()
    }

    @Test
    fun typingInName_triggersCallback() {
        var captured = ""
        composeTestRule.setThemedContent {
            ExerciseFormContent(
                name = "",
                description = "",
                tags = "",
                onNameChange = { captured = it },
                onDescriptionChange = {},
                onTagsChange = {}
            )
        }

        composeTestRule.onNodeWithText("Name").performTextInput("Squat")
        assertEquals("Squat", captured)
    }

    @Test
    fun typingInDescription_triggersCallback() {
        var captured = ""
        composeTestRule.setThemedContent {
            ExerciseFormContent(
                name = "",
                description = "",
                tags = "",
                onNameChange = {},
                onDescriptionChange = { captured = it },
                onTagsChange = {}
            )
        }

        composeTestRule.onNodeWithText("Description").performTextInput("Leg exercise")
        assertEquals("Leg exercise", captured)
    }

    @Test
    fun typingInTags_triggersCallback() {
        var captured = ""
        composeTestRule.setThemedContent {
            ExerciseFormContent(
                name = "",
                description = "",
                tags = "",
                onNameChange = {},
                onDescriptionChange = {},
                onTagsChange = { captured = it }
            )
        }

        composeTestRule.onNodeWithText("Tags (comma separated)").performTextInput("legs")
        assertEquals("legs", captured)
    }
}
