package com.example.nocapfit.ui.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Locale

fun formatDate(epochMs: Long): String {
    val ldt = Instant.fromEpochMilliseconds(epochMs)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val dayOfWeek = ldt.dayOfWeek.name.lowercase()
        .replaceFirstChar { it.titlecase(Locale.getDefault()) }
    val month = ldt.month.name.lowercase()
        .replaceFirstChar { it.titlecase(Locale.getDefault()) }
    return "$dayOfWeek, ${ldt.dayOfMonth} $month ${ldt.year}"
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
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}
