package dev.kolas.nocapfit.ui.util

import dev.kolas.nocapfit.util.WEIGHT_MULTIPLIER
import kotlin.math.abs
import kotlin.math.roundToInt

fun formatWeightDisplay(thousandths: Int): String {
    val sign = if (thousandths < 0) "-" else ""
    val whole = abs(thousandths) / WEIGHT_MULTIPLIER
    val frac = abs(thousandths) % WEIGHT_MULTIPLIER
    return if (frac == 0) {
        "$sign$whole"
    } else {
        "$sign$whole." + frac.toString().padStart(FRACTION_DIGITS, '0').trimEnd('0')
    }
}

fun formatWeightInput(thousandths: Int): String {
    val display = formatWeightDisplay(thousandths)
    return if ('.' in display) display else "$display.0"
}

fun parseWeight(input: String): Int {
    val value = input.toDoubleOrNull() ?: 0.0
    return (value * WEIGHT_MULTIPLIER).roundToInt()
}

private const val FRACTION_DIGITS = 3
