package dev.kolas.nocapfit.ui.screens.exercises

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.kolas.nocapfit.data.db.entity.Exercise
import dev.kolas.nocapfit.setThemedContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExerciseListContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun testExercise(id: Long, name: String, tags: String = "") = Exercise(
        id = id,
        profileId = 1L,
        name = name,
        tags = tags
    )

    @Test
    fun emptyList_showsEmptyState() {
        composeTestRule.setThemedContent {
            ExerciseListContent(
                exercises = emptyList(),
                searchQuery = "",
                onSearchQueryChange = {},
                onShowAddDialog = {},
                onExerciseClick = {}
            )
        }

        composeTestRule.onNodeWithText("No exercises yet").assertIsDisplayed()
    }

    @Test
    fun emptyList_showsNewExerciseAction() {
        var showAddCalled = false
        composeTestRule.setThemedContent {
            ExerciseListContent(
                exercises = emptyList(),
                searchQuery = "",
                onSearchQueryChange = {},
                onShowAddDialog = { showAddCalled = true },
                onExerciseClick = {}
            )
        }

        composeTestRule.onNodeWithText("New Exercise").performClick()
        assertTrue(showAddCalled)
    }

    @Test
    fun populatedList_rendersExerciseNames() {
        val exercises = listOf(
            testExercise(1, "Bench Press"),
            testExercise(2, "Squat")
        )

        composeTestRule.setThemedContent {
            ExerciseListContent(
                exercises = exercises,
                searchQuery = "",
                onSearchQueryChange = {},
                onShowAddDialog = {},
                onExerciseClick = {}
            )
        }

        composeTestRule.onNodeWithText("Bench Press").assertIsDisplayed()
        composeTestRule.onNodeWithText("Squat").assertIsDisplayed()
    }

    @Test
    fun exerciseWithTags_rendersTags() {
        val exercises = listOf(
            testExercise(1, "Bench Press", tags = "chest, triceps")
        )

        composeTestRule.setThemedContent {
            ExerciseListContent(
                exercises = exercises,
                searchQuery = "",
                onSearchQueryChange = {},
                onShowAddDialog = {},
                onExerciseClick = {}
            )
        }

        composeTestRule.onNodeWithText("chest, triceps").assertIsDisplayed()
    }

    @Test
    fun clickingExercise_triggersCallback() {
        val exercises = listOf(testExercise(1, "Bench Press"))
        var clickedExercise: Exercise? = null

        composeTestRule.setThemedContent {
            ExerciseListContent(
                exercises = exercises,
                searchQuery = "",
                onSearchQueryChange = {},
                onShowAddDialog = {},
                onExerciseClick = { clickedExercise = it }
            )
        }

        composeTestRule.onNodeWithText("Bench Press").performClick()
        assertEquals(1L, clickedExercise?.id)
    }

    @Test
    fun searchBar_displaysCurrentQuery() {
        composeTestRule.setThemedContent {
            ExerciseListContent(
                exercises = emptyList(),
                searchQuery = "bench",
                onSearchQueryChange = {},
                onShowAddDialog = {},
                onExerciseClick = {}
            )
        }

        composeTestRule.onNodeWithText("bench").assertIsDisplayed()
    }
}
