package dev.kolas.nocapfit.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RestTimeForAllDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var restTimeSeconds by remember { mutableIntStateOf(DEFAULT_REST_TIME_SECONDS) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Rest Time for All Sets") },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                RestTimeInput(
                    restTimeSeconds = restTimeSeconds,
                    onRestTimeChange = { restTimeSeconds = it },
                    modifier = Modifier.width(80.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(restTimeSeconds) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private const val DEFAULT_REST_TIME_SECONDS = 60
