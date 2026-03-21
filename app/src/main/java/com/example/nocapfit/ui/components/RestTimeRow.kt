package com.example.nocapfit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nocapfit.util.MILLIS_PER_SECOND
import com.example.nocapfit.util.SECONDS_PER_MINUTE
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
    timerEndAtEpochMs: Long = 0L
) {
    var digits by remember(restTimeSeconds) {
        mutableStateOf(secondsToMmSsDigits(restTimeSeconds))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
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

        if (isTimerActive && timerEndAtEpochMs > 0) {
            // Show live countdown
            var remainingMs by remember { mutableLongStateOf(timerEndAtEpochMs - System.currentTimeMillis()) }

            LaunchedEffect(timerEndAtEpochMs) {
                while (true) {
                    remainingMs = (timerEndAtEpochMs - System.currentTimeMillis()).coerceAtLeast(0)
                    if (remainingMs <= 0) break
                    delay(100)
                }
            }

            val totalSecs = (remainingMs / MILLIS_PER_SECOND).coerceAtLeast(0)
            val mins = totalSecs / SECONDS_PER_MINUTE
            val secs = totalSecs % SECONDS_PER_MINUTE
            Text(
                text = "%d:%02d".format(mins, secs),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        } else if (onRestTimeChange != null) {
            // Editable input
            OutlinedTextField(
                value = digits,
                onValueChange = { newValue ->
                    val filtered = newValue.filter { it.isDigit() }.take(4)
                    digits = filtered
                    onRestTimeChange(parseMmSsToSeconds(filtered))
                },
                label = { Text("Rest") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = MmSsVisualTransformation(),
                modifier = Modifier.width(100.dp)
            )
        } else {
            // Read-only display
            val mins = restTimeSeconds / SECONDS_PER_MINUTE
            val secs = restTimeSeconds % SECONDS_PER_MINUTE
            Text(
                text = "%d:%02d".format(mins, secs),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
