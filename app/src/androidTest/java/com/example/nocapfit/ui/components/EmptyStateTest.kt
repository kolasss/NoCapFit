package com.example.nocapfit.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.nocapfit.setThemedContent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EmptyStateTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersTitle() {
        composeTestRule.setThemedContent {
            EmptyState(
                icon = Icons.Default.FitnessCenter,
                title = "No exercises yet",
                subtitle = "Add exercises to use in your workouts"
            )
        }

        composeTestRule.onNodeWithText("No exercises yet").assertIsDisplayed()
    }

    @Test
    fun rendersSubtitle() {
        composeTestRule.setThemedContent {
            EmptyState(
                icon = Icons.Default.FitnessCenter,
                title = "No exercises yet",
                subtitle = "Add exercises to use in your workouts"
            )
        }

        composeTestRule.onNodeWithText("Add exercises to use in your workouts")
            .assertIsDisplayed()
    }

    @Test
    fun withActionLabel_showsActionButton() {
        composeTestRule.setThemedContent {
            EmptyState(
                icon = Icons.Default.FitnessCenter,
                title = "No exercises yet",
                subtitle = "Add exercises",
                actionLabel = "New Exercise",
                onAction = {}
            )
        }

        composeTestRule.onNodeWithText("New Exercise").assertIsDisplayed()
    }

    @Test
    fun withoutActionLabel_hidesActionButton() {
        composeTestRule.setThemedContent {
            EmptyState(
                icon = Icons.Default.FitnessCenter,
                title = "No exercises yet",
                subtitle = "Add exercises"
            )
        }

        composeTestRule.onNodeWithText("New Exercise").assertDoesNotExist()
    }

    @Test
    fun actionButton_triggersCallback() {
        var clicked = false
        composeTestRule.setThemedContent {
            EmptyState(
                icon = Icons.Default.FitnessCenter,
                title = "No exercises yet",
                subtitle = "Add exercises",
                actionLabel = "New Exercise",
                onAction = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("New Exercise").performClick()
        assertTrue(clicked)
    }
}
