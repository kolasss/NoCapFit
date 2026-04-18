package com.example.nocapfit.ui.screens.addworkout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nocapfit.data.db.relation.ProgramWithExercises
import com.example.nocapfit.ui.components.ConfirmDialog
import com.example.nocapfit.ui.navigation.Screen
import com.example.nocapfit.ui.util.formatRelativeDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AddWorkoutViewModel = hiltViewModel()
) {
    val programs by viewModel.programs.collectAsState()
    val lastWorkoutTimes by viewModel.lastWorkoutTimes.collectAsState()
    val profileLoaded by viewModel.profileLoaded.collectAsState()
    val scope = rememberCoroutineScope()
    var showActiveWorkoutDialog by remember { mutableStateOf(false) }
    var activeWorkoutId by remember { mutableLongStateOf(0L) }

    if (showActiveWorkoutDialog) {
        ActiveWorkoutDialog(
            onResume = {
                showActiveWorkoutDialog = false
                navController.navigate(Screen.WorkoutInProgress.createRoute(activeWorkoutId))
            },
            onDismiss = { showActiveWorkoutDialog = false }
        )
    }

    AddWorkoutContent(
        programs = programs,
        lastWorkoutTimes = lastWorkoutTimes,
        onBack = { navController.popBackStack() },
        onStartWorkout = { createWorkout ->
            if (!profileLoaded) return@AddWorkoutContent
            scope.launch {
                val existing = viewModel.getActiveWorkoutId()
                if (existing != null) {
                    activeWorkoutId = existing
                    showActiveWorkoutDialog = true
                    return@launch
                }
                val workoutId = createWorkout()
                navController.navigate(Screen.WorkoutInProgress.createRoute(workoutId))
            }
        },
        onCreateEmpty = { viewModel.createEmptyWorkout() },
        onCreateFromProgram = { viewModel.createWorkoutFromProgram(it) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWorkoutContent(
    programs: List<ProgramWithExercises>,
    lastWorkoutTimes: Map<Long, Long>,
    onBack: () -> Unit,
    onStartWorkout: (suspend () -> Long) -> Unit,
    onCreateEmpty: suspend () -> Long,
    onCreateFromProgram: suspend (Long) -> Long,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Start Workout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Quick Start",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                QuickStartCard(onClick = { onStartWorkout(onCreateEmpty) })
            }

            if (programs.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "From a Program",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(programs) { programWithExercises ->
                    ProgramCard(
                        programWithExercises = programWithExercises,
                        lastWorkoutTime = lastWorkoutTimes[programWithExercises.program.id],
                        onClick = {
                            onStartWorkout { onCreateFromProgram(programWithExercises.program.id) }
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun QuickStartCard(onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Empty Workout",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Start from scratch",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActiveWorkoutDialog(onResume: () -> Unit, onDismiss: () -> Unit) {
    ConfirmDialog(
        title = "Active Workout",
        message = "You already have a workout in progress. Resume it or cancel it first.",
        confirmLabel = "Resume",
        onConfirm = onResume,
        onDismiss = onDismiss
    )
}

@Composable
private fun ProgramCard(
    programWithExercises: ProgramWithExercises,
    lastWorkoutTime: Long?,
    onClick: () -> Unit
) {
    val exerciseNames = programWithExercises.exercises
        .sortedBy { it.programExercise.orderIndex }
        .take(3)
        .joinToString(", ") { it.exercise.name }
    val moreCount = (programWithExercises.exercises.size - 3).coerceAtLeast(0)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = programWithExercises.program.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (exerciseNames.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = exerciseNames + if (moreCount > 0) " +$moreCount more" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (lastWorkoutTime != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatRelativeDate(lastWorkoutTime),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
