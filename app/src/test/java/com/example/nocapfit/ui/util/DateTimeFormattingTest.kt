package com.example.nocapfit.ui.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Instant

class DateTimeFormattingTest {

    // formatDuration tests

    @Test
    fun formatDuration_zeroMs_returns0s() {
        assertEquals("0s", formatDuration(0L))
    }

    @Test
    fun formatDuration_secondsOnly() {
        assertEquals("45s", formatDuration(45_000L))
    }

    @Test
    fun formatDuration_exactly59s() {
        assertEquals("59s", formatDuration(59_000L))
    }

    @Test
    fun formatDuration_exactly60s_showsMinutesAndSeconds() {
        assertEquals("1m 0s", formatDuration(60_000L))
    }

    @Test
    fun formatDuration_minutesAndSeconds() {
        assertEquals("5m 30s", formatDuration(330_000L))
    }

    @Test
    fun formatDuration_exactly3599s() {
        assertEquals("59m 59s", formatDuration(3_599_000L))
    }

    @Test
    fun formatDuration_exactly3600s_showsHoursAndMinutes() {
        assertEquals("1h 0m", formatDuration(3_600_000L))
    }

    @Test
    fun formatDuration_hoursAndMinutes() {
        assertEquals("2h 15m", formatDuration(8_100_000L))
    }

    @Test
    fun formatDuration_hoursDropsSeconds() {
        // 1h 30m 45s should display as 1h 30m (seconds omitted)
        assertEquals("1h 30m", formatDuration(5_445_000L))
    }

    // formatDate tests

    @Test
    fun formatDate_containsWeekdayAndYear() {
        // 2026-01-01 00:00 UTC = Thursday
        val epochMs = 1767225600000L
        val result = formatDate(epochMs)
        val ldt = Instant.fromEpochMilliseconds(epochMs)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        assertTrue("Should contain the year", result.contains(ldt.year.toString()))
        assertTrue("Should contain a comma after weekday", result.contains(","))
    }

    @Test
    fun formatDate_matchesExpectedStructure() {
        val epochMs = 1767225600000L
        val result = formatDate(epochMs)
        // Pattern: "Weekday, Day Month Year"
        assertTrue(
            "Should match 'Weekday, Day Month Year' pattern",
            result.matches(Regex("[A-Z][a-z]+, \\d{1,2} [A-Z][a-z]+ \\d{4}"))
        )
    }

    // formatTime tests

    @Test
    fun formatTime_returns24hFormat() {
        val epochMs = 1767225600000L
        val result = formatTime(epochMs)
        // Should be HH:MM format
        assertTrue(
            "Should match HH:MM pattern",
            result.matches(Regex("\\d{2}:\\d{2}"))
        )
    }

    // formatDateTime tests

    @Test
    fun formatDateTime_containsBulletSeparator() {
        val epochMs = 1767225600000L
        val result = formatDateTime(epochMs)
        assertTrue("Should contain ' · ' separator", result.contains(" · "))
    }

    @Test
    fun formatDateTime_combinesDateAndTime() {
        val epochMs = 1767225600000L
        val result = formatDateTime(epochMs)
        val expectedDate = formatDate(epochMs)
        val expectedTime = formatTime(epochMs)
        assertEquals("$expectedDate · $expectedTime", result)
    }
}
