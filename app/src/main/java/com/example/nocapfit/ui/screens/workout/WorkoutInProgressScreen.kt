package com.example.nocapfit.ui.screens.workout

import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.nocapfit.data.db.entity.Exercise
import com.example.nocapfit.service.TimerCoordinator
import com.example.nocapfit.ui.components.ExerciseCard
import com.example.nocapfit.ui.components.RestTimerOverlay
import com.example.nocapfit.ui.navigation.Screen
import com.example.nocapfit.util.MILLIS_PER_SECOND
import com.example.nocapfit.util.SECONDS_PER_HOUR
import com.example.nocapfit.util.SECONDS_PER_MINUTE
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
    val elapsedText = rememberElapsedTime(startTime = workout?.workout?.startTime)

    Scaffold(
        modifier = modifier,
        topBar = {
            WorkoutTopAppBar(
                elapsedText = elapsedText,
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
            elapsedText = elapsedText,
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
    elapsedText: String,
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

    val sortedExercises = workout.exercises.sortedBy { it.workoutExercise.orderIndex }
    val activeTimerSetId = (timerState as? TimerCoordinator.TimerUiState.Running)?.workoutSetId
    val timerEndAtEpochMs = (timerState as? TimerCoordinator.TimerUiState.Running)
        ?.endAtEpochMs ?: 0L

    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
        WorkoutExerciseList(
            sortedExercises = sortedExercises,
            timerState = timerState,
            elapsedText = elapsedText,
            activeTimerSetId = activeTimerSetId,
            timerEndAtEpochMs = timerEndAtEpochMs,
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
    elapsedText: String,
    activeTimerSetId: Long?,
    timerEndAtEpochMs: Long,
    onRemoveExercise: (Long) -> Unit,
    onAddSet: (Long) -> Unit,
    onUpdateSet: (com.example.nocapfit.data.db.entity.WorkoutSet) -> Unit,
    onCompleteSet: (Long, Int) -> Unit,
    onRevertSet: (Long) -> Unit,
    onAddExerciseClick: () -> Unit,
    onFinishClick: () -> Unit
) {
    val completedSets = sortedExercises.sumOf { ex -> ex.sets.count { it.completed } }
    val totalSets = sortedExercises.sumOf { it.sets.size }

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
        item {
            WorkoutSummaryCard(
                exerciseCount = sortedExercises.size,
                completedSets = completedSets,
                totalSets = totalSets,
                elapsedText = elapsedText
            )
        }

        items(sortedExercises, key = { it.workoutExercise.id }) { exerciseWithSets ->
            ExerciseCard(
                exerciseName = exerciseWithSets.workoutExercise.exerciseName,
                sets = exerciseWithSets.sets,
                onRemoveExercise = { onRemoveExercise(exerciseWithSets.workoutExercise.id) },
                onAddSet = { onAddSet(exerciseWithSets.workoutExercise.id) },
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
                modifier = Modifier.animateItem()
            )
        }

        item {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutTopAppBar(
    elapsedText: String,
    showOverflowMenu: Boolean,
    onBackClick: () -> Unit,
    onFinishClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onOverflowDismiss: () -> Unit,
    onCancelWorkoutClick: () -> Unit
) {
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
    totalSets: Int,
    elapsedText: String
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = elapsedText,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Elapsed",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisePickerSheet(
    exercises: List<Exercise>,
    onSelectExercise: (Exercise) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredExercises = remember(exercises, searchQuery) {
        if (searchQuery.isBlank()) {
            exercises
        } else {
            exercises.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Add Exercise",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search exercises") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (filteredExercises.isEmpty()) {
                Text(
                    text = if (exercises.isEmpty()) {
                        "No exercises available. Create exercises first."
                    } else {
                        "No matches found."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn {
                    items(filteredExercises, key = { it.id }) { exercise ->
                        ListItem(
                            headlineContent = { Text(exercise.name) },
                            supportingContent = if (exercise.description.isNotBlank()) {
                                { Text(exercise.description) }
                            } else {
                                null
                            },
                            modifier = Modifier.clickable { onSelectExercise(exercise) }
                        )
                    }
                }
            }
        }
    }
}

private fun formatElapsedTime(millis: Long): String {
    val totalSeconds = millis / MILLIS_PER_SECOND
    val hours = totalSeconds / SECONDS_PER_HOUR
    val minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}
