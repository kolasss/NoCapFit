package com.example.nocapfit.ui.screens.programs

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgramFormViewModelCompanionTest {

    // --- formatWeight ---

    @Test
    fun formatWeight_wholeNumber_returnsWithDecimal() {
        assertEquals("80.0", ProgramFormViewModel.formatWeight(80000))
    }

    @Test
    fun formatWeight_fractional_returnsDecimal() {
        assertEquals("75.5", ProgramFormViewModel.formatWeight(75500))
    }

    @Test
    fun formatWeight_zero_returnsZeroPointZero() {
        assertEquals("0.0", ProgramFormViewModel.formatWeight(0))
    }

    @Test
    fun formatWeight_smallFraction_returnsCorrectDecimal() {
        assertEquals("2.25", ProgramFormViewModel.formatWeight(2250))
    }

    // --- parseWeight ---

    @Test
    fun parseWeight_wholeNumber_returnsThousandths() {
        assertEquals(80000, ProgramFormViewModel.parseWeight("80"))
    }

    @Test
    fun parseWeight_decimal_returnsThousandths() {
        assertEquals(75500, ProgramFormViewModel.parseWeight("75.5"))
    }

    @Test
    fun parseWeight_empty_returnsZero() {
        assertEquals(0, ProgramFormViewModel.parseWeight(""))
    }

    @Test
    fun parseWeight_invalid_returnsZero() {
        assertEquals(0, ProgramFormViewModel.parseWeight("abc"))
    }

    // --- Round-trip ---

    @Test
    fun roundTrip_formatParse_identity() {
        val testValues = listOf(0, 1000, 2250, 50000, 75500, 80000, 100000)
        for (thousandths in testValues) {
            val formatted = ProgramFormViewModel.formatWeight(thousandths)
            val parsed = ProgramFormViewModel.parseWeight(formatted)
            assertEquals("Round-trip failed for $thousandths (formatted=$formatted)", thousandths, parsed)
        }
    }
}
