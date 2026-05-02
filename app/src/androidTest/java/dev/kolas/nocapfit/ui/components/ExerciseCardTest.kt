package dev.kolas.nocapfit.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.kolas.nocapfit.setThemedContent
import dev.kolas.nocapfit.ui.model.SetUiModel
import org.junit.Assert.assertEquals
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

    @Test
    fun note_isDisplayed_whenNonBlank() {
        composeTestRule.setThemedContent {
            ExerciseCard(
                exerciseName = "Bench Press",
                sets = listOf(testSet(0)),
                onRemoveExercise = {},
                onAddSet = {},
                onWeightChange = { _, _ -> },
                onRepsChange = { _, _ -> },
                onToggleComplete = {},
                note = "keep elbows tucked"
            )
        }

        composeTestRule.onNodeWithText("keep elbows tucked").assertIsDisplayed()
    }

    @Test
    fun note_isHidden_whenBlank() {
        composeTestRule.setThemedContent {
            ExerciseCard(
                exerciseName = "Bench Press",
                sets = listOf(testSet(0)),
                onRemoveExercise = {},
                onAddSet = {},
                onWeightChange = { _, _ -> },
                onRepsChange = { _, _ -> },
                onToggleComplete = {},
                note = "   "
            )
        }

        composeTestRule.onNodeWithText("   ").assertDoesNotExist()
    }

    @Test
    fun overflowMenu_showsAddNote_whenNoteBlank() {
        composeTestRule.setThemedContent {
            ExerciseCard(
                exerciseName = "Bench Press",
                sets = listOf(testSet(0)),
                onRemoveExercise = {},
                onAddSet = {},
                onWeightChange = { _, _ -> },
                onRepsChange = { _, _ -> },
                onToggleComplete = {},
                note = null,
                onUpdateNote = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("More").performClick()
        composeTestRule.onNodeWithText("Edit Note").assertDoesNotExist()
        composeTestRule.onNodeWithText("Add Note").assertIsDisplayed()
    }

    @Test
    fun overflowMenu_showsEditNote_whenNoteSet() {
        composeTestRule.setThemedContent {
            ExerciseCard(
                exerciseName = "Bench Press",
                sets = listOf(testSet(0)),
                onRemoveExercise = {},
                onAddSet = {},
                onWeightChange = { _, _ -> },
                onRepsChange = { _, _ -> },
                onToggleComplete = {},
                note = "keep elbows tucked",
                onUpdateNote = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("More").performClick()
        composeTestRule.onNodeWithText("Add Note").assertDoesNotExist()
        composeTestRule.onNodeWithText("Edit Note").assertIsDisplayed()
    }

    @Test
    fun overflowMenu_hidesNoteItem_whenOnUpdateNoteNull() {
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

        composeTestRule.onNodeWithContentDescription("More").performClick()
        composeTestRule.onNodeWithText("Add Note").assertDoesNotExist()
        composeTestRule.onNodeWithText("Edit Note").assertDoesNotExist()
    }

    @Test
    fun editNoteMenuItem_opensDialog_andSavingInvokesCallback() {
        var savedNote: String? = "unset"
        composeTestRule.setThemedContent {
            ExerciseCard(
                exerciseName = "Bench Press",
                sets = listOf(testSet(0)),
                onRemoveExercise = {},
                onAddSet = {},
                onWeightChange = { _, _ -> },
                onRepsChange = { _, _ -> },
                onToggleComplete = {},
                note = "keep elbows tucked",
                onUpdateNote = { savedNote = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("More").performClick()
        composeTestRule.onNodeWithText("Edit Note").performClick()
        composeTestRule.onNodeWithText("Save").performClick()
        assertEquals("keep elbows tucked", savedNote)
    }
}
