package com.example.nocapfit.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.nocapfit.ui.model.SetUiModel

@Composable
fun ExerciseCard(
    exerciseName: String,
    sets: List<SetUiModel>,
    onAddSet: () -> Unit,
    onRemoveExercise: () -> Unit,
    onWeightChange: (SetUiModel, Int) -> Unit,
    onRepsChange: (SetUiModel, Int) -> Unit,
    modifier: Modifier = Modifier,
    showComplete: Boolean = true,
    onToggleComplete: ((SetUiModel) -> Unit)? = null,
    onRestTimeChange: ((SetUiModel, Int) -> Unit)? = null,
    onRemoveSet: ((SetUiModel) -> Unit)? = null,
    onSetRestTimeForAll: (() -> Unit)? = null,
    onExerciseTitleClick: (() -> Unit)? = null,
    activeTimerSetId: Long? = null,
    timerEndAtEpochMs: Long = 0L,
    showRestTime: Boolean = true,
    showAddSetButton: Boolean = false,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null
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
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ExerciseCardHeader(
                exerciseName = exerciseName,
                onExerciseTitleClick = onExerciseTitleClick,
                onAddSet = onAddSet,
                onRemoveExercise = { showRemoveDialog = true },
                onMoveUp = onMoveUp,
                onMoveDown = onMoveDown,
                onSetRestTimeForAll = onSetRestTimeForAll,
                showAddSetMenuItem = !showAddSetButton
            )

            SetHeaderRow(showTrailingIcon = showComplete || onRemoveSet != null)

            val sortedSets = remember(sets) { sets.sortedBy { it.setIndex } }
            sortedSets.forEachIndexed { index, set ->
                ExerciseSetItem(
                    index = index,
                    set = set,
                    showComplete = showComplete,
                    canRemoveSet = onRemoveSet != null && sets.size > 1,
                    onWeightChange = onWeightChange,
                    onRepsChange = onRepsChange,
                    onToggleComplete = onToggleComplete,
                    onRestTimeChange = onRestTimeChange,
                    onRemoveSet = onRemoveSet,
                    showRestTime = showRestTime,
                    activeTimerSetId = activeTimerSetId,
                    timerEndAtEpochMs = timerEndAtEpochMs
                )
            }

            if (showAddSetButton) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onAddSet,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Set")
                }
            }
        }
    }

    RemoveExerciseDialog(
        visible = showRemoveDialog,
        exerciseName = exerciseName,
        onConfirm = {
            showRemoveDialog = false
            onRemoveExercise()
        },
        onDismiss = { showRemoveDialog = false }
    )
}

@Composable
private fun ExerciseCardHeader(
    exerciseName: String,
    onExerciseTitleClick: (() -> Unit)?,
    onAddSet: () -> Unit,
    onRemoveExercise: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    onSetRestTimeForAll: (() -> Unit)?,
    showAddSetMenuItem: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val titleModifier = Modifier.weight(1f).let { mod ->
            if (onExerciseTitleClick != null) mod.clickable(onClick = onExerciseTitleClick) else mod
        }
        Text(
            text = exerciseName,
            style = MaterialTheme.typography.titleMedium,
            color = if (onExerciseTitleClick != null) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = titleModifier
        )
        ExerciseOverflowMenu(
            onAddSetClick = onAddSet,
            onRemoveClick = onRemoveExercise,
            onMoveUp = onMoveUp,
            onMoveDown = onMoveDown,
            onSetRestTimeForAll = onSetRestTimeForAll,
            showAddSet = showAddSetMenuItem
        )
    }
}

@Composable
private fun ExerciseSetItem(
    index: Int,
    set: SetUiModel,
    showComplete: Boolean,
    canRemoveSet: Boolean,
    onWeightChange: (SetUiModel, Int) -> Unit,
    onRepsChange: (SetUiModel, Int) -> Unit,
    onToggleComplete: ((SetUiModel) -> Unit)?,
    onRestTimeChange: ((SetUiModel, Int) -> Unit)?,
    onRemoveSet: ((SetUiModel) -> Unit)?,
    showRestTime: Boolean,
    activeTimerSetId: Long?,
    timerEndAtEpochMs: Long
) {
    val rememberedOnWeightChange = remember(set) {
        {
                newWeight: Int ->
            onWeightChange(set, newWeight)
        }
    }
    val rememberedOnRepsChange = remember(set) {
        {
                newReps: Int ->
            onRepsChange(set, newReps)
        }
    }
    val rememberedOnToggleComplete = remember(set) {
        if (onToggleComplete != null) {
            { onToggleComplete(set) }
        } else {
            null
        }
    }
    val rememberedOnRemove = remember(set, canRemoveSet) {
        if (canRemoveSet && onRemoveSet != null) {
            { onRemoveSet(set) }
        } else {
            null
        }
    }
    SetRow(
        setNumber = index + 1,
        set = set,
        onWeightChange = rememberedOnWeightChange,
        onRepsChange = rememberedOnRepsChange,
        showComplete = showComplete,
        onToggleComplete = rememberedOnToggleComplete,
        onRemove = rememberedOnRemove
    )
    if (showRestTime) {
        val rememberedOnRestTimeChange = remember(set, onRestTimeChange) {
            if (onRestTimeChange != null) {
                { newSeconds: Int -> onRestTimeChange(set, newSeconds) }
            } else {
                null
            }
        }
        RestTimeRow(
            restTimeSeconds = set.restTimeSeconds,
            onRestTimeChange = rememberedOnRestTimeChange,
            isTimerActive = activeTimerSetId == set.id,
            timerEndAtEpochMs = if (activeTimerSetId == set.id) timerEndAtEpochMs else 0L,
            isCompleted = set.completed
        )
    }
}

@Composable
private fun ExerciseOverflowMenu(
    onAddSetClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    onSetRestTimeForAll: (() -> Unit)?,
    showAddSet: Boolean
) {
    Box {
        var showMenu by remember { mutableStateOf(false) }
        IconButton(onClick = { showMenu = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More")
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            if (onMoveUp != null) {
                DropdownMenuItem(
                    text = { Text("Move Up") },
                    onClick = {
                        showMenu = false
                        onMoveUp()
                    }
                )
            }
            if (onMoveDown != null) {
                DropdownMenuItem(
                    text = { Text("Move Down") },
                    onClick = {
                        showMenu = false
                        onMoveDown()
                    }
                )
            }
            if (onSetRestTimeForAll != null) {
                DropdownMenuItem(
                    text = { Text("Set Rest Time for All Sets") },
                    onClick = {
                        showMenu = false
                        onSetRestTimeForAll()
                    }
                )
            }
            if (showAddSet) {
                DropdownMenuItem(
                    text = { Text("Add Set") },
                    onClick = {
                        showMenu = false
                        onAddSetClick()
                    }
                )
            }
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
private fun SetHeaderRow(showTrailingIcon: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Set",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(20.dp)
        )
        Text(
            text = "Prev",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "kg",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(56.dp)
        )
        Text(
            text = "Reps",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(56.dp)
        )
        if (showTrailingIcon) {
            Spacer(modifier = Modifier.width(48.dp))
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
