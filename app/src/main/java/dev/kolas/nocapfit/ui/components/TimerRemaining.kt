package dev.kolas.nocapfit.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.kolas.nocapfit.util.MILLIS_PER_SECOND
import kotlinx.coroutines.delay

@Composable
fun rememberTimerRemainingMs(isActive: Boolean, endAtEpochMs: Long): Long {
    var remainingMs by remember(endAtEpochMs, isActive) {
        mutableLongStateOf(
            if (isActive && endAtEpochMs > 0) {
                (endAtEpochMs - System.currentTimeMillis()).coerceAtLeast(0)
            } else {
                0L
            }
        )
    }
    if (isActive && endAtEpochMs > 0) {
        LaunchedEffect(endAtEpochMs) {
            while (true) {
                remainingMs = (endAtEpochMs - System.currentTimeMillis()).coerceAtLeast(0)
                if (remainingMs <= 0) break
                delay(MILLIS_PER_SECOND)
            }
        }
    }
    return remainingMs
}
