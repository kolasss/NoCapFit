package dev.kolas.nocapfit.ui.model

import dev.kolas.nocapfit.data.db.entity.Exercise
import dev.kolas.nocapfit.ui.screens.programs.ExerciseEntry
import dev.kolas.nocapfit.ui.screens.programs.SetEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgramExerciseRowTest {

    private fun exercise(id: Long, name: String = "Ex $id") =
        Exercise(id = id, profileId = 1L, name = name)

    @Test
    fun emptyExercises_returnsEmpty() {
        val result = buildProgramExerciseRows(emptyList(), PreviousSetLookup(emptyMap()))
        assertTrue(result.isEmpty())
    }

    @Test
    fun parsesWeightFromString() {
        val rows = buildProgramExerciseRows(
            listOf(
                ExerciseEntry(
                    exercise = exercise(1L),
                    sets = listOf(SetEntry(weight = "60.5", reps = "8", restTimeSeconds = "130"))
                )
            ),
            PreviousSetLookup(emptyMap())
        )
        val set = rows.single().sets.single()
        assertEquals(60500, set.weightThousandths)
        assertEquals(8, set.reps)
        assertEquals(90, set.restTimeSeconds)
    }

    @Test
    fun blankInputsProduceZeros() {
        val rows = buildProgramExerciseRows(
            listOf(
                ExerciseEntry(
                    exercise = exercise(1L),
                    sets = listOf(SetEntry(weight = "", reps = "", restTimeSeconds = ""))
                )
            ),
            PreviousSetLookup(emptyMap())
        )
        val set = rows.single().sets.single()
        assertEquals(0, set.weightThousandths)
        assertEquals(0, set.reps)
        assertEquals(0, set.restTimeSeconds)
    }

    @Test
    fun partialWeightStringParsesToZero() {
        val rows = buildProgramExerciseRows(
            listOf(
                ExerciseEntry(
                    exercise = exercise(1L),
                    sets = listOf(SetEntry(weight = "1.", reps = "5", restTimeSeconds = "0"))
                )
            ),
            PreviousSetLookup(emptyMap())
        )
        val set = rows.single().sets.single()
        // "1." parses to 1.0 → 1000 thousandths (parseWeight handles trailing dot)
        assertEquals(1000, set.weightThousandths)
        assertEquals(5, set.reps)
    }

    @Test
    fun previousTextPopulatedFromLookup() {
        val previous = PreviousSetLookup(
            mapOf(
                (1L to 0) to PreviousSetData(weightThousandths = 60000, reps = 10),
                (1L to 1) to PreviousSetData(weightThousandths = 65000, reps = 8)
            )
        )
        val rows = buildProgramExerciseRows(
            listOf(
                ExerciseEntry(
                    exercise = exercise(1L),
                    sets = listOf(SetEntry(), SetEntry(), SetEntry())
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
    fun preservesExerciseOrder() {
        val rows = buildProgramExerciseRows(
            listOf(
                ExerciseEntry(exercise = exercise(2L, "B"), sets = listOf(SetEntry())),
                ExerciseEntry(exercise = exercise(1L, "A"), sets = listOf(SetEntry()))
            ),
            PreviousSetLookup(emptyMap())
        )
        assertEquals(listOf("B", "A"), rows.map { it.exerciseEntry.exercise.name })
    }
}
