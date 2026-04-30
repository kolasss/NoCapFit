package dev.kolas.nocapfit.ui.screens.programs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.kolas.nocapfit.data.db.entity.Exercise
import dev.kolas.nocapfit.data.db.entity.Program
import dev.kolas.nocapfit.data.db.entity.ProgramExercise
import dev.kolas.nocapfit.data.db.entity.ProgramExerciseSet
import dev.kolas.nocapfit.data.db.relation.ProgramExerciseWithSets
import dev.kolas.nocapfit.data.db.relation.ProgramWithExercises
import dev.kolas.nocapfit.setThemedContent
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
                onEdit = {},
                onCopy = {},
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
                onEdit = {},
                onCopy = {},
                onDeleteRequest = {}
            )
        }

        composeTestRule.onNodeWithText("Bench Press, OHP").assertIsDisplayed()
    }

    @Test
    fun moreThanThreeExercises_showsAllNames() {
        val program = testProgram(
            exerciseNames = listOf("Bench Press", "OHP", "Flyes", "Dips")
        )

        composeTestRule.setThemedContent {
            ProgramListItem(
                programWithExercises = program,
                onEdit = {},
                onCopy = {},
                onDeleteRequest = {}
            )
        }

        composeTestRule.onNodeWithText("Bench Press, OHP, Flyes, Dips").assertIsDisplayed()
        composeTestRule.onNodeWithText("more", substring = true).assertDoesNotExist()
    }

    @Test
    fun menuEdit_triggersCallback() {
        var edited = false
        val program = testProgram()

        composeTestRule.setThemedContent {
            ProgramListItem(
                programWithExercises = program,
                onEdit = { edited = true },
                onCopy = {},
                onDeleteRequest = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("More").performClick()
        composeTestRule.onNodeWithText("Edit").performClick()
        assertTrue(edited)
    }

    @Test
    fun menuCopy_triggersCallback() {
        var copied = false
        val program = testProgram()

        composeTestRule.setThemedContent {
            ProgramListItem(
                programWithExercises = program,
                onEdit = {},
                onCopy = { copied = true },
                onDeleteRequest = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("More").performClick()
        composeTestRule.onNodeWithText("Create Copy").performClick()
        assertTrue(copied)
    }

    @Test
    fun menuDelete_triggersCallback() {
        var deleteRequested = false
        val program = testProgram()

        composeTestRule.setThemedContent {
            ProgramListItem(
                programWithExercises = program,
                onEdit = {},
                onCopy = {},
                onDeleteRequest = { deleteRequested = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("More").performClick()
        composeTestRule.onNodeWithText("Delete").performClick()
        assertTrue(deleteRequested)
    }
}
