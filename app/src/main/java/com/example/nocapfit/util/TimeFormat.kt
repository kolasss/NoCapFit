package com.example.nocapfit.util

fun ceilSecondsFromMs(remainingMs: Long): Int {
    if (remainingMs <= 0) return 0
    return ((remainingMs + MILLIS_PER_SECOND - 1) / MILLIS_PER_SECOND).toInt()
}

fun formatMmSs(totalSeconds: Int): String {
    val s = totalSeconds.coerceAtLeast(0)
    val mins = s / SECONDS_PER_MINUTE
    val secs = s % SECONDS_PER_MINUTE
    return "%02d:%02d".format(mins, secs)
}

fun restTimerFillProgress(remainingMs: Long, totalMs: Long): Float {
    val totalSec = (totalMs / MILLIS_PER_SECOND).toInt()
    if (totalSec <= 0) return 0f
    return (1f - ceilSecondsFromMs(remainingMs).toFloat() / totalSec).coerceIn(0f, 1f)
}
