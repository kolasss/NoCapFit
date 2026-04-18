package com.example.nocapfit.ui.util

import com.example.nocapfit.util.WEIGHT_DIVISOR
import com.example.nocapfit.util.WEIGHT_MULTIPLIER
import kotlin.math.roundToInt

fun formatWeightDisplay(thousandths: Int): String {
    val value = thousandths / WEIGHT_DIVISOR
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        value.toBigDecimal().stripTrailingZeros().toPlainString()
    }
}

fun formatWeightInput(thousandths: Int): String {
    val value = thousandths / WEIGHT_DIVISOR
    return if (value == value.toLong().toDouble()) {
        "${value.toLong()}.0"
    } else {
        value.toBigDecimal().stripTrailingZeros().toPlainString().let { s ->
            if ('.' !in s) "$s.0" else s
        }
    }
}

fun parseWeight(input: String): Int {
    val value = input.toDoubleOrNull() ?: 0.0
    return (value * WEIGHT_MULTIPLIER).roundToInt()
}
