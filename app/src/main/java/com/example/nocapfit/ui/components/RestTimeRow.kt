package com.example.nocapfit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nocapfit.util.MILLIS_PER_SECOND
import com.example.nocapfit.util.SECONDS_PER_MINUTE
import com.example.nocapfit.util.ceilSecondsFromMs
import com.example.nocapfit.util.formatMmSs
import com.example.nocapfit.util.restTimerFillProgress
import kotlinx.coroutines.delay

/**
 * Parse mm:ss digit string to total seconds.
 * "130" → 90s (1 min 30 sec), "634" → 394s (6 min 34 sec), "60" → 60s
 */
fun parseMmSsToSeconds(digits: String): Int {
    if (digits.isEmpty()) return 0
    return if (digits.length <= 2) {
        digits.toIntOrNull() ?: 0
    } else {
        val seconds = digits.takeLast(2).toIntOrNull() ?: 0
        val minutes = digits.dropLast(2).toIntOrNull() ?: 0
        minutes * SECONDS_PER_MINUTE + seconds
    }
}

/**
 * Convert total seconds to raw digit string for mm:ss input.
 * 90 → "130", 394 → "634", 60 → "100"
 */
fun secondsToMmSsDigits(totalSeconds: Int): String {
    if (totalSeconds <= 0) return ""
    val minutes = totalSeconds / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return if (minutes == 0) {
        seconds.toString()
    } else {
        "$minutes${seconds.toString().padStart(2, '0')}"
    }
}

@Composable
fun RestTimeRow(
    restTimeSeconds: Int,
    onRestTimeChange: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
    isTimerActive: Boolean = false,
    timerEndAtEpochMs: Long = 0L,
    timerTotalMs: Long = 0L,
    isCompleted: Boolean = false,
    onCancelTimer: (() -> Unit)? = null
) {
    val remainingMs = rememberTimerRemainingMs(isTimerActive, timerEndAtEpochMs)
    val fillProgress = if (isTimerActive) {
        restTimerFillProgress(remainingMs, timerTotalMs)
    } else {
        0f
    }

    val completedColor = MaterialTheme.colorScheme.primaryContainer
    val surfaceColor = MaterialTheme.colorScheme.surface
    val progressColor = MaterialTheme.colorScheme.tertiaryContainer

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .drawBehind {
                    drawRect(
                        if (isCompleted && !isTimerActive) completedColor else surfaceColor
                    )
                    if (fillProgress > 0f) {
                        drawRect(
                            color = progressColor,
                            size = Size(size.width * fillProgress, size.height)
                        )
                    }
                }
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Timer,
                contentDescription = "Rest time",
                tint = if (isTimerActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            RestTimeRowContent(
                isTimerActive = isTimerActive,
                timerEndAtEpochMs = timerEndAtEpochMs,
                remainingMs = remainingMs,
                onCancelTimer = onCancelTimer,
                isCompleted = isCompleted,
                onRestTimeChange = onRestTimeChange,
                restTimeSeconds = restTimeSeconds
            )
        }
    }
}

@Composable
private fun rememberTimerRemainingMs(isTimerActive: Boolean, timerEndAtEpochMs: Long): Long {
    var remainingMs by remember(timerEndAtEpochMs) {
        mutableLongStateOf(
            if (isTimerActive && timerEndAtEpochMs > 0) {
                (timerEndAtEpochMs - System.currentTimeMillis()).coerceAtLeast(0)
            } else {
                0L
            }
        )
    }
    if (isTimerActive && timerEndAtEpochMs > 0) {
        LaunchedEffect(timerEndAtEpochMs) {
            while (true) {
                remainingMs = (timerEndAtEpochMs - System.currentTimeMillis()).coerceAtLeast(0)
                if (remainingMs <= 0) break
                delay(MILLIS_PER_SECOND)
            }
        }
    }
    return remainingMs
}

@Composable
private fun RowScope.RestTimeRowContent(
    isTimerActive: Boolean,
    timerEndAtEpochMs: Long,
    remainingMs: Long,
    onCancelTimer: (() -> Unit)?,
    isCompleted: Boolean,
    onRestTimeChange: ((Int) -> Unit)?,
    restTimeSeconds: Int
) {
    if (isTimerActive && timerEndAtEpochMs > 0) {
        RestTimerCountdown(remainingMs)
        if (onCancelTimer != null) {
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onCancelTimer) {
                Text("Skip")
            }
        }
    } else if (!isCompleted && onRestTimeChange != null) {
        RestTimeInput(restTimeSeconds = restTimeSeconds, onRestTimeChange = onRestTimeChange)
    } else {
        Text(
            text = formatMmSs(restTimeSeconds),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RestTimeInput(
    restTimeSeconds: Int,
    onRestTimeChange: (Int) -> Unit,
    modifier: Modifier = Modifier.width(80.dp)
) {
    var digits by remember { mutableStateOf(secondsToMmSsDigits(restTimeSeconds)) }
    var lastExternalSeconds by remember { mutableIntStateOf(restTimeSeconds) }
    var lastSentSeconds by remember { mutableIntStateOf(restTimeSeconds) }
    if (restTimeSeconds != lastExternalSeconds) {
        lastExternalSeconds = restTimeSeconds
        if (restTimeSeconds != lastSentSeconds) {
            digits = secondsToMmSsDigits(restTimeSeconds)
            lastSentSeconds = restTimeSeconds
        }
    }
    CompactInput(
        value = digits,
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isDigit() }.take(4)
            digits = filtered
            val seconds = parseMmSsToSeconds(filtered)
            lastSentSeconds = seconds
            onRestTimeChange(seconds)
        },
        keyboardType = KeyboardType.Number,
        visualTransformation = mmSsTransformation,
        modifier = modifier
    )
}

@Composable
private fun RestTimerCountdown(remainingMs: Long) {
    Text(
        text = formatMmSs(ceilSecondsFromMs(remainingMs)),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

private val mmSsTransformation = MmSsVisualTransformation()
