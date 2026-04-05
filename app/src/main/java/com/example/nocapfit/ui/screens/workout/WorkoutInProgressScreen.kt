package com.example.nocapfit.ui.screens.workout

import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nocapfit.service.TimerCoordinator
import com.example.nocapfit.ui.components.ExerciseCard
import com.example.nocapfit.ui.components.ExercisePickerSheet
import com.example.nocapfit.ui.components.RestTimerOverlay
import com.example.nocapfit.ui.navigation.Screen
import com.example.nocapfit.util.MILLIS_PER_SECOND
import com.example.nocapfit.util.SECONDS_PER_HOUR
import com.example.nocapfit.util.SECONDS_PER_MINUTE
import com.example.nocapfit.util.WEIGHT_DIVISOR
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutInProgressScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: WorkoutInProgressViewModel = hiltViewModel(),
    onMinimize: ((Long) -> Unit)? = null
) {
    val workout by viewModel.workout.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val availableExercises by viewModel.availableExercises.collectAsState()
    val previousSets by viewModel.previousSets.collectAsState()

    var showFinishDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showExercisePicker by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    val onBack: () -> Unit = {
        if (onMinimize != null) onMinimize(viewModel.workoutId) else navController.popBackStack()
    }
    val navigateToHistory: () -> Unit = {
        navController.navigate(Screen.WorkoutHistory.route) {
            popUpTo(Screen.WorkoutHistory.route) { inclusive = true }
        }
    }
    BackHandler(onBack = onBack)
    KeepScreenOn()

    Scaffold(
        modifier = modifier,
        topBar = {
            WorkoutTopAppBar(
                startTime = workout?.workout?.startTime,
                showOverflowMenu = showOverflowMenu,
                onBackClick = onBack,
                onFinishClick = { showFinishDialog = true },
                onOverflowClick = { showOverflowMenu = true },
                onOverflowDismiss = { showOverflowMenu = false },
                onCancelWorkoutClick = {
                    showOverflowMenu = false
                    showCancelDialog = true
                }
            )
        }
    ) { padding ->
        WorkoutContent(
            padding = padding,
            workout = workout,
            timerState = timerState,
            previousSets = previousSets,
            onMoveExercise = viewModel::moveExercise,
            onRemoveExercise = viewModel::removeExercise,
            onAddSet = viewModel::addSet,
            onUpdateSet = viewModel::updateSet,
            onCompleteSet = viewModel::completeSet,
            onRevertSet = viewModel::revertSet,
            onCancelTimer = viewModel::cancelTimer,
            onAddExerciseClick = { showExercisePicker = true },
            onFinishClick = { showFinishDialog = true }
        )
    }

    WorkoutDialogs(
        showFinishDialog = showFinishDialog,
        showCancelDialog = showCancelDialog,
        onFinishDismiss = { showFinishDialog = false },
        onCancelDismiss = { showCancelDialog = false },
        onFinishConfirm = {
            showFinishDialog = false
            if (viewModel.finishWorkout()) navigateToHistory()
        },
        onCancelConfirm = {
            showCancelDialog = false
            viewModel.cancelWorkout()
            navigateToHistory()
        }
    )

    if (showExercisePicker) {
        ExercisePickerSheet(
            exercises = availableExercises,
            onSelectExercise = { exercise ->
                viewModel.addExerciseFromDb(exercise.id, exercise.name)
                showExercisePicker = false
            },
            onDismiss = { showExercisePicker = false }
        )
    }
}

@Composable
private fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

@Composable
private fun rememberElapsedTime(startTime: Long?): String {
    var elapsedText by remember { mutableStateOf("00:00:00") }
    LaunchedEffect(startTime) {
        if (startTime == null) return@LaunchedEffect
        while (true) {
            val elapsed = System.currentTimeMillis() - startTime
            elapsedText = formatElapsedTime(elapsed)
            delay(MILLIS_PER_SECOND)
        }
    }
    return elapsedText
}

@Composable
private fun WorkoutContent(
    padding: PaddingValues,
    workout: com.example.nocapfit.data.db.relation.WorkoutWithExercises?,
    timerState: TimerCoordinator.TimerUiState,
    previousSets: Map<Pair<Long, Int>, PreviousSetData>,
    onMoveExercise: (Long, Int) -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onAddSet: (Long) -> Unit,
    onUpdateSet: (com.example.nocapfit.data.db.entity.WorkoutSet) -> Unit,
    onCompleteSet: (Long, Int) -> Unit,
    onRevertSet: (Long) -> Unit,
    onCancelTimer: () -> Unit,
    onAddExerciseClick: () -> Unit,
    onFinishClick: () -> Unit
) {
    if (workout == null) {
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) { Text("Loading workout...") }
        return
    }

    val sortedExercises = remember(workout.exercises) {
        workout.exercises.sortedBy { it.workoutExercise.orderIndex }
    }
    val activeTimerSetId = (timerState as? TimerCoordinator.TimerUiState.Running)?.workoutSetId
    val timerEndAtEpochMs = (timerState as? TimerCoordinator.TimerUiState.Running)
        ?.endAtEpochMs ?: 0L

    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
        WorkoutExerciseList(
            sortedExercises = sortedExercises,
            timerState = timerState,
            activeTimerSetId = activeTimerSetId,
            timerEndAtEpochMs = timerEndAtEpochMs,
            previousSets = previousSets,
            onMoveExercise = onMoveExercise,
            onRemoveExercise = onRemoveExercise,
            onAddSet = onAddSet,
            onUpdateSet = onUpdateSet,
            onCompleteSet = onCompleteSet,
            onRevertSet = onRevertSet,
            onAddExerciseClick = onAddExerciseClick,
            onFinishClick = onFinishClick
        )

        if (timerState is TimerCoordinator.TimerUiState.Running) {
            RestTimerOverlay(
                endAtEpochMs = timerState.endAtEpochMs,
                totalMs = timerState.totalMs,
                onCancel = onCancelTimer,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun WorkoutExerciseList(
    sortedExercises: List<com.example.nocapfit.data.db.relation.WorkoutExerciseWithSets>,
    timerState: TimerCoordinator.TimerUiState,
    activeTimerSetId: Long?,
    timerEndAtEpochMs: Long,
    previousSets: Map<Pair<Long, Int>, PreviousSetData>,
    onMoveExercise: (Long, Int) -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onAddSet: (Long) -> Unit,
    onUpdateSet: (com.example.nocapfit.data.db.entity.WorkoutSet) -> Unit,
    onCompleteSet: (Long, Int) -> Unit,
    onRevertSet: (Long) -> Unit,
    onAddExerciseClick: () -> Unit,
    onFinishClick: () -> Unit
) {
    val completedSets = remember(sortedExercises) {
        sortedExercises.sumOf { ex -> ex.sets.count { it.completed } }
    }
    val totalSets = remember(sortedExercises) {
        sortedExercises.sumOf { it.sets.size }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = if (timerState is TimerCoordinator.TimerUiState.Running) 120.dp else 80.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(contentType = "summary") {
            WorkoutSummaryCard(
                exerciseCount = sortedExercises.size,
                completedSets = completedSets,
                totalSets = totalSets
            )
        }

        itemsIndexed(
            sortedExercises,
            key = { _, item -> item.workoutExercise.id },
            contentType = { _, _ -> "exercise" }
        ) { index, exerciseWithSets ->
            ExerciseCardItem(
                exerciseWithSets = exerciseWithSets,
                index = index,
                lastIndex = sortedExercises.lastIndex,
                previousSets = previousSets,
                activeTimerSetId = activeTimerSetId,
                timerEndAtEpochMs = timerEndAtEpochMs,
                onMoveExercise = onMoveExercise,
                onRemoveExercise = onRemoveExercise,
                onAddSet = onAddSet,
                onUpdateSet = onUpdateSet,
                onCompleteSet = onCompleteSet,
                onRevertSet = onRevertSet
            )
        }

        item(contentType = "controls") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onAddExerciseClick) { Text("+ Add Exercise") }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onFinishClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Finish Workout")
                }
            }
        }
    }
}

@Composable
private fun ExerciseCardItem(
    exerciseWithSets: com.example.nocapfit.data.db.relation.WorkoutExerciseWithSets,
    index: Int,
    lastIndex: Int,
    previousSets: Map<Pair<Long, Int>, PreviousSetData>,
    activeTimerSetId: Long?,
    timerEndAtEpochMs: Long,
    onMoveExercise: (Long, Int) -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onAddSet: (Long) -> Unit,
    onUpdateSet: (com.example.nocapfit.data.db.entity.WorkoutSet) -> Unit,
    onCompleteSet: (Long, Int) -> Unit,
    onRevertSet: (Long) -> Unit
) {
    val id = exerciseWithSets.workoutExercise.id
    val exId = exerciseWithSets.workoutExercise.exerciseId
    val exercisePrevSets = remember(exId, previousSets) {
        if (exId != null) {
            previousSets.filterKeys { it.first == exId }
                .map { (key, data) -> key.second to formatPrevSet(data) }
                .toMap()
        } else {
            null
        }
    }
    val onMoveUp = remember(id, index) {
        if (index > 0) { { onMoveExercise(id, -1) } } else null
    }
    val onMoveDown = remember(id, index, lastIndex) {
        if (index < lastIndex) { { onMoveExercise(id, 1) } } else null
    }
    ExerciseCard(
        exerciseName = exerciseWithSets.workoutExercise.exerciseName,
        sets = exerciseWithSets.sets,
        workoutExerciseId = id,
        onRemoveExercise = onRemoveExercise,
        onAddSet = onAddSet,
        onWeightChange = { ws, w -> onUpdateSet(ws.copy(weightThousandths = w)) },
        onRepsChange = { ws, r -> onUpdateSet(ws.copy(reps = r)) },
        onToggleComplete = { ws ->
            if (ws.completed) {
                onRevertSet(ws.id)
            } else {
                onCompleteSet(ws.id, ws.restTimeSeconds)
            }
        },
        onRestTimeChange = { ws, s -> onUpdateSet(ws.copy(restTimeSeconds = s)) },
        activeTimerSetId = activeTimerSetId,
        timerEndAtEpochMs = timerEndAtEpochMs,
        onMoveUp = onMoveUp,
        onMoveDown = onMoveDown,
        previousSets = exercisePrevSets
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutTopAppBar(
    startTime: Long?,
    showOverflowMenu: Boolean,
    onBackClick: () -> Unit,
    onFinishClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onOverflowDismiss: () -> Unit,
    onCancelWorkoutClick: () -> Unit
) {
    val elapsedText = rememberElapsedTime(startTime = startTime)
    TopAppBar(
        title = { Text(elapsedText) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onFinishClick) {
                Icon(Icons.Default.Check, contentDescription = "Finish")
            }
            Box {
                IconButton(onClick = onOverflowClick) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = showOverflowMenu,
                    onDismissRequest = onOverflowDismiss
                ) {
                    DropdownMenuItem(
                        text = { Text("Cancel Workout") },
                        onClick = onCancelWorkoutClick
                    )
                }
            }
        }
    )
}

@Composable
private fun WorkoutSummaryCard(
    exerciseCount: Int,
    completedSets: Int,
    totalSets: Int
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$exerciseCount",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Exercises",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$completedSets/$totalSets",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Sets",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WorkoutDialogs(
    showFinishDialog: Boolean,
    showCancelDialog: Boolean,
    onFinishDismiss: () -> Unit,
    onCancelDismiss: () -> Unit,
    onFinishConfirm: () -> Unit,
    onCancelConfirm: () -> Unit
) {
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = onFinishDismiss,
            title = { Text("Finish Workout") },
            text = { Text("Are you sure you want to finish this workout?") },
            confirmButton = {
                TextButton(onClick = onFinishConfirm) {
                    Text("Finish")
                }
            },
            dismissButton = {
                TextButton(onClick = onFinishDismiss) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = onCancelDismiss,
            title = { Text("Cancel Workout") },
            text = { Text("Are you sure you want to cancel this workout? All data will be lost.") },
            confirmButton = {
                TextButton(onClick = onCancelConfirm) {
                    Text("Cancel Workout")
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelDismiss) {
                    Text("Keep Working Out")
                }
            }
        )
    }
}

private fun formatPrevSet(data: PreviousSetData): String {
    val kg = data.weightThousandths / WEIGHT_DIVISOR
    val weightStr = if (kg == kg.toLong().toDouble()) {
        kg.toLong().toString()
    } else {
        kg.toBigDecimal().stripTrailingZeros().toPlainString()
    }
    return "${weightStr}x${data.reps}"
}

private fun formatElapsedTime(millis: Long): String {
    val totalSeconds = millis / MILLIS_PER_SECOND
    val hours = totalSeconds / SECONDS_PER_HOUR
    val minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}
