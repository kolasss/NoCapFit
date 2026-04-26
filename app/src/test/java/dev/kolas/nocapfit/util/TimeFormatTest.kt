package dev.kolas.nocapfit.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatTest {

    // --- ceilSecondsFromMs ---

    @Test
    fun ceilSecondsFromMs_zero_returnsZero() {
        assertEquals(0, ceilSecondsFromMs(0L))
    }

    @Test
    fun ceilSecondsFromMs_negative_returnsZero() {
        assertEquals(0, ceilSecondsFromMs(-5L))
    }

    @Test
    fun ceilSecondsFromMs_oneMs_roundsUpToOne() {
        assertEquals(1, ceilSecondsFromMs(1L))
    }

    @Test
    fun ceilSecondsFromMs_999ms_roundsUpToOne() {
        assertEquals(1, ceilSecondsFromMs(999L))
    }

    @Test
    fun ceilSecondsFromMs_exactSecond_returnsThatSecond() {
        assertEquals(1, ceilSecondsFromMs(1000L))
    }

    @Test
    fun ceilSecondsFromMs_justOverASecond_roundsUpToNext() {
        assertEquals(2, ceilSecondsFromMs(1001L))
    }

    @Test
    fun ceilSecondsFromMs_1s40ms_returnsTwo() {
        assertEquals(2, ceilSecondsFromMs(1040L))
    }

    @Test
    fun ceilSecondsFromMs_1s600ms_returnsTwo() {
        assertEquals(2, ceilSecondsFromMs(1600L))
    }

    @Test
    fun ceilSecondsFromMs_900ms_returnsOne() {
        assertEquals(1, ceilSecondsFromMs(900L))
    }

    // --- formatMmSs ---

    @Test
    fun formatMmSs_zero_returnsDoubleZeroPaddedString() {
        assertEquals("00:00", formatMmSs(0))
    }

    @Test
    fun formatMmSs_negative_returnsZeroString() {
        assertEquals("00:00", formatMmSs(-3))
    }

    @Test
    fun formatMmSs_singleSecond_padsSecondsAndMinutes() {
        assertEquals("00:05", formatMmSs(5))
    }

    @Test
    fun formatMmSs_underOneMinute_padsMinutes() {
        assertEquals("00:59", formatMmSs(59))
    }

    @Test
    fun formatMmSs_exactMinute_returnsOneMinute() {
        assertEquals("01:00", formatMmSs(60))
    }

    @Test
    fun formatMmSs_minuteAndSeconds_returnsMmSs() {
        assertEquals("01:30", formatMmSs(90))
    }

    @Test
    fun formatMmSs_tenMinutes_stillTwoDigitMinutes() {
        assertEquals("10:00", formatMmSs(600))
    }

    // --- restTimerFillProgress ---

    private val delta = 0.0001f

    @Test
    fun restTimerFillProgress_totalZero_returnsZero() {
        assertEquals(0f, restTimerFillProgress(5_000L, 0L), delta)
    }

    @Test
    fun restTimerFillProgress_remainingEqualsTotal_returnsZero() {
        assertEquals(0f, restTimerFillProgress(10_000L, 10_000L), delta)
    }

    @Test
    fun restTimerFillProgress_noRemaining_returnsOne() {
        assertEquals(1f, restTimerFillProgress(0L, 10_000L), delta)
    }

    @Test
    fun restTimerFillProgress_halfway_usesCeil() {
        // 5100ms remaining of 10s → ceil=6 → fill = 1 - 6/10 = 0.4
        assertEquals(0.4f, restTimerFillProgress(5_100L, 10_000L), delta)
    }

    @Test
    fun restTimerFillProgress_500msRemaining_ninetyPercent() {
        // 500ms of 10s → ceil=1 → fill = 1 - 1/10 = 0.9
        assertEquals(0.9f, restTimerFillProgress(500L, 10_000L), delta)
    }
}
