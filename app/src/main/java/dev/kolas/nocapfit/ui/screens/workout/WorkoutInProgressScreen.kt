package dev.kolas.nocapfit.ui.screens.workout

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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import dev.kolas.nocapfit.service.TimerCoordinator
import dev.kolas.nocapfit.ui.components.ConfirmDialog
import dev.kolas.nocapfit.ui.components.ExerciseCard
import dev.kolas.nocapfit.ui.components.ExercisePickerSheet
import dev.kolas.nocapfit.ui.components.rememberTimerRemainingMs
import dev.kolas.nocapfit.ui.model.SetUiModel
import dev.kolas.nocapfit.ui.model.WorkoutExerciseRow
import dev.kolas.nocapfit.ui.navigation.Screen
import dev.kolas.nocapfit.util.MILLIS_PER_SECOND
import dev.kolas.nocapfit.util.SECONDS_PER_HOUR
import dev.kolas.nocapfit.util.SECONDS_PER_MINUTE
import dev.kolas.nocapfit.util.ceilSecondsFromMs
import dev.kolas.nocapfit.util.formatMmSs
import dev.kolas.nocapfit.util.restTimerFillProgress
import kotlinx.coroutines.delay

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutInProgressScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: WorkoutInProgressViewModel = hiltViewModel(),
    onMinimize: (() -> Unit)? = null
) {
    val workout by viewModel.workout.collectAsState()
    val exerciseRows by viewModel.exerciseRows.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val availableExercises by viewModel.availableExercises.collectAsState()
    var showFinishDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showExercisePicker by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    val onBack: () -> Unit = { onMinimize?.invoke() ?: navController.popBackStack() }
    BackHandler(onBack = onBack)
    KeepScreenOn()
    val lazyListState = rememberLazyListState()
    val showTopBarTimer = rememberShowTopBarTimer(timerState, exerciseRows, lazyListState)
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
            workoutLoaded = workout != null,
            rows = exerciseRows,
            timerState = timerState,
            lazyListState = lazyListState,
            onMoveExercise = viewModel::moveExercise,
            onRemoveExercise = viewModel::removeExercise,
            onAddSet = viewModel::addSet,
            onUpdateSet = viewModel::updateSet,
            onSetRestTimeForAll = viewModel::setRestTimeForAll,
            onUpdateNote = viewModel::updateExerciseNote,
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
    rows: List<WorkoutExerciseRow>,
    lazyListState: LazyListState
): Boolean {
    val activeTimerExerciseId: Long? = remember(timerState, rows) {
        val setId = (timerState as? TimerCoordinator.TimerUiState.Running)?.workoutSetId
            ?: return@remember null
        rows.firstOrNull { row -> setId in row.setsById }?.workoutExercise?.id
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

@Suppress("LongParameterList")
@Composable
private fun WorkoutContent(
    padding: PaddingValues,
    workoutLoaded: Boolean,
    rows: List<WorkoutExerciseRow>,
    timerState: TimerCoordinator.TimerUiState,
    lazyListState: LazyListState,
    onMoveExercise: (Long, Int) -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onAddSet: (Long) -> Unit,
    onUpdateSet: (dev.kolas.nocapfit.data.db.entity.WorkoutSet) -> Unit,
    onSetRestTimeForAll: (Long, Int) -> Unit,
    onUpdateNote: (Long, String?) -> Unit,
    onCompleteSet: (Long, Int) -> Unit,
    onRevertSet: (Long) -> Unit,
    onCancelTimer: () -> Unit,
    onExerciseTitleClick: (Long) -> Unit,
    onAddExerciseClick: () -> Unit,
    onFinishClick: () -> Unit
) {
    if (!workoutLoaded) {
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) { Text("Loading workout...") }
        return
    }

    WorkoutExerciseList(
        modifier = Modifier.fillMaxSize().padding(padding),
        rows = rows,
        timerState = timerState,
        lazyListState = lazyListState,
        onMoveExercise = onMoveExercise,
        onRemoveExercise = onRemoveExercise,
        onAddSet = onAddSet,
        onUpdateSet = onUpdateSet,
        onSetRestTimeForAll = onSetRestTimeForAll,
        onUpdateNote = onUpdateNote,
        onCompleteSet = onCompleteSet,
        onRevertSet = onRevertSet,
        onCancelTimer = onCancelTimer,
        onExerciseTitleClick = onExerciseTitleClick,
        onAddExerciseClick = onAddExerciseClick,
        onFinishClick = onFinishClick
    )
}

@Suppress("LongParameterList")
@Composable
private fun WorkoutExerciseList(
    rows: List<WorkoutExerciseRow>,
    timerState: TimerCoordinator.TimerUiState,
    lazyListState: LazyListState,
    onMoveExercise: (Long, Int) -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onAddSet: (Long) -> Unit,
    onUpdateSet: (dev.kolas.nocapfit.data.db.entity.WorkoutSet) -> Unit,
    onSetRestTimeForAll: (Long, Int) -> Unit,
    onUpdateNote: (Long, String?) -> Unit,
    onCompleteSet: (Long, Int) -> Unit,
    onRevertSet: (Long) -> Unit,
    onCancelTimer: () -> Unit,
    onExerciseTitleClick: (Long) -> Unit,
    onAddExerciseClick: () -> Unit,
    onFinishClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedSets = remember(rows) {
        rows.sumOf { row -> row.sets.count { it.completed } }
    }
    val totalSets = remember(rows) { rows.sumOf { it.sets.size } }

    val running = timerState as? TimerCoordinator.TimerUiState.Running
    val activeTimerSetId: Long? = running?.workoutSetId
    val timerEndAtEpochMs: Long = running?.endAtEpochMs ?: 0L
    val timerTotalMs: Long = running?.totalMs ?: 0L

    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        contentPadding = PaddingValues(
            top = 0.dp,
            bottom = 32.dp
        )
    ) {
        item(contentType = "summary") {
            WorkoutProgressStrip(
                exerciseCount = rows.size,
                completedSets = completedSets,
                totalSets = totalSets
            )
        }

        itemsIndexed(
            rows,
            key = { _, item -> item.workoutExercise.id },
            contentType = { _, _ -> "exercise" }
        ) { index, row ->
            ExerciseCardItem(
                row = row,
                index = index,
                lastIndex = rows.lastIndex,
                activeTimerSetId = activeTimerSetId,
                timerEndAtEpochMs = timerEndAtEpochMs,
                timerTotalMs = timerTotalMs,
                onMoveExercise = onMoveExercise,
                onRemoveExercise = onRemoveExercise,
                onAddSet = onAddSet,
                onUpdateSet = onUpdateSet,
                onSetRestTimeForAll = onSetRestTimeForAll,
                onUpdateNote = onUpdateNote,
                onCompleteSet = onCompleteSet,
                onRevertSet = onRevertSet,
                onCancelTimer = onCancelTimer,
                onExerciseTitleClick = onExerciseTitleClick
            )
        }

        item(contentType = "controls") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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

@Suppress("LongParameterList", "LongMethod")
@Composable
private fun ExerciseCardItem(
    row: WorkoutExerciseRow,
    index: Int,
    lastIndex: Int,
    activeTimerSetId: Long?,
    timerEndAtEpochMs: Long,
    timerTotalMs: Long,
    onMoveExercise: (Long, Int) -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onAddSet: (Long) -> Unit,
    onUpdateSet: (dev.kolas.nocapfit.data.db.entity.WorkoutSet) -> Unit,
    onSetRestTimeForAll: (Long, Int) -> Unit,
    onUpdateNote: (Long, String?) -> Unit,
    onCompleteSet: (Long, Int) -> Unit,
    onRevertSet: (Long) -> Unit,
    onCancelTimer: () -> Unit,
    onExerciseTitleClick: (Long) -> Unit
) {
    val id = row.workoutExercise.id
    val exId = row.workoutExercise.exerciseId
    val setsById = row.setsById
    val ownsActiveTimer = activeTimerSetId != null && activeTimerSetId in setsById

    val weightChange = remember<(SetUiModel, Int) -> Unit>(setsById, onUpdateSet) {
        {
                model, w ->
            setsById[model.id]?.let { onUpdateSet(it.copy(weightThousandths = w)) }
        }
    }
    val repsChange = remember<(SetUiModel, Int) -> Unit>(setsById, onUpdateSet) {
        {
                model, r ->
            setsById[model.id]?.let { onUpdateSet(it.copy(reps = r)) }
        }
    }
    val toggleComplete = remember<(SetUiModel) -> Unit>(setsById, onCompleteSet, onRevertSet) {
        {
                model ->
            setsById[model.id]?.let { ws ->
                if (ws.completed) onRevertSet(ws.id) else onCompleteSet(ws.id, ws.restTimeSeconds)
            }
        }
    }
    val restTimeChange = remember<(SetUiModel, Int) -> Unit>(setsById, onUpdateSet) {
        {
                model, s ->
            setsById[model.id]?.let { onUpdateSet(it.copy(restTimeSeconds = s)) }
        }
    }
    val addSet = remember(id, onAddSet) { { onAddSet(id) } }
    val removeExercise = remember(id, onRemoveExercise) { { onRemoveExercise(id) } }
    val setRestTimeForAll = remember<(Int) -> Unit>(id, onSetRestTimeForAll) {
        {
                seconds ->
            onSetRestTimeForAll(id, seconds)
        }
    }
    val updateNote = remember<(String?) -> Unit>(id, onUpdateNote) {
        {
                note ->
            onUpdateNote(id, note)
        }
    }
    val titleClick: (() -> Unit)? = if (exId != null) {
        remember(exId, onExerciseTitleClick) { { onExerciseTitleClick(exId) } }
    } else {
        null
    }

    val canMoveUp = index > 0
    val canMoveDown = index < lastIndex
    val moveUp = remember(canMoveUp, id, onMoveExercise) {
        if (canMoveUp) ({ onMoveExercise(id, -1) }) else null
    }
    val moveDown = remember(canMoveDown, id, onMoveExercise) {
        if (canMoveDown) ({ onMoveExercise(id, 1) }) else null
    }

    ExerciseCard(
        exerciseName = row.workoutExercise.exerciseName,
        sets = row.sets,
        onAddSet = addSet,
        onRemoveExercise = removeExercise,
        onWeightChange = weightChange,
        onRepsChange = repsChange,
        onToggleComplete = toggleComplete,
        onRestTimeChange = restTimeChange,
        onSetRestTimeForAll = setRestTimeForAll,
        activeTimerSetId = if (ownsActiveTimer) activeTimerSetId else null,
        timerEndAtEpochMs = if (ownsActiveTimer) timerEndAtEpochMs else 0L,
        timerTotalMs = if (ownsActiveTimer) timerTotalMs else 0L,
        onExerciseTitleClick = titleClick,
        onCancelTimer = onCancelTimer,
        onMoveUp = moveUp,
        onMoveDown = moveDown,
        showBottomDivider = canMoveDown,
        note = row.workoutExercise.note,
        onUpdateNote = updateNote
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
                trackColor = Color.Transparent,
                drawStopIndicator = {}
            )
        }
    }
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
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.height(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = formatMmSs(ceilSecondsFromMs(timerRemainingMs)),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun WorkoutProgressStrip(
    exerciseCount: Int,
    completedSets: Int,
    totalSets: Int
) {
    val progress = if (totalSets > 0) completedSets.toFloat() / totalSets else 0f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$exerciseCount EXERCISES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$completedSets / $totalSets sets",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainer,
            drawStopIndicator = {}
        )
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
        ConfirmDialog(
            title = "Finish Workout",
            message = "Are you sure you want to finish this workout?",
            confirmLabel = "Finish",
            onConfirm = onFinishConfirm,
            onDismiss = onFinishDismiss
        )
    }

    if (showCancelDialog) {
        ConfirmDialog(
            title = "Cancel Workout",
            message = "Are you sure you want to cancel this workout? All data will be lost.",
            confirmLabel = "Cancel Workout",
            dismissLabel = "Keep Working Out",
            onConfirm = onCancelConfirm,
            onDismiss = onCancelDismiss
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
