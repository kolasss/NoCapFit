package com.example.nocapfit.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun RestTimerOverlay(
    remainingMs: Long,
    totalMs: Long,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentRemainingMs by remember { mutableLongStateOf(remainingMs) }
    var progress by remember { mutableFloatStateOf(if (totalMs > 0) remainingMs.toFloat() / totalMs else 0f) }

    LaunchedEffect(remainingMs) {
        currentRemainingMs = remainingMs
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            currentRemainingMs = (currentRemainingMs - 100).coerceAtLeast(0)
            progress = if (totalMs > 0) currentRemainingMs.toFloat() / totalMs else 0f
        }
    }

    val totalSeconds = (currentRemainingMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val timeText = "%d:%02d".format(minutes, seconds)

    AnimatedVisibility(
        visible = currentRemainingMs > 0,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.tertiary,
                            trackColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f)
                        )
                    }
                    Text(
                        text = "Rest: $timeText",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                TextButton(onClick = onCancel) {
                    Text("Skip")
                }
            }
        }
    }
}
