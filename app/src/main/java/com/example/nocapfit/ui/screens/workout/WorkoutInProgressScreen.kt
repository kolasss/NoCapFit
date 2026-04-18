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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nocapfit.service.TimerCoordinator
import com.example.nocapfit.ui.components.ExerciseCard
import com.example.nocapfit.ui.components.ExercisePickerSheet
import com.example.nocapfit.ui.components.RestTimeForAllDialog
import com.example.nocapfit.ui.model.PreviousSetLookup
import com.example.nocapfit.ui.model.SetUiModel
import com.example.nocapfit.ui.model.formatPreviousSet
import com.example.nocapfit.ui.navigation.Screen
import com.example.nocapfit.util.MILLIS_PER_SECOND
import com.example.nocapfit.util.SECONDS_PER_HOUR
import com.example.nocapfit.util.SECONDS_PER_MINUTE
import com.example.nocapfit.util.ceilSecondsFromMs
import com.example.nocapfit.util.formatMmSs
import com.example.nocapfit.util.restTimerFillProgress
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

@Suppress("LongMethod")
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
    val onBack: () -> Unit = { onMinimize?.invoke(viewModel.workoutId) ?: navController.popBackStack() }
    BackHandler(onBack = onBack)
    KeepScreenOn()
    val lazyListState = rememberLazyListState()
    val showTopBarTimer = rememberShowTopBarTimer(timerState, workout, lazyListState)
    val runningState = timerState as? TimerCoordinator.TimerUiState.Running
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
                },
                showTimer = showTopBarTimer,
                timerEndAtEpochMs = runningState?.endAtEpochMs ?: 0L,
                timerTotalMs = runningState?.totalMs ?: 0L
            )
        }
    ) { padding ->
        WorkoutContent(
            padding = padding,
            workout = workout,
            timerStateFlow = viewModel.timerState,
            previousSets = previousSets,
            lazyListState = lazyListState,
            onMoveExercise = viewModel::moveExercise,
            onRemoveExercise = viewModel::removeExercise,
            onAddSet = viewModel::addSet,
            onUpdateSet = viewModel::updateSet,
            onSetRestTimeForAll = viewModel::setRestTimeForAll,
            onCompleteSet = viewModel::completeSet,
            onRevertSet = viewModel::revertSet,
            onCancelTimer = viewModel::cancelTimer,
            onExerciseTitleClick = { navController.navigate(Screen.ExerciseDetail.createRoute(it)) },
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
            if (viewModel.finishWorkout()) navController.navigateToWorkoutHistory()
        },
        onCancelConfirm = {
            showCancelDialog = false
            viewModel.cancelWorkout()
            navController.navigateToWorkoutHistory()
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
private fun rememberShowTopBarTimer(
    timerState: TimerCoordinator.TimerUiState,
    workout: com.example.nocapfit.data.db.relation.WorkoutWithExercises?,
    lazyListState: LazyListState
): Boolean {
    val activeTimerExerciseId: Long? = remember(timerState, workout) {
        val setId = (timerState as? TimerCoordinator.TimerUiState.Running)?.workoutSetId
            ?: return@remember null
        workout?.exercises?.firstOrNull { ex ->
            ex.sets.any { it.id == setId }
        }?.workoutExercise?.id
    }
    val isVisible by remember(activeTimerExerciseId) {
        derivedStateOf {
            activeTimerExerciseId != null &&
                lazyListState.layoutInfo.visibleItemsInfo.any {
                    it.key == activeTimerExerciseId
                }
        }
    }
    return timerState is TimerCoordinator.TimerUiState.Running && !isVisible
}

@Composable
private fun WorkoutContent(
    padding: PaddingValues,
    workout: com.example.nocapfit.data.db.relation.WorkoutWithExercises?,
    timerStateFlow: StateFlow<TimerCoordinator.TimerUiState>,
    previousSets: PreviousSetLookup,
    lazyListState: LazyListState,
    onMoveExercise: (Long, Int) -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onAddSet: (Long) -> Unit,
    onUpdateSet: (com.example.nocapfit.data.db.entity.WorkoutSet) -> Unit,
    onSetRestTimeForAll: (Long, Int) -> Unit,
    onCompleteSet: (Long, Int) -> Unit,
    onRevertSet: (Long) -> Unit,
    onCancelTimer: () -> Unit,
    onExerciseTitleClick: (Long) -> Unit,
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

    WorkoutExerciseList(
        modifier = Modifier.fillMaxSize().padding(padding),
        sortedExercises = sortedExercises,
        timerStateFlow = timerStateFlow,
        previousSets = previousSets,
        lazyListState = lazyListState,
        onMoveExercise = onMoveExercise,
        onRemoveExercise = onRemoveExercise,
        onAddSet = onAddSet,
        onUpdateSet = onUpdateSet,
        onSetRestTimeForAll = onSetRestTimeForAll,
        onCompleteSet = onCompleteSet,
        onRevertSet = onRevertSet,
        onCancelTimer = onCancelTimer,
        onExerciseTitleClick = onExerciseTitleClick,
        onAddExerciseClick = onAddExerciseClick,
        onFinishClick = onFinishClick
    )
}

@Composable
private fun WorkoutExerciseList(
    sortedExercises: List<com.example.nocapfit.data.db.relation.WorkoutExerciseWithSets>,
    timerStateFlow: StateFlow<TimerCoordinator.TimerUiState>,
    previousSets: PreviousSetLookup,
    lazyListState: LazyListState,
    onMoveExercise: (Long, Int) -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onAddSet: (Long) -> Unit,
    onUpdateSet: (com.example.nocapfit.data.db.entity.WorkoutSet) -> Unit,
    onSetRestTimeForAll: (Long, Int) -> Unit,
    onCompleteSet: (Long, Int) -> Unit,
    onRevertSet: (Long) -> Unit,
    onCancelTimer: () -> Unit,
    onExerciseTitleClick: (Long) -> Unit,
    onAddExerciseClick: () -> Unit,
    onFinishClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedSets = remember(sortedExercises) {
        sortedExercises.sumOf { ex -> ex.sets.count { it.completed } }
    }
    val totalSets = remember(sortedExercises) {
        sortedExercises.sumOf { it.sets.size }
    }

    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 80.dp
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
                timerStateFlow = timerStateFlow,
                onMoveExercise = onMoveExercise,
                onRemoveExercise = onRemoveExercise,
                onAddSet = onAddSet,
                onUpdateSet = onUpdateSet,
                onSetRestTimeForAll = onSetRestTimeForAll,
                onCompleteSet = onCompleteSet,
                onRevertSet = onRevertSet,
                onCancelTimer = onCancelTimer,
                onExerciseTitleClick = onExerciseTitleClick
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
    previousSets: PreviousSetLookup,
    timerStateFlow: StateFlow<TimerCoordinator.TimerUiState>,
    onMoveExercise: (Long, Int) -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onAddSet: (Long) -> Unit,
    onUpdateSet: (com.example.nocapfit.data.db.entity.WorkoutSet) -> Unit,
    onSetRestTimeForAll: (Long, Int) -> Unit,
    onCompleteSet: (Long, Int) -> Unit,
    onRevertSet: (Long) -> Unit,
    onCancelTimer: () -> Unit,
    onExerciseTitleClick: (Long) -> Unit
) {
    var showRestTimeDialog by remember { mutableStateOf(false) }
    val id = exerciseWithSets.workoutExercise.id
    val exId = exerciseWithSets.workoutExercise.exerciseId
    val sets = exerciseWithSets.sets
    val setsById = remember(sets) { sets.associateBy { it.id } }

    val timerUiState by timerStateFlow.collectAsState()
    val setIds = remember(sets) { sets.map { it.id }.toSet() }
    val activeTimer = remember(timerUiState, setIds) {
        (timerUiState as? TimerCoordinator.TimerUiState.Running)
            ?.takeIf { it.workoutSetId in setIds }
    }

    val setUiModels = remember(sets, exId, previousSets) {
        sets.map { ws ->
            val prevText = if (exId != null) {
                previousSets[exId to ws.setIndex]?.let { formatPreviousSet(it) }
            } else {
                null
            }
            SetUiModel(
                id = ws.id,
                setIndex = ws.setIndex,
                weightThousandths = ws.weightThousandths,
                reps = ws.reps,
                restTimeSeconds = ws.restTimeSeconds,
                completed = ws.completed,
                previousText = prevText
            )
        }
    }
    val onMoveUp = if (index > 0) { { onMoveExercise(id, -1) } } else null
    val onMoveDown = if (index < lastIndex) { { onMoveExercise(id, 1) } } else null
    ExerciseCard(
        exerciseName = exerciseWithSets.workoutExercise.exerciseName,
        sets = setUiModels,
        onAddSet = { onAddSet(id) },
        onRemoveExercise = { onRemoveExercise(id) },
        onWeightChange = { model, w ->
            setsById[model.id]?.let { onUpdateSet(it.copy(weightThousandths = w)) }
        },
        onRepsChange = { model, r ->
            setsById[model.id]?.let { onUpdateSet(it.copy(reps = r)) }
        },
        onToggleComplete = { model ->
            setsById[model.id]?.let { ws ->
                if (ws.completed) {
                    onRevertSet(ws.id)
                } else {
                    onCompleteSet(ws.id, ws.restTimeSeconds)
                }
            }
        },
        onRestTimeChange = { model, s ->
            setsById[model.id]?.let { onUpdateSet(it.copy(restTimeSeconds = s)) }
        },
        onSetRestTimeForAll = { showRestTimeDialog = true },
        activeTimerSetId = activeTimer?.workoutSetId,
        timerEndAtEpochMs = activeTimer?.endAtEpochMs ?: 0L,
        timerTotalMs = activeTimer?.totalMs ?: 0L,
        onExerciseTitleClick = exId?.let { { onExerciseTitleClick(it) } },
        onCancelTimer = onCancelTimer,
        onMoveUp = onMoveUp,
        onMoveDown = onMoveDown
    )
    if (showRestTimeDialog) {
        RestTimeForAllDialog(
            onDismiss = { showRestTimeDialog = false },
            onConfirm = { seconds ->
                onSetRestTimeForAll(id, seconds)
                showRestTimeDialog = false
            }
        )
    }
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
    onCancelWorkoutClick: () -> Unit,
    showTimer: Boolean = false,
    timerEndAtEpochMs: Long = 0L,
    timerTotalMs: Long = 0L
) {
    val elapsedText = rememberElapsedTime(startTime = startTime)
    val timerRemainingMs = rememberTimerRemainingMs(showTimer, timerEndAtEpochMs)
    val timerProgress = if (showTimer) {
        restTimerFillProgress(timerRemainingMs, timerTotalMs)
    } else {
        0f
    }

    Column {
        TopAppBar(
            title = {
                TopBarTitle(showTimer, elapsedText, timerRemainingMs)
            },
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
        if (showTimer) {
            LinearProgressIndicator(
                progress = { timerProgress },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                trackColor = Color.Transparent
            )
        }
    }
}

@Composable
private fun rememberTimerRemainingMs(showTimer: Boolean, timerEndAtEpochMs: Long): Long {
    var timerRemainingMs by remember(timerEndAtEpochMs) {
        mutableLongStateOf(
            if (showTimer && timerEndAtEpochMs > 0) {
                (timerEndAtEpochMs - System.currentTimeMillis()).coerceAtLeast(0)
            } else {
                0L
            }
        )
    }
    if (showTimer && timerEndAtEpochMs > 0) {
        LaunchedEffect(timerEndAtEpochMs) {
            while (true) {
                timerRemainingMs = (timerEndAtEpochMs - System.currentTimeMillis()).coerceAtLeast(0)
                if (timerRemainingMs <= 0) break
                delay(MILLIS_PER_SECOND)
            }
        }
    }
    return timerRemainingMs
}

@Composable
private fun TopBarTitle(showTimer: Boolean, elapsedText: String, timerRemainingMs: Long) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(elapsedText)
        if (showTimer) {
            Spacer(Modifier.width(12.dp))
            Icon(
                Icons.Default.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = formatMmSs(ceilSecondsFromMs(timerRemainingMs)),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
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

private fun NavController.navigateToWorkoutHistory() {
    navigate(Screen.WorkoutHistory.route) {
        popUpTo(Screen.WorkoutHistory.route) { inclusive = true }
    }
}

private fun formatElapsedTime(millis: Long): String {
    val totalSeconds = millis / MILLIS_PER_SECOND
    val hours = totalSeconds / SECONDS_PER_HOUR
    val minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}
