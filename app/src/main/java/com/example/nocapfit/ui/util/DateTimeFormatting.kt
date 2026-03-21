package com.example.nocapfit.ui.util

import com.example.nocapfit.util.MILLIS_PER_SECOND
import com.example.nocapfit.util.SECONDS_PER_HOUR
import com.example.nocapfit.util.SECONDS_PER_MINUTE
import kotlinx.datetime.TimeZone
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

fun formatTime(epochMs: Long): String {
    val ldt = Instant.fromEpochMilliseconds(epochMs)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return "%02d:%02d".format(ldt.hour, ldt.minute)
}

fun formatDateTime(epochMs: Long): String {
    return "${formatDate(epochMs)} · ${formatTime(epochMs)}"
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
