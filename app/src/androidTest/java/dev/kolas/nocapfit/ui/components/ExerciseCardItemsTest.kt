package dev.kolas.nocapfit.ui.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.kolas.nocapfit.setThemedContent
import dev.kolas.nocapfit.ui.model.SetUiModel
import dev.kolas.nocapfit.util.DEFAULT_REST_TIME_SECONDS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExerciseCardItemsTest {

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

    @Suppress("LongParameterList")
    private fun setCardItemsContent(
        sets: List<SetUiModel>,
        onAddSet: () -> Unit = {},
        onRemoveExercise: () -> Unit = {},
        onSetRestTimeForAll: ((Int) -> Unit)? = null,
        note: String? = null,
        onUpdateNote: ((String?) -> Unit)? = null,
        showAddSetButton: Boolean = false
    ) {
        composeTestRule.setThemedContent {
            LazyColumn {
                exerciseCardItems(
                    keyPrefix = "test-1",
                    exerciseName = "Bench Press",
                    sets = sets,
                    onAddSet = onAddSet,
                    onRemoveExercise = onRemoveExercise,
                    onWeightChange = { _, _ -> },
                    onRepsChange = { _, _ -> },
                    onToggleComplete = {},
                    onSetRestTimeForAll = onSetRestTimeForAll,
                    note = note,
                    onUpdateNote = onUpdateNote,
                    showAddSetButton = showAddSetButton
                )
            }
        }
    }

    @Test
    fun rendersExerciseName() {
        setCardItemsContent(sets = listOf(testSet(0)))

        composeTestRule.onNodeWithText("Bench Press").assertIsDisplayed()
    }

    @Test
    fun rendersCorrectNumberOfSets() {
        setCardItemsContent(sets = listOf(testSet(0), testSet(1), testSet(2)))

        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun addSetMenuItem_triggersCallback() {
        var addSetClicked = false
        setCardItemsContent(sets = listOf(testSet(0)), onAddSet = { addSetClicked = true })

        composeTestRule.onNodeWithContentDescription("More").performClick()
        composeTestRule.onNodeWithText("Add Set").performClick()
        assertTrue(addSetClicked)
    }

    @Test
    fun addSetButton_shownInFooter_andTriggersCallback() {
        var addSetClicked = false
        setCardItemsContent(
            sets = listOf(testSet(0)),
            onAddSet = { addSetClicked = true },
            showAddSetButton = true
        )

        composeTestRule.onNodeWithText("Add Set").performClick()
        assertTrue(addSetClicked)
    }

    @Test
    fun removeExerciseMenu_triggersCallbackAfterConfirmation() {
        var removeClicked = false
        setCardItemsContent(sets = listOf(testSet(0)), onRemoveExercise = { removeClicked = true })

        composeTestRule.onNodeWithContentDescription("More").performClick()
        composeTestRule.onNodeWithText("Remove Exercise").performClick()
        composeTestRule.onNodeWithText("Remove").performClick()
        assertTrue(removeClicked)
    }

    @Test
    fun rendersOverflowMenu() {
        setCardItemsContent(sets = listOf(testSet(0)))

        composeTestRule.onNodeWithContentDescription("More").assertIsDisplayed()
    }

    @Test
    fun note_isDisplayed_whenNonBlank() {
        setCardItemsContent(sets = listOf(testSet(0)), note = "keep elbows tucked")

        composeTestRule.onNodeWithText("keep elbows tucked").assertIsDisplayed()
    }

    @Test
    fun note_isHidden_whenBlank() {
        setCardItemsContent(sets = listOf(testSet(0)), note = "   ")

        composeTestRule.onNodeWithText("   ").assertDoesNotExist()
    }

    @Test
    fun overflowMenu_showsAddNote_whenNoteBlank() {
        setCardItemsContent(sets = listOf(testSet(0)), note = null, onUpdateNote = {})

        composeTestRule.onNodeWithContentDescription("More").performClick()
        composeTestRule.onNodeWithText("Edit Note").assertDoesNotExist()
        composeTestRule.onNodeWithText("Add Note").assertIsDisplayed()
    }

    @Test
    fun overflowMenu_showsEditNote_whenNoteSet() {
        setCardItemsContent(sets = listOf(testSet(0)), note = "keep elbows tucked", onUpdateNote = {})

        composeTestRule.onNodeWithContentDescription("More").performClick()
        composeTestRule.onNodeWithText("Add Note").assertDoesNotExist()
        composeTestRule.onNodeWithText("Edit Note").assertIsDisplayed()
    }

    @Test
    fun overflowMenu_hidesNoteItem_whenOnUpdateNoteNull() {
        setCardItemsContent(sets = listOf(testSet(0)))

        composeTestRule.onNodeWithContentDescription("More").performClick()
        composeTestRule.onNodeWithText("Add Note").assertDoesNotExist()
        composeTestRule.onNodeWithText("Edit Note").assertDoesNotExist()
    }

    @Test
    fun addNoteMenuItem_opensDialog_andSavingBlankInvokesNullCallback() {
        var savedNote: String? = "unset"
        setCardItemsContent(
            sets = listOf(testSet(0)),
            note = null,
            onUpdateNote = { savedNote = it }
        )

        composeTestRule.onNodeWithContentDescription("More").performClick()
        composeTestRule.onNodeWithText("Add Note").performClick()
        composeTestRule.onNodeWithText("Save").performClick()
        assertNull(savedNote)
    }

    @Test
    fun editNoteMenuItem_opensDialog_andSavingInvokesCallback() {
        var savedNote: String? = "unset"
        setCardItemsContent(
            sets = listOf(testSet(0)),
            note = "keep elbows tucked",
            onUpdateNote = { savedNote = it }
        )

        composeTestRule.onNodeWithContentDescription("More").performClick()
        composeTestRule.onNodeWithText("Edit Note").performClick()
        composeTestRule.onNodeWithText("Save").performClick()
        assertEquals("keep elbows tucked", savedNote)
    }

    @Test
    fun setRestTimeForAllMenuItem_opensDialog_andApplyInvokesCallback() {
        var seconds: Int? = null
        setCardItemsContent(sets = listOf(testSet(0)), onSetRestTimeForAll = { seconds = it })

        composeTestRule.onNodeWithContentDescription("More").performClick()
        composeTestRule.onNodeWithText("Set Rest Time for All Sets").performClick()
        composeTestRule.onNodeWithText("Apply").performClick()
        assertEquals(DEFAULT_REST_TIME_SECONDS, seconds)
    }
}
