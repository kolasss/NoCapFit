package dev.kolas.nocapfit.ui.screens.workoutedit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import dev.kolas.nocapfit.data.db.entity.WorkoutExercise
import dev.kolas.nocapfit.data.db.entity.WorkoutSet
import dev.kolas.nocapfit.setThemedContent
import dev.kolas.nocapfit.ui.model.SetUiModel
import dev.kolas.nocapfit.ui.model.WorkoutExerciseRow
import org.junit.Rule
import org.junit.Test

class WorkoutEditContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testRows = listOf(
        WorkoutExerciseRow(
            workoutExercise = WorkoutExercise(
                id = 10L,
                workoutId = 1L,
                exerciseName = "Bench Press",
                exerciseId = 1L,
                orderIndex = 0
            ),
            sets = listOf(
                SetUiModel(
                    id = 1L,
                    setIndex = 0,
                    weightThousandths = 60000,
                    reps = 10,
                    restTimeSeconds = 90,
                    completed = true
                )
            ),
            setsById = mapOf(
                1L to WorkoutSet(
                    id = 1L,
                    workoutExerciseId = 10L,
                    setIndex = 0,
                    weightThousandths = 60000,
                    reps = 10,
                    restTimeSeconds = 90,
                    completed = true
                )
            )
        )
    )

    @Test
    fun notLoaded_hidesContent() {
        composeTestRule.setThemedContent {
            WorkoutEditContent(
                dataLoaded = false,
                rows = emptyList(),
                programName = "",
                onProgramNameChange = {},
                onMoveExercise = { _, _ -> },
                onRemoveExercise = {},
                onAddSet = {},
                onUpdateSet = { _, _ -> },
                onUpdateReps = { _, _ -> },
                onToggleComplete = {},
                onUpdateNote = { _, _ -> },
                onAddExerciseClick = {}
            )
        }

        composeTestRule.onNodeWithText("Program Name").assertDoesNotExist()
    }

    @Test
    fun loadedData_showsProgramNameField() {
        composeTestRule.setThemedContent {
            WorkoutEditContent(
                dataLoaded = true,
                rows = testRows,
                programName = "Push Day",
                onProgramNameChange = {},
                onMoveExercise = { _, _ -> },
                onRemoveExercise = {},
                onAddSet = {},
                onUpdateSet = { _, _ -> },
                onUpdateReps = { _, _ -> },
                onToggleComplete = {},
                onUpdateNote = { _, _ -> },
                onAddExerciseClick = {}
            )
        }

        composeTestRule.onNodeWithText("Program Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Push Day").assertIsDisplayed()
    }

    @Test
    fun loadedData_showsExerciseName() {
        composeTestRule.setThemedContent {
            WorkoutEditContent(
                dataLoaded = true,
                rows = testRows,
                programName = "Push Day",
                onProgramNameChange = {},
                onMoveExercise = { _, _ -> },
                onRemoveExercise = {},
                onAddSet = {},
                onUpdateSet = { _, _ -> },
                onUpdateReps = { _, _ -> },
                onToggleComplete = {},
                onUpdateNote = { _, _ -> },
                onAddExerciseClick = {}
            )
        }

        composeTestRule.onNodeWithText("Bench Press").assertIsDisplayed()
    }

    @Test
    fun loadedData_showsAddExerciseButton() {
        composeTestRule.setThemedContent {
            WorkoutEditContent(
                dataLoaded = true,
                rows = testRows,
                programName = "Push Day",
                onProgramNameChange = {},
                onMoveExercise = { _, _ -> },
                onRemoveExercise = {},
                onAddSet = {},
                onUpdateSet = { _, _ -> },
                onUpdateReps = { _, _ -> },
                onToggleComplete = {},
                onUpdateNote = { _, _ -> },
                onAddExerciseClick = {}
            )
        }

        composeTestRule.onNodeWithText("+ Add Exercise").assertIsDisplayed()
    }
}
