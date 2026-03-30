package com.example.nocapfit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.nocapfit.data.db.entity.WorkoutSet

@Composable
fun ExerciseCard(
    exerciseName: String,
    sets: List<WorkoutSet>,
    workoutExerciseId: Long,
    onRemoveExercise: (Long) -> Unit,
    onAddSet: (Long) -> Unit,
    onWeightChange: (WorkoutSet, Int) -> Unit,
    onRepsChange: (WorkoutSet, Int) -> Unit,
    onToggleComplete: (WorkoutSet) -> Unit,
    modifier: Modifier = Modifier,
    onRestTimeChange: ((WorkoutSet, Int) -> Unit)? = null,
    activeTimerSetId: Long? = null,
    timerEndAtEpochMs: Long = 0L
) {
    var showRemoveDialog by remember { mutableStateOf(false) }
    val accentColor = MaterialTheme.colorScheme.tertiaryContainer
    val accentWidthPx = with(LocalDensity.current) { 4.dp.toPx() }

    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(accentColor, size = Size(accentWidthPx, size.height))
                }
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                ExerciseOverflowMenu(
                    onAddSetClick = { onAddSet(workoutExerciseId) },
                    onRemoveClick = { showRemoveDialog = true }
                )
            }

            val sortedSets = remember(sets) { sets.sortedBy { it.setIndex } }
            sortedSets.forEachIndexed { index, workoutSet ->
                SetRow(
                    setNumber = index + 1,
                    workoutSet = workoutSet,
                    onWeightChange = { newWeight -> onWeightChange(workoutSet, newWeight) },
                    onRepsChange = { newReps -> onRepsChange(workoutSet, newReps) },
                    onToggleComplete = { onToggleComplete(workoutSet) }
                )
                RestTimeRow(
                    restTimeSeconds = workoutSet.restTimeSeconds,
                    onRestTimeChange = if (onRestTimeChange != null) {
                        { newSeconds -> onRestTimeChange(workoutSet, newSeconds) }
                    } else {
                        null
                    },
                    isTimerActive = activeTimerSetId == workoutSet.id,
                    timerEndAtEpochMs = if (activeTimerSetId == workoutSet.id) timerEndAtEpochMs else 0L
                )
            }
        }
    }

    RemoveExerciseDialog(
        visible = showRemoveDialog,
        exerciseName = exerciseName,
        onConfirm = {
            showRemoveDialog = false
            onRemoveExercise(workoutExerciseId)
        },
        onDismiss = { showRemoveDialog = false }
    )
}

@Composable
private fun ExerciseOverflowMenu(onAddSetClick: () -> Unit, onRemoveClick: () -> Unit) {
    Box {
        var showMenu by remember { mutableStateOf(false) }
        IconButton(onClick = { showMenu = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More")
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Add Set") },
                onClick = {
                    showMenu = false
                    onAddSetClick()
                }
            )
            DropdownMenuItem(
                text = { Text("Remove Exercise") },
                onClick = {
                    showMenu = false
                    onRemoveClick()
                }
            )
        }
    }
}

@Composable
private fun RemoveExerciseDialog(
    visible: Boolean,
    exerciseName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Remove Exercise") },
            text = { Text("Remove $exerciseName from this workout?") },
            confirmButton = {
                TextButton(onClick = onConfirm) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        )
    }
}
