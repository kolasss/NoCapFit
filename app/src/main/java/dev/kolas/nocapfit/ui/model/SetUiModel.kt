package dev.kolas.nocapfit.ui.model

import androidx.compose.runtime.Immutable
import dev.kolas.nocapfit.ui.util.formatWeightDisplay

@Immutable
data class SetUiModel(
    val id: Long,
    val setIndex: Int,
    val weightThousandths: Int,
    val reps: Int,
    val restTimeSeconds: Int,
    val completed: Boolean = false,
    val previousText: String? = null
)

fun formatPreviousSet(data: PreviousSetData): String =
    "${formatWeightDisplay(data.weightThousandths)}x${data.reps}"

@Immutable
data class PreviousSetData(val weightThousandths: Int, val reps: Int)

@Immutable
data class PreviousSetLookup(
    private val map: Map<Pair<Long, Int>, PreviousSetData>
) {
    operator fun get(key: Pair<Long, Int>): PreviousSetData? = map[key]
    fun isEmpty(): Boolean = map.isEmpty()
    fun toMutableMap(): MutableMap<Pair<Long, Int>, PreviousSetData> = map.toMutableMap()
    fun entries(): Set<Map.Entry<Pair<Long, Int>, PreviousSetData>> = map.entries
}

/** Previous-set display texts keyed by exerciseId, then setIndex. */
fun buildPreviousTextsByExercise(lookup: PreviousSetLookup): Map<Long, Map<Int, String>> {
    if (lookup.isEmpty()) return emptyMap()
    val accumulator = mutableMapOf<Long, MutableMap<Int, String>>()
    for ((key, data) in lookup.entries()) {
        val (exId, setIndex) = key
        accumulator.getOrPut(exId) { mutableMapOf() }[setIndex] = formatPreviousSet(data)
    }
    return accumulator
}
