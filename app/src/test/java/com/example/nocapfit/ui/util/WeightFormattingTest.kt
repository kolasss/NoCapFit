package com.example.nocapfit.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test

class WeightFormattingTest {

    // --- formatWeightInput (always with decimal) ---

    @Test
    fun formatWeightInput_wholeNumber_returnsWithDecimal() {
        assertEquals("80.0", formatWeightInput(80000))
    }

    @Test
    fun formatWeightInput_fractional_returnsDecimal() {
        assertEquals("75.5", formatWeightInput(75500))
    }

    @Test
    fun formatWeightInput_zero_returnsZeroPointZero() {
        assertEquals("0.0", formatWeightInput(0))
    }

    @Test
    fun formatWeightInput_smallFraction_returnsCorrectDecimal() {
        assertEquals("2.25", formatWeightInput(2250))
    }

    // --- formatWeightDisplay (strips trailing zeros) ---

    @Test
    fun formatWeightDisplay_wholeNumber_returnsInteger() {
        assertEquals("80", formatWeightDisplay(80000))
    }

    @Test
    fun formatWeightDisplay_fractional_returnsDecimal() {
        assertEquals("75.5", formatWeightDisplay(75500))
    }

    @Test
    fun formatWeightDisplay_zero_returnsZero() {
        assertEquals("0", formatWeightDisplay(0))
    }

    // --- parseWeight ---

    @Test
    fun parseWeight_wholeNumber_returnsThousandths() {
        assertEquals(80000, parseWeight("80"))
    }

    @Test
    fun parseWeight_decimal_returnsThousandths() {
        assertEquals(75500, parseWeight("75.5"))
    }

    @Test
    fun parseWeight_empty_returnsZero() {
        assertEquals(0, parseWeight(""))
    }

    @Test
    fun parseWeight_invalid_returnsZero() {
        assertEquals(0, parseWeight("abc"))
    }

    // --- Round-trip ---

    @Test
    fun roundTrip_formatInputParse_identity() {
        val testValues = listOf(0, 1000, 2250, 50000, 75500, 80000, 100000)
        for (thousandths in testValues) {
            val formatted = formatWeightInput(thousandths)
            val parsed = parseWeight(formatted)
            assertEquals("Round-trip failed for $thousandths (formatted=$formatted)", thousandths, parsed)
        }
    }
}
