package dev.kolas.nocapfit.ui.model

import dev.kolas.nocapfit.data.db.entity.WorkoutExercise
import dev.kolas.nocapfit.data.db.entity.WorkoutSet
import dev.kolas.nocapfit.data.db.relation.WorkoutExerciseWithSets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutExerciseRowTest {

    private fun set(id: Long, setIndex: Int, weightThousandths: Int = 0, reps: Int = 0): WorkoutSet =
        WorkoutSet(
            id = id,
            workoutExerciseId = 1L,
            setIndex = setIndex,
            weightThousandths = weightThousandths,
            reps = reps,
            restTimeSeconds = 60
        )

    private fun exercise(
        id: Long,
        orderIndex: Int,
        exerciseId: Long? = null,
        sets: List<WorkoutSet>
    ): WorkoutExerciseWithSets =
        WorkoutExerciseWithSets(
            workoutExercise = WorkoutExercise(
                id = id,
                workoutId = 1L,
                exerciseName = "Ex $id",
                exerciseId = exerciseId,
                orderIndex = orderIndex
            ),
            sets = sets
        )

    @Test
    fun emptyExercises_returnsEmptyList() {
        val result = buildWorkoutExerciseRows(emptyList(), PreviousSetLookup(emptyMap()))
        assertTrue(result.isEmpty())
    }

    @Test
    fun ordersExercisesByOrderIndex() {
        val exB = exercise(id = 2L, orderIndex = 0, sets = listOf(set(20L, 0)))
        val exA = exercise(id = 1L, orderIndex = 1, sets = listOf(set(10L, 0)))
        val result = buildWorkoutExerciseRows(listOf(exA, exB), PreviousSetLookup(emptyMap()))
        assertEquals(listOf(2L, 1L), result.map { it.workoutExercise.id })
    }

    @Test
    fun sortsSetsBySetIndexAndMapsFields() {
        val unsorted = listOf(
            set(11L, setIndex = 2, weightThousandths = 30000, reps = 5),
            set(10L, setIndex = 0, weightThousandths = 10000, reps = 8),
            set(12L, setIndex = 1, weightThousandths = 20000, reps = 6)
        )
        val rows = buildWorkoutExerciseRows(
            listOf(exercise(id = 1L, orderIndex = 0, sets = unsorted)),
            PreviousSetLookup(emptyMap())
        )
        val sets = rows.single().sets
        assertEquals(listOf(0, 1, 2), sets.map { it.setIndex })
        assertEquals(listOf(10000, 20000, 30000), sets.map { it.weightThousandths })
        assertEquals(listOf(8, 6, 5), sets.map { it.reps })
        assertEquals(listOf(10L, 12L, 11L), sets.map { it.id })
    }

    @Test
    fun populatesPreviousTextWhenAvailable() {
        val previous = PreviousSetLookup(
            mapOf(
                (5L to 0) to PreviousSetData(weightThousandths = 60000, reps = 10),
                (5L to 1) to PreviousSetData(weightThousandths = 65000, reps = 8)
            )
        )
        val rows = buildWorkoutExerciseRows(
            listOf(
                exercise(
                    id = 1L,
                    orderIndex = 0,
                    exerciseId = 5L,
                    sets = listOf(set(10L, setIndex = 0), set(11L, setIndex = 1), set(12L, setIndex = 2))
                )
            ),
            previous
        )
        val sets = rows.single().sets
        assertEquals("60x10", sets[0].previousText)
        assertEquals("65x8", sets[1].previousText)
        assertNull(sets[2].previousText)
    }

    @Test
    fun previousTextNullWhenExerciseIdIsNull() {
        val previous = PreviousSetLookup(
            mapOf((5L to 0) to PreviousSetData(weightThousandths = 60000, reps = 10))
        )
        val rows = buildWorkoutExerciseRows(
            listOf(exercise(id = 1L, orderIndex = 0, exerciseId = null, sets = listOf(set(10L, 0)))),
            previous
        )
        assertNull(rows.single().sets.single().previousText)
    }

    @Test
    fun builderReusesUnchangedRowAcrossEmissions() {
        val builder = WorkoutExerciseRowBuilder()
        val exA = exercise(id = 1L, orderIndex = 0, sets = listOf(set(10L, 0)))
        val exB = exercise(id = 2L, orderIndex = 1, sets = listOf(set(20L, 0)))
        val first = builder.build(listOf(exA, exB), PreviousSetLookup(emptyMap()))

        // Re-emit with only exB's sets changed; exA is the same value
        val exBChanged = exB.copy(sets = listOf(set(20L, 0, weightThousandths = 100)))
        val second = builder.build(listOf(exA, exBChanged), PreviousSetLookup(emptyMap()))

        // exA row must be reference-identical so Compose can skip its item body
        assertTrue(first[0] === second[0])
        // exB row must be a new instance (its content changed)
        assertTrue(first[1] !== second[1])
        assertEquals(100, second[1].sets[0].weightThousandths)
    }

    @Test
    fun setsByIdMatchesEverySet() {
        val rawSets = listOf(set(10L, 0), set(11L, 1), set(12L, 2))
        val rows = buildWorkoutExerciseRows(
            listOf(exercise(id = 1L, orderIndex = 0, sets = rawSets)),
            PreviousSetLookup(emptyMap())
        )
        val byId = rows.single().setsById
        assertEquals(rawSets.toSet(), byId.values.toSet())
        assertEquals(setOf(10L, 11L, 12L), byId.keys)
    }
}
