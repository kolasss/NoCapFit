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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutInProgressScreen(
    navController: NavController,
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

    // Back handler: minimize instead of popping
    BackHandler {
        if (onMinimize != null) {
            onMinimize(viewModel.workoutId)
        } else {
            navController.popBackStack()
        }
    }

    // Keep screen on
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Elapsed time
    var elapsedText by remember { mutableStateOf("00:00:00") }
    val startTime = workout?.workout?.startTime
    LaunchedEffect(startTime) {
        if (startTime == null) return@LaunchedEffect
        while (true) {
            val elapsed = System.currentTimeMillis() - startTime
            elapsedText = formatElapsedTime(elapsed)
            delay(1000)
        }
    }

    // Extract active timer set info
    val activeTimerSetId: Long? = (timerState as? TimerCoordinator.TimerUiState.Running)
        ?.workoutSetId
    val timerEndAtEpochMs: Long = (timerState as? TimerCoordinator.TimerUiState.Running)
        ?.endAtEpochMs ?: 0L

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(elapsedText) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (onMinimize != null) {
                            onMinimize(viewModel.workoutId)
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFinishDialog = true }) {
                        Icon(Icons.Default.Check, contentDescription = "Finish")
                    }
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Cancel Workout") },
                                onClick = {
                                    showOverflowMenu = false
                                    showCancelDialog = true
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        val workoutData = workout

        if (workoutData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading workout...")
            }
            return@Scaffold
        }

        val sortedExercises = workoutData.exercises.sortedBy { it.workoutExercise.orderIndex }
        val completedSets = sortedExercises.sumOf { ex -> ex.sets.count { it.completed } }
        val totalSets = sortedExercises.sumOf { it.sets.size }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${sortedExercises.size}",
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

                items(sortedExercises, key = { it.workoutExercise.id }) { exerciseWithSets ->
                    ExerciseCard(
                        exerciseName = exerciseWithSets.workoutExercise.exerciseName,
                        sets = exerciseWithSets.sets,
                        onRemoveExercise = {
                            viewModel.removeExercise(exerciseWithSets.workoutExercise.id)
                        },
                        onAddSet = {
                            viewModel.addSet(exerciseWithSets.workoutExercise.id)
                        },
                        onWeightChange = { workoutSet, newWeight ->
                            viewModel.updateSet(workoutSet.copy(weightThousandths = newWeight))
                        },
                        onRepsChange = { workoutSet, newReps ->
                            viewModel.updateSet(workoutSet.copy(reps = newReps))
                        },
                        onToggleComplete = { workoutSet ->
                            if (workoutSet.completed) {
                                viewModel.revertSet(workoutSet.id)
                            } else {
                                viewModel.completeSet(workoutSet.id, workoutSet.restTimeSeconds)
                            }
                        },
                        onRestTimeChange = { workoutSet, newSeconds ->
                            viewModel.updateSet(workoutSet.copy(restTimeSeconds = newSeconds))
                        },
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
                        TextButton(onClick = { showExercisePicker = true }) {
                            Text("+ Add Exercise")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showFinishDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Finish Workout")
                        }
                    }
                }
            }

            // Timer overlay at bottom
            val currentTimerState = timerState
            if (currentTimerState is TimerCoordinator.TimerUiState.Running) {
                val remainingMs = currentTimerState.endAtEpochMs - System.currentTimeMillis()
                if (remainingMs > 0) {
                    RestTimerOverlay(
                        remainingMs = remainingMs,
                        totalMs = currentTimerState.totalMs,
                        onCancel = { viewModel.cancelTimer() },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }

    // Finish workout dialog
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Finish Workout") },
            text = { Text("Are you sure you want to finish this workout?") },
            confirmButton = {
                TextButton(onClick = {
                    showFinishDialog = false
                    if (viewModel.finishWorkout()) {
                        navController.navigate(Screen.WorkoutHistory.route) {
                            popUpTo(Screen.WorkoutHistory.route) { inclusive = true }
                        }
                    }
                }) {
                    Text("Finish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Cancel workout dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Workout") },
            text = { Text("Are you sure you want to cancel this workout? All data will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    viewModel.cancelWorkout()
                    navController.navigate(Screen.WorkoutHistory.route) {
                        popUpTo(Screen.WorkoutHistory.route) { inclusive = true }
                    }
                }) {
                    Text("Cancel Workout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Working Out")
                }
            }
        )
    }

    // Exercise picker bottom sheet
    if (showExercisePicker) {
        ExercisePickerSheet(
            exercises = availableExercises,
            onExerciseSelected = { exercise ->
                viewModel.addExerciseFromDb(exercise.id, exercise.name)
                showExercisePicker = false
            },
            onDismiss = { showExercisePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisePickerSheet(
    exercises: List<Exercise>,
    onExerciseSelected: (Exercise) -> Unit,
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
                            modifier = Modifier.clickable { onExerciseSelected(exercise) }
                        )
                    }
                }
            }
        }
    }
}

private fun formatElapsedTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}
