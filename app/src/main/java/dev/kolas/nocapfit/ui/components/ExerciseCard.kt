package dev.kolas.nocapfit.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.kolas.nocapfit.ui.model.SetUiModel

@Suppress("LongParameterList", "LongMethod")
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
    timerTotalMs: Long = 0L,
    onCancelTimer: (() -> Unit)? = null,
    showRestTime: Boolean = true,
    showAddSetButton: Boolean = false,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    showBottomDivider: Boolean = false,
    note: String? = null,
    onEditNote: (() -> Unit)? = null
) {
    var showRemoveDialog by remember { mutableStateOf(false) }
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    val dividerPx = with(LocalDensity.current) { 1.dp.toPx() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                if (showBottomDivider) {
                    drawRect(
                        color = dividerColor,
                        topLeft = Offset(0f, size.height - dividerPx),
                        size = Size(size.width, dividerPx)
                    )
                }
            }
    ) {
        ExerciseCardHeader(
            exerciseName = exerciseName,
            onExerciseTitleClick = onExerciseTitleClick,
            onAddSet = onAddSet,
            onRemoveExercise = { showRemoveDialog = true },
            onMoveUp = onMoveUp,
            onMoveDown = onMoveDown,
            onSetRestTimeForAll = onSetRestTimeForAll,
            onEditNote = onEditNote,
            hasNote = !note.isNullOrBlank(),
            showAddSetMenuItem = !showAddSetButton,
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 10.dp)
        )

        if (!note.isNullOrBlank()) {
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        SetHeaderRow(
            showTrailingIcon = showComplete || onRemoveSet != null,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

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
                timerEndAtEpochMs = timerEndAtEpochMs,
                timerTotalMs = timerTotalMs,
                onCancelTimer = onCancelTimer,
                rowHorizontalPadding = 16.dp
            )
        }

        if (showAddSetButton) {
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
                onClick = onAddSet,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("Add Set")
            }
            Spacer(modifier = Modifier.height(4.dp))
        } else {
            Spacer(modifier = Modifier.height(10.dp))
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
    onEditNote: (() -> Unit)?,
    hasNote: Boolean,
    showAddSetMenuItem: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
            onEditNote = onEditNote,
            hasNote = hasNote,
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
    timerEndAtEpochMs: Long,
    timerTotalMs: Long,
    onCancelTimer: (() -> Unit)?,
    rowHorizontalPadding: Dp = 8.dp
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
        onRemove = rememberedOnRemove,
        contentHorizontalPadding = rowHorizontalPadding
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
            timerTotalMs = if (activeTimerSetId == set.id) timerTotalMs else 0L,
            isCompleted = set.completed,
            onCancelTimer = if (activeTimerSetId == set.id) onCancelTimer else null,
            contentHorizontalPadding = 4.dp
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
    onEditNote: (() -> Unit)?,
    hasNote: Boolean,
    showAddSet: Boolean
) {
    Box {
        var showMenu by remember { mutableStateOf(false) }
        IconButton(onClick = { showMenu = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More")
        }
        if (showMenu) {
            DropdownMenu(
                expanded = true,
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
                if (onEditNote != null) {
                    DropdownMenuItem(
                        text = { Text(if (hasNote) "Edit Note" else "Add Note") },
                        onClick = {
                            showMenu = false
                            onEditNote()
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
}

@Composable
private fun SetHeaderRow(
    modifier: Modifier = Modifier,
    showTrailingIcon: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, bottom = 3.dp),
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
        ConfirmDialog(
            title = "Remove Exercise",
            message = "Remove $exerciseName from this workout?",
            confirmLabel = "Remove",
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }
}
