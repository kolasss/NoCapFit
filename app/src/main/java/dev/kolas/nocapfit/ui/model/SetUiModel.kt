package dev.kolas.nocapfit.ui.model

import androidx.compose.runtime.Immutable
import dev.kolas.nocapfit.util.WEIGHT_DIVISOR

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

fun formatPreviousSet(data: PreviousSetData): String {
    val kg = data.weightThousandths / WEIGHT_DIVISOR
    val weightStr = if (kg == kg.toLong().toDouble()) {
        kg.toLong().toString()
    } else {
        kg.toBigDecimal().stripTrailingZeros().toPlainString()
    }
    return "${weightStr}x${data.reps}"
}

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

@Immutable
data class PreviousTextsByExercise(private val byExId: Map<Long, Map<Int, String>>) {
    fun forExercise(exId: Long): PreviousTextsForExercise =
        byExId[exId]?.let(::PreviousTextsForExercise) ?: PreviousTextsForExercise.Empty

    companion object {
        val Empty = PreviousTextsByExercise(emptyMap())
    }
}

@Immutable
data class PreviousTextsForExercise(private val byIndex: Map<Int, String>) {
    operator fun get(setIndex: Int): String? = byIndex[setIndex]

    companion object {
        val Empty = PreviousTextsForExercise(emptyMap())
    }
}

fun buildPreviousTextsByExercise(lookup: PreviousSetLookup): PreviousTextsByExercise {
    if (lookup.isEmpty()) return PreviousTextsByExercise.Empty
    val accumulator = mutableMapOf<Long, MutableMap<Int, String>>()
    for ((key, data) in lookup.entries()) {
        val (exId, setIndex) = key
        accumulator.getOrPut(exId) { mutableMapOf() }[setIndex] = formatPreviousSet(data)
    }
    return PreviousTextsByExercise(accumulator.mapValues { it.value.toMap() })
}
