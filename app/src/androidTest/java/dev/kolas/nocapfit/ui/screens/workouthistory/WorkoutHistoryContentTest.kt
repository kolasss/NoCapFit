package dev.kolas.nocapfit.ui.screens.workouthistory

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.kolas.nocapfit.setThemedContent
import org.junit.Assert.assertTrue
import java.util.Locale
import org.junit.Rule
import org.junit.Test

class WorkoutHistoryContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testMonth = "March 2024"

    private fun testItem(
        id: Long = 1L,
        title: String = "Push Day",
        exerciseSummary: String = "Bench Press, OHP"
    ): WorkoutHistoryItemUi =
        WorkoutHistoryItemUi(
            id = id,
            title = title,
            dateTimeText = "Wednesday, 27 March 2024, 12:00",
            durationText = "1h 0m",
            exerciseSummary = exerciseSummary
        )

    private fun groups(vararg items: WorkoutHistoryItemUi): List<WorkoutHistoryMonthGroup> =
        listOf(WorkoutHistoryMonthGroup(testMonth, items.toList()))

    @Test
    fun loading_showsProgressIndicator() {
        composeTestRule.setThemedContent {
            WorkoutHistoryContent(
                isLoading = true,
                groups = emptyList(),
                onStartWorkout = {},
                onWorkoutClick = {}
            )
        }

        composeTestRule.onNodeWithText("No workouts yet").assertDoesNotExist()
    }

    @Test
    fun emptyList_showsEmptyState() {
        composeTestRule.setThemedContent {
            WorkoutHistoryContent(
                isLoading = false,
                groups = emptyList(),
                onStartWorkout = {},
                onWorkoutClick = {}
            )
        }

        composeTestRule.onNodeWithText("No workouts yet").assertIsDisplayed()
    }

    @Test
    fun emptyList_startWorkoutAction() {
        var startClicked = false
        composeTestRule.setThemedContent {
            WorkoutHistoryContent(
                isLoading = false,
                groups = emptyList(),
                onStartWorkout = { startClicked = true },
                onWorkoutClick = {}
            )
        }

        composeTestRule.onNodeWithText("Start Workout").performClick()
        assertTrue(startClicked)
    }

    @Test
    fun populatedList_showsProgramName() {
        composeTestRule.setThemedContent {
            WorkoutHistoryContent(
                isLoading = false,
                groups = groups(testItem(title = "Push Day")),
                onStartWorkout = {},
                onWorkoutClick = {}
            )
        }

        composeTestRule.onNodeWithText("Push Day").assertIsDisplayed()
    }

    @Test
    fun populatedList_showsDateHeader() {
        composeTestRule.setThemedContent {
            WorkoutHistoryContent(
                isLoading = false,
                groups = groups(testItem()),
                onStartWorkout = {},
                onWorkoutClick = {}
            )
        }

        composeTestRule.onNodeWithText(testMonth.uppercase(Locale.ROOT)).assertIsDisplayed()
    }

    @Test
    fun populatedList_showsExerciseNames() {
        composeTestRule.setThemedContent {
            WorkoutHistoryContent(
                isLoading = false,
                groups = groups(testItem(exerciseSummary = "Bench Press, OHP")),
                onStartWorkout = {},
                onWorkoutClick = {}
            )
        }

        composeTestRule.onNodeWithText("Bench Press, OHP").assertIsDisplayed()
    }

    @Test
    fun workoutClick_reportsWorkoutId() {
        var clickedId: Long? = null
        composeTestRule.setThemedContent {
            WorkoutHistoryContent(
                isLoading = false,
                groups = groups(testItem(id = 42L, title = "Push Day")),
                onStartWorkout = {},
                onWorkoutClick = { clickedId = it }
            )
        }

        composeTestRule.onNodeWithText("Push Day").performClick()
        assertTrue(clickedId == 42L)
    }
}
