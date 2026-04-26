package dev.kolas.nocapfit.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.kolas.nocapfit.setThemedContent
import dev.kolas.nocapfit.ui.model.SetUiModel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SetRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun testSet(
        weightThousandths: Int = 75000,
        reps: Int = 10,
        completed: Boolean = false
    ) = SetUiModel(
        id = 1L,
        setIndex = 0,
        weightThousandths = weightThousandths,
        reps = reps,
        restTimeSeconds = 60,
        completed = completed
    )

    @Test
    fun rendersSetNumber() {
        composeTestRule.setThemedContent {
            SetRow(
                setNumber = 1,
                set = testSet(),
                onWeightChange = {},
                onRepsChange = {},
                onToggleComplete = {}
            )
        }

        composeTestRule.onNodeWithText("1").assertIsDisplayed()
    }

    @Test
    fun rendersWeightValue() {
        composeTestRule.setThemedContent {
            SetRow(
                setNumber = 1,
                set = testSet(weightThousandths = 75000),
                onWeightChange = {},
                onRepsChange = {},
                onToggleComplete = {}
            )
        }

        composeTestRule.onNodeWithText("75").assertIsDisplayed()
    }

    @Test
    fun rendersRepsValue() {
        composeTestRule.setThemedContent {
            SetRow(
                setNumber = 1,
                set = testSet(reps = 10),
                onWeightChange = {},
                onRepsChange = {},
                onToggleComplete = {}
            )
        }

        composeTestRule.onNodeWithText("10").assertIsDisplayed()
    }

    @Test
    fun rendersCompleteToggle() {
        composeTestRule.setThemedContent {
            SetRow(
                setNumber = 1,
                set = testSet(),
                onWeightChange = {},
                onRepsChange = {},
                onToggleComplete = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Complete set").assertIsDisplayed()
    }

    @Test
    fun toggleComplete_triggersCallback() {
        var toggled = false
        composeTestRule.setThemedContent {
            SetRow(
                setNumber = 1,
                set = testSet(),
                onWeightChange = {},
                onRepsChange = {},
                onToggleComplete = { toggled = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Complete set").performClick()
        assertTrue(toggled)
    }

    @Test
    fun zeroWeight_showsEmptyField() {
        composeTestRule.setThemedContent {
            SetRow(
                setNumber = 1,
                set = testSet(weightThousandths = 0),
                onWeightChange = {},
                onRepsChange = {},
                onToggleComplete = {}
            )
        }

        composeTestRule.onNodeWithText("75").assertDoesNotExist()
    }

    @Test
    fun zeroReps_showsEmptyField() {
        composeTestRule.setThemedContent {
            SetRow(
                setNumber = 1,
                set = testSet(reps = 0),
                onWeightChange = {},
                onRepsChange = {},
                onToggleComplete = {}
            )
        }

        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }
}
