package com.example.nocapfit.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.nocapfit.setThemedContent
import com.example.nocapfit.ui.model.SetUiModel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExerciseCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun testSet(index: Int) = SetUiModel(
        id = index.toLong(),
        setIndex = index,
        weightThousandths = 75000,
        reps = 10,
        restTimeSeconds = 60,
        completed = false
    )

    @Test
    fun rendersExerciseName() {
        composeTestRule.setThemedContent {
            ExerciseCard(
                exerciseName = "Bench Press",
                sets = listOf(testSet(0)),
                onRemoveExercise = {},
                onAddSet = {},
                onWeightChange = { _, _ -> },
                onRepsChange = { _, _ -> },
                onToggleComplete = {}
            )
        }

        composeTestRule.onNodeWithText("Bench Press").assertIsDisplayed()
    }

    @Test
    fun rendersCorrectNumberOfSets() {
        composeTestRule.setThemedContent {
            ExerciseCard(
                exerciseName = "Bench Press",
                sets = listOf(testSet(0), testSet(1), testSet(2)),
                onRemoveExercise = {},
                onAddSet = {},
                onWeightChange = { _, _ -> },
                onRepsChange = { _, _ -> },
                onToggleComplete = {}
            )
        }

        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun addSetMenuItem_triggersCallback() {
        var addSetClicked = false
        composeTestRule.setThemedContent {
            ExerciseCard(
                exerciseName = "Bench Press",
                sets = listOf(testSet(0)),
                onRemoveExercise = {},
                onAddSet = { addSetClicked = true },
                onWeightChange = { _, _ -> },
                onRepsChange = { _, _ -> },
                onToggleComplete = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("More").performClick()
        composeTestRule.onNodeWithText("Add Set").performClick()
        assertTrue(addSetClicked)
    }

    @Test
    fun removeExerciseMenu_triggersCallbackAfterConfirmation() {
        var removeClicked = false
        composeTestRule.setThemedContent {
            ExerciseCard(
                exerciseName = "Bench Press",
                sets = listOf(testSet(0)),
                onRemoveExercise = { removeClicked = true },
                onAddSet = {},
                onWeightChange = { _, _ -> },
                onRepsChange = { _, _ -> },
                onToggleComplete = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("More").performClick()
        composeTestRule.onNodeWithText("Remove Exercise").performClick()
        composeTestRule.onNodeWithText("Remove").performClick()
        assertTrue(removeClicked)
    }

    @Test
    fun rendersOverflowMenu() {
        composeTestRule.setThemedContent {
            ExerciseCard(
                exerciseName = "Bench Press",
                sets = listOf(testSet(0)),
                onRemoveExercise = {},
                onAddSet = {},
                onWeightChange = { _, _ -> },
                onRepsChange = { _, _ -> },
                onToggleComplete = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("More").assertIsDisplayed()
    }
}
