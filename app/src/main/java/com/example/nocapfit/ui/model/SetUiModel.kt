package com.example.nocapfit.ui.model

import androidx.compose.runtime.Immutable
import com.example.nocapfit.util.WEIGHT_DIVISOR

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
}
