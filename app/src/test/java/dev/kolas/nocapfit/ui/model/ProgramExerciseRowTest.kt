package dev.kolas.nocapfit.ui.model

import dev.kolas.nocapfit.data.db.entity.Exercise
import dev.kolas.nocapfit.ui.screens.programs.ExerciseEntry
import dev.kolas.nocapfit.ui.screens.programs.SetEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgramExerciseRowTest {

    private fun exercise(id: Long, name: String = "Ex $id") =
        Exercise(id = id, profileId = 1L, name = name)

    private fun entry(
        entryId: Long,
        exerciseId: Long = entryId,
        sets: List<SetEntry> = listOf(SetEntry())
    ): ExerciseEntry = ExerciseEntry(entryId = entryId, exercise = exercise(exerciseId), sets = sets)

    @Test
    fun emptyExercises_returnsEmpty() {
        val result = buildProgramExerciseRows(emptyList(), PreviousSetLookup(emptyMap()))
        assertTrue(result.isEmpty())
    }

    @Test
    fun parsesWeightFromString() {
        val rows = buildProgramExerciseRows(
            listOf(entry(1L, sets = listOf(SetEntry(weight = "60.5", reps = "8", restTimeSeconds = "130")))),
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
            listOf(entry(1L, sets = listOf(SetEntry(weight = "", reps = "", restTimeSeconds = "")))),
            PreviousSetLookup(emptyMap())
        )
        val set = rows.single().sets.single()
        assertEquals(0, set.weightThousandths)
        assertEquals(0, set.reps)
        assertEquals(0, set.restTimeSeconds)
    }

    @Test
    fun trailingDotInWeightParsesAsWholeNumber() {
        val rows = buildProgramExerciseRows(
            listOf(entry(1L, sets = listOf(SetEntry(weight = "1.", reps = "5", restTimeSeconds = "0")))),
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
            listOf(entry(1L, sets = listOf(SetEntry(), SetEntry(), SetEntry()))),
            previous
        )
        val sets = rows.single().sets
        assertEquals("60x10", sets[0].previousText)
        assertEquals("65x8", sets[1].previousText)
        assertNull(sets[2].previousText)
    }

    @Test
    fun builderReusesUnchangedRowAcrossEmissions() {
        val builder = ProgramExerciseRowBuilder()
        val a = entry(entryId = 1L, sets = listOf(SetEntry(weight = "10")))
        val b = entry(entryId = 2L, sets = listOf(SetEntry(weight = "20")))
        val first = builder.build(listOf(a, b), PreviousSetLookup(emptyMap()))

        // Only b's weight string changes (simulating a keystroke in exercise b's weight field)
        val bChanged = b.copy(sets = listOf(SetEntry(weight = "25")))
        val second = builder.build(listOf(a, bChanged), PreviousSetLookup(emptyMap()))

        assertTrue(first[0] === second[0])
        assertTrue(first[1] !== second[1])
        assertEquals(25000, second[1].sets[0].weightThousandths)
    }

    @Test
    fun preservesExerciseOrder() {
        val rows = buildProgramExerciseRows(
            listOf(
                entry(entryId = 1L, exerciseId = 2L, sets = listOf(SetEntry())),
                entry(entryId = 2L, exerciseId = 1L, sets = listOf(SetEntry()))
            ),
            PreviousSetLookup(emptyMap())
        )
        assertEquals(listOf(1L, 2L), rows.map { it.exerciseEntry.entryId })
    }

    @Test
    fun duplicateExerciseIdsCacheIndependently() {
        // Same Exercise can be added twice (e.g. supersets). Distinct entryIds keep their rows
        // separate in the cache.
        val builder = ProgramExerciseRowBuilder()
        val a = entry(entryId = 1L, exerciseId = 5L, sets = listOf(SetEntry(weight = "10")))
        val b = entry(entryId = 2L, exerciseId = 5L, sets = listOf(SetEntry(weight = "20")))
        val first = builder.build(listOf(a, b), PreviousSetLookup(emptyMap()))

        assertEquals(2, first.size)
        assertEquals(10000, first[0].sets[0].weightThousandths)
        assertEquals(20000, first[1].sets[0].weightThousandths)

        // Edit only a; b's row must stay reference-equal even though they share exercise.id.
        val aChanged = a.copy(sets = listOf(SetEntry(weight = "12")))
        val second = builder.build(listOf(aChanged, b), PreviousSetLookup(emptyMap()))

        assertTrue(first[1] === second[1])
        assertEquals(12000, second[0].sets[0].weightThousandths)
        assertNotNull(second[0])
    }
}
