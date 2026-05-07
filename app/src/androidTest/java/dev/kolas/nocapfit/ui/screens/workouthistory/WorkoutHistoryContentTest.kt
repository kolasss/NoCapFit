package dev.kolas.nocapfit.ui.screens.workouthistory

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.kolas.nocapfit.data.db.entity.Workout
import dev.kolas.nocapfit.data.db.entity.WorkoutExercise
import dev.kolas.nocapfit.data.db.relation.WorkoutExerciseWithSets
import dev.kolas.nocapfit.data.db.relation.WorkoutWithExercises
import dev.kolas.nocapfit.setThemedContent
import dev.kolas.nocapfit.ui.util.formatMonth
import org.junit.Assert.assertTrue
import java.util.Locale
import org.junit.Rule
import org.junit.Test

class WorkoutHistoryContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testStartTime = 1711540800000L // 2024-03-27 12:00:00 UTC
    private val testEndTime = testStartTime + 3600000L // +1 hour

    private fun testWorkout(
        id: Long = 1L,
        programName: String? = "Push Day",
        startTime: Long = testStartTime,
        endTime: Long? = testEndTime,
        exerciseNames: List<String> = listOf("Bench Press", "OHP")
    ): WorkoutWithExercises {
        val exercises = exerciseNames.mapIndexed { index, name ->
            WorkoutExerciseWithSets(
                workoutExercise = WorkoutExercise(
                    id = index.toLong() + 1,
                    workoutId = id,
                    exerciseName = name,
                    orderIndex = index
                ),
                sets = emptyList()
            )
        }
        return WorkoutWithExercises(
            workout = Workout(
                id = id,
                profileId = 1L,
                programName = programName,
                startTime = startTime,
                endTime = endTime
            ),
            exercises = exercises
        )
    }

    @Test
    fun loading_showsProgressIndicator() {
        composeTestRule.setThemedContent {
            WorkoutHistoryContent(
                isLoading = true,
                grouped = emptyMap(),
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
                grouped = emptyMap(),
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
                grouped = emptyMap(),
                onStartWorkout = { startClicked = true },
                onWorkoutClick = {}
            )
        }

        composeTestRule.onNodeWithText("Start Workout").performClick()
        assertTrue(startClicked)
    }

    @Test
    fun populatedList_showsProgramName() {
        val workout = testWorkout(programName = "Push Day")
        val dateHeader = formatMonth(workout.workout.startTime)

        composeTestRule.setThemedContent {
            WorkoutHistoryContent(
                isLoading = false,
                grouped = mapOf(dateHeader to listOf(workout)),
                onStartWorkout = {},
                onWorkoutClick = {}
            )
        }

        composeTestRule.onNodeWithText("Push Day").assertIsDisplayed()
    }

    @Test
    fun populatedList_showsDateHeader() {
        val workout = testWorkout()
        val dateHeader = formatMonth(workout.workout.startTime)

        composeTestRule.setThemedContent {
            WorkoutHistoryContent(
                isLoading = false,
                grouped = mapOf(dateHeader to listOf(workout)),
                onStartWorkout = {},
                onWorkoutClick = {}
            )
        }

        composeTestRule.onNodeWithText(dateHeader.uppercase(Locale.ROOT)).assertIsDisplayed()
    }

    @Test
    fun nullProgramName_showsFreeWorkout() {
        val workout = testWorkout(programName = null)
        val dateHeader = formatMonth(workout.workout.startTime)

        composeTestRule.setThemedContent {
            WorkoutHistoryContent(
                isLoading = false,
                grouped = mapOf(dateHeader to listOf(workout)),
                onStartWorkout = {},
                onWorkoutClick = {}
            )
        }

        composeTestRule.onNodeWithText("Free Workout").assertIsDisplayed()
    }

    @Test
    fun populatedList_showsExerciseNames() {
        val workout = testWorkout(exerciseNames = listOf("Bench Press", "OHP"))
        val dateHeader = formatMonth(workout.workout.startTime)

        composeTestRule.setThemedContent {
            WorkoutHistoryContent(
                isLoading = false,
                grouped = mapOf(dateHeader to listOf(workout)),
                onStartWorkout = {},
                onWorkoutClick = {}
            )
        }

        composeTestRule.onNodeWithText("Bench Press, OHP").assertIsDisplayed()
    }

    @Test
    fun moreThanThreeExercises_showsAllNames() {
        val workout = testWorkout(
            exerciseNames = listOf("Bench", "OHP", "Flyes", "Dips")
        )
        val dateHeader = formatMonth(workout.workout.startTime)

        composeTestRule.setThemedContent {
            WorkoutHistoryContent(
                isLoading = false,
                grouped = mapOf(dateHeader to listOf(workout)),
                onStartWorkout = {},
                onWorkoutClick = {}
            )
        }

        composeTestRule.onNodeWithText("Bench, OHP, Flyes, Dips").assertIsDisplayed()
        composeTestRule.onNodeWithText("more", substring = true).assertDoesNotExist()
    }
}
