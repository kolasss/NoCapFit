package dev.kolas.nocapfit.ui.screens.workouthistory

import dev.kolas.nocapfit.data.db.entity.Workout
import dev.kolas.nocapfit.data.db.entity.WorkoutExercise
import dev.kolas.nocapfit.data.db.relation.WorkoutWithExerciseNames
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutHistoryUiModelTest {

    private val startTime = 1711540800000L // 2024-03-27 12:00:00 UTC

    private fun source(
        id: Long = 1L,
        programName: String? = "Push Day",
        exerciseNames: List<String> = listOf("Bench Press", "OHP")
    ): WorkoutWithExerciseNames =
        WorkoutWithExerciseNames(
            workout = Workout(
                id = id,
                profileId = 1L,
                programName = programName,
                startTime = startTime,
                endTime = startTime + 3600000L
            ),
            // Reverse order with descending orderIndex: mapping must sort by orderIndex.
            exercises = exerciseNames.mapIndexed { index, name ->
                WorkoutExercise(
                    id = index.toLong() + 1,
                    workoutId = id,
                    exerciseName = name,
                    orderIndex = index
                )
            }.reversed()
        )

    @Test
    fun mapsTitleFromProgramName() {
        assertEquals("Push Day", toHistoryItemUi(source()).title)
    }

    @Test
    fun nullProgramNameBecomesFreeWorkout() {
        assertEquals("Free Workout", toHistoryItemUi(source(programName = null)).title)
    }

    @Test
    fun exerciseSummaryOrderedByOrderIndex() {
        val item = toHistoryItemUi(source(exerciseNames = listOf("Bench Press", "OHP", "Dips")))
        assertEquals("Bench Press, OHP, Dips", item.exerciseSummary)
    }

    @Test
    fun groupsWorkoutsByMonthPreservingOrder() {
        val april = source(id = 2L).let {
            it.copy(workout = it.workout.copy(startTime = startTime + 30L * 24 * 3600 * 1000))
        }
        val groups = groupByMonth(listOf(april, source(id = 1L)))
        assertEquals(2, groups.size)
        assertEquals(listOf(2L), groups[0].workouts.map { w -> w.id })
        assertEquals(listOf(1L), groups[1].workouts.map { w -> w.id })
    }
}
