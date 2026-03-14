package com.example.nocapfit.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class RestTimeRowKtTest {

    // --- parseMmSsToSeconds ---

    @Test
    fun parseMmSsToSeconds_empty_returnsZero() {
        assertEquals(0, parseMmSsToSeconds(""))
    }

    @Test
    fun parseMmSsToSeconds_singleDigit_returnsThatManySeconds() {
        assertEquals(5, parseMmSsToSeconds("5"))
    }

    @Test
    fun parseMmSsToSeconds_twoDigits_returnsThatManySeconds() {
        assertEquals(60, parseMmSsToSeconds("60"))
    }

    @Test
    fun parseMmSsToSeconds_threeDigits_parsesAsMinutesAndSeconds() {
        // "130" = 1 min 30 sec = 90
        assertEquals(90, parseMmSsToSeconds("130"))
    }

    @Test
    fun parseMmSsToSeconds_fourDigits_parsesAsMinutesAndSeconds() {
        // "1030" = 10 min 30 sec = 630
        assertEquals(630, parseMmSsToSeconds("1030"))
    }

    // --- secondsToMmSsDigits ---

    @Test
    fun secondsToMmSsDigits_zero_returnsZeroString() {
        assertEquals("0", secondsToMmSsDigits(0))
    }

    @Test
    fun secondsToMmSsDigits_underSixty_returnsSecondsOnly() {
        assertEquals("45", secondsToMmSsDigits(45))
    }

    @Test
    fun secondsToMmSsDigits_exactlyOneMinute_returns100() {
        assertEquals("100", secondsToMmSsDigits(60))
    }

    @Test
    fun secondsToMmSsDigits_ninetySeconds_returns130() {
        assertEquals("130", secondsToMmSsDigits(90))
    }

    @Test
    fun secondsToMmSsDigits_394seconds_returns634() {
        assertEquals("634", secondsToMmSsDigits(394))
    }

    // --- Round-trip ---

    @Test
    fun roundTrip_parseFormatParse_identity() {
        val testValues = listOf(0, 5, 30, 60, 90, 120, 394, 600)
        for (seconds in testValues) {
            val digits = secondsToMmSsDigits(seconds)
            val parsed = parseMmSsToSeconds(digits)
            assertEquals("Round-trip failed for $seconds seconds (digits=$digits)", seconds, parsed)
        }
    }
}
