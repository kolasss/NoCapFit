package com.example.nocapfit.ui.screens.workoutedit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.nocapfit.data.db.entity.Workout
import com.example.nocapfit.data.db.entity.WorkoutExercise
import com.example.nocapfit.data.db.entity.WorkoutSet
import com.example.nocapfit.data.db.relation.WorkoutExerciseWithSets
import com.example.nocapfit.data.db.relation.WorkoutWithExercises
import com.example.nocapfit.setThemedContent
import org.junit.Rule
import org.junit.Test

class WorkoutEditContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testSet = WorkoutSet(
        id = 1L,
        workoutExerciseId = 10L,
        setIndex = 0,
        weightThousandths = 60000,
        reps = 10,
        restTimeSeconds = 90,
        completed = true
    )
    private val testData = WorkoutWithExercises(
        workout = Workout(
            id = 1L,
            profileId = 1L,
            programName = "Push Day",
            startTime = 1000L,
            endTime = 2000L
        ),
        exercises = listOf(
            WorkoutExerciseWithSets(
                workoutExercise = WorkoutExercise(
                    id = 10L,
                    workoutId = 1L,
                    exerciseName = "Bench Press",
                    exerciseId = 1L,
                    orderIndex = 0
                ),
                sets = listOf(testSet)
            )
        )
    )

    @Test
    fun nullData_showsLoadingIndicator() {
        composeTestRule.setThemedContent {
            WorkoutEditContent(
                data = null,
                programName = "",
                onProgramNameChange = {},
                onMoveExercise = { _, _ -> },
                onRemoveExercise = {},
                onAddSet = {},
                onUpdateSet = { _, _ -> },
                onUpdateReps = { _, _ -> },
                onToggleComplete = {},
                onAddExerciseClick = {}
            )
        }

        composeTestRule.onNodeWithText("Program Name").assertDoesNotExist()
    }

    @Test
    fun loadedData_showsProgramNameField() {
        composeTestRule.setThemedContent {
            WorkoutEditContent(
                data = testData,
                programName = "Push Day",
                onProgramNameChange = {},
                onMoveExercise = { _, _ -> },
                onRemoveExercise = {},
                onAddSet = {},
                onUpdateSet = { _, _ -> },
                onUpdateReps = { _, _ -> },
                onToggleComplete = {},
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
                data = testData,
                programName = "Push Day",
                onProgramNameChange = {},
                onMoveExercise = { _, _ -> },
                onRemoveExercise = {},
                onAddSet = {},
                onUpdateSet = { _, _ -> },
                onUpdateReps = { _, _ -> },
                onToggleComplete = {},
                onAddExerciseClick = {}
            )
        }

        composeTestRule.onNodeWithText("Bench Press").assertIsDisplayed()
    }

    @Test
    fun loadedData_showsAddExerciseButton() {
        composeTestRule.setThemedContent {
            WorkoutEditContent(
                data = testData,
                programName = "Push Day",
                onProgramNameChange = {},
                onMoveExercise = { _, _ -> },
                onRemoveExercise = {},
                onAddSet = {},
                onUpdateSet = { _, _ -> },
                onUpdateReps = { _, _ -> },
                onToggleComplete = {},
                onAddExerciseClick = {}
            )
        }

        composeTestRule.onNodeWithText("+ Add Exercise").assertIsDisplayed()
    }
}
