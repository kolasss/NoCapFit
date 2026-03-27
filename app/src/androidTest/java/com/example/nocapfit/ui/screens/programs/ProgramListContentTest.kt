package com.example.nocapfit.ui.screens.programs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.nocapfit.data.db.entity.Exercise
import com.example.nocapfit.data.db.entity.Program
import com.example.nocapfit.data.db.entity.ProgramExercise
import com.example.nocapfit.data.db.entity.ProgramExerciseSet
import com.example.nocapfit.data.db.relation.ProgramExerciseWithSets
import com.example.nocapfit.data.db.relation.ProgramWithExercises
import com.example.nocapfit.setThemedContent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProgramListContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun testProgram(
        id: Long = 1L,
        name: String = "Push Day",
        exerciseNames: List<String> = listOf("Bench Press", "OHP")
    ): ProgramWithExercises {
        val exercises = exerciseNames.mapIndexed { index, exName ->
            ProgramExerciseWithSets(
                programExercise = ProgramExercise(
                    id = index.toLong() + 1,
                    programId = id,
                    exerciseId = index.toLong() + 1,
                    orderIndex = index
                ),
                exercise = Exercise(
                    id = index.toLong() + 1,
                    profileId = 1L,
                    name = exName
                ),
                sets = listOf(
                    ProgramExerciseSet(
                        id = index.toLong() + 1,
                        programExerciseId = index.toLong() + 1,
                        setIndex = 0,
                        weightThousandths = 75000,
                        reps = 10,
                        restTimeSeconds = 60
                    )
                )
            )
        }
        return ProgramWithExercises(
            program = Program(id = id, profileId = 1L, name = name),
            exercises = exercises
        )
    }

    @Test
    fun rendersProgramName() {
        val program = testProgram(name = "Push Day")

        composeTestRule.setThemedContent {
            ProgramListItem(
                programWithExercises = program,
                onClick = {},
                onDeleteRequest = {}
            )
        }

        composeTestRule.onNodeWithText("Push Day").assertIsDisplayed()
    }

    @Test
    fun rendersExerciseNames() {
        val program = testProgram(exerciseNames = listOf("Bench Press", "OHP"))

        composeTestRule.setThemedContent {
            ProgramListItem(
                programWithExercises = program,
                onClick = {},
                onDeleteRequest = {}
            )
        }

        composeTestRule.onNodeWithText("Bench Press, OHP").assertIsDisplayed()
    }

    @Test
    fun moreThanThreeExercises_showsMoreCount() {
        val program = testProgram(
            exerciseNames = listOf("Bench Press", "OHP", "Flyes", "Dips")
        )

        composeTestRule.setThemedContent {
            ProgramListItem(
                programWithExercises = program,
                onClick = {},
                onDeleteRequest = {}
            )
        }

        composeTestRule.onNodeWithText("+1 more").assertIsDisplayed()
    }

    @Test
    fun threeOrFewerExercises_doesNotShowMoreCount() {
        val program = testProgram(
            exerciseNames = listOf("Bench Press", "OHP", "Flyes")
        )

        composeTestRule.setThemedContent {
            ProgramListItem(
                programWithExercises = program,
                onClick = {},
                onDeleteRequest = {}
            )
        }

        composeTestRule.onNodeWithText("+", substring = true).assertDoesNotExist()
    }

    @Test
    fun clickingItem_triggersCallback() {
        var clicked = false
        val program = testProgram()

        composeTestRule.setThemedContent {
            ProgramListItem(
                programWithExercises = program,
                onClick = { clicked = true },
                onDeleteRequest = {}
            )
        }

        composeTestRule.onNodeWithText("Push Day").performClick()
        assertTrue(clicked)
    }
}
