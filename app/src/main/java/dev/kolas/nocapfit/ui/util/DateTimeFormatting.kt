package dev.kolas.nocapfit.ui.util

import dev.kolas.nocapfit.util.MILLIS_PER_SECOND
import dev.kolas.nocapfit.util.SECONDS_PER_HOUR
import dev.kolas.nocapfit.util.SECONDS_PER_MINUTE
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import java.util.Locale
import kotlin.time.Instant

fun formatDate(epochMs: Long): String {
    val ldt = Instant.fromEpochMilliseconds(epochMs)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val dayOfWeek = ldt.dayOfWeek.name.lowercase()
        .replaceFirstChar { it.titlecase(Locale.getDefault()) }
    val month = ldt.month.name.lowercase()
        .replaceFirstChar { it.titlecase(Locale.getDefault()) }
    return "$dayOfWeek, ${ldt.day} $month ${ldt.year}"
}

fun formatMonth(epochMs: Long): String {
    val ldt = Instant.fromEpochMilliseconds(epochMs)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val month = ldt.month.name.lowercase()
        .replaceFirstChar { it.titlecase(Locale.getDefault()) }
    return "$month ${ldt.year}"
}

fun formatTime(epochMs: Long): String {
    val ldt = Instant.fromEpochMilliseconds(epochMs)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return "%02d:%02d".format(ldt.hour, ldt.minute)
}

fun formatDateTime(epochMs: Long): String {
    return "${formatDate(epochMs)} · ${formatTime(epochMs)}"
}

private const val DAYS_THRESHOLD = 30

fun formatRelativeDate(
    epochMs: Long,
    now: Long = System.currentTimeMillis()
): String {
    val tz = TimeZone.currentSystemDefault()
    val nowDate = Instant.fromEpochMilliseconds(now).toLocalDateTime(tz).date
    val pastDate = Instant.fromEpochMilliseconds(epochMs).toLocalDateTime(tz).date
    val daysAgo = pastDate.daysUntil(nowDate)
    return when {
        daysAgo < 1 -> "Today"
        daysAgo < 2 -> "Yesterday"
        daysAgo < DAYS_THRESHOLD -> "$daysAgo days ago"
        else -> formatDate(epochMs)
    }
}

fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / MILLIS_PER_SECOND
    val hours = totalSeconds / SECONDS_PER_HOUR
    val minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}
