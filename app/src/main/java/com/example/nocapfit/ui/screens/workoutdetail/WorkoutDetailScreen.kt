package com.example.nocapfit.ui.screens.workoutdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nocapfit.data.db.relation.WorkoutExerciseWithSets
import com.example.nocapfit.ui.components.ConfirmDialog
import com.example.nocapfit.ui.components.InputDialog
import com.example.nocapfit.ui.navigation.Screen
import com.example.nocapfit.ui.util.formatDateTime
import com.example.nocapfit.ui.util.formatDuration
import com.example.nocapfit.ui.util.formatWeightDisplay
import com.example.nocapfit.util.MILLIS_PER_SECOND

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: WorkoutDetailViewModel = hiltViewModel()
) {
    val workoutWithExercises by viewModel.workoutWithExercises.collectAsState()
    val showDeleteConfirmation by viewModel.showDeleteConfirmation.collectAsState()
    val showSaveAsProgram by viewModel.showSaveAsProgram.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val data = workoutWithExercises

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(data?.workout?.programName ?: "Workout Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (data != null) {
                        WorkoutDetailMenu(
                            onEdit = {
                                navController.navigate(
                                    Screen.WorkoutEdit.createRoute(data.workout.id)
                                )
                            },
                            onSaveAsProgram = { viewModel.showSaveAsProgram() },
                            onDelete = { viewModel.showDeleteConfirmation() }
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        WorkoutDetailContent(
            data = data,
            modifier = Modifier.padding(padding)
        )
    }

    if (showDeleteConfirmation) {
        ConfirmDialog(
            title = "Delete Workout",
            message = "Are you sure you want to delete this workout? This action cannot be undone.",
            onConfirm = { viewModel.deleteWorkout { navController.popBackStack() } },
            onDismiss = { viewModel.dismissDeleteConfirmation() }
        )
    }

    if (showSaveAsProgram) {
        InputDialog(
            title = "Save as Program",
            initialValue = data?.workout?.programName ?: "",
            label = "Program name",
            onConfirm = { viewModel.saveAsProgram(it) },
            onDismiss = { viewModel.dismissSaveAsProgram() }
        )
    }
}

@Composable
private fun WorkoutDetailMenu(
    onEdit: () -> Unit,
    onSaveAsProgram: () -> Unit,
    onDelete: () -> Unit
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
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    showMenu = false
                    onEdit()
                }
            )
            DropdownMenuItem(
                text = { Text("Save as Program") },
                onClick = {
                    showMenu = false
                    onSaveAsProgram()
                }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    showMenu = false
                    onDelete()
                }
            )
        }
    }
}

@Composable
private fun WorkoutDetailContent(
    data: com.example.nocapfit.data.db.relation.WorkoutWithExercises?,
    modifier: Modifier = Modifier
) {
    if (data == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val workout = data.workout
    val dateTimeText = formatDateTime(workout.startTime)
    val durationMs = (workout.endTime ?: workout.startTime) - workout.startTime
    val durationText = formatDuration(durationMs)

    val totalSets = data.exercises.sumOf { ex ->
        ex.sets.count { it.completed }
    }
    val totalVolume = data.exercises.sumOf { ex ->
        ex.sets.filter { it.completed }.sumOf { set ->
            (set.weightThousandths.toLong() * set.reps) / MILLIS_PER_SECOND
        }
    }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            WorkoutSummarySection(
                dateTimeText = dateTimeText,
                durationText = durationText,
                totalSets = totalSets,
                totalVolume = totalVolume
            )
        }

        items(
            data.exercises.sortedBy { it.workoutExercise.orderIndex },
            key = { it.workoutExercise.id }
        ) { exerciseWithSets ->
            ExerciseDetailItem(exerciseWithSets)
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun WorkoutSummarySection(
    dateTimeText: String,
    durationText: String,
    totalSets: Int,
    totalVolume: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = dateTimeText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryStat(
                label = "DURATION",
                value = durationText,
                modifier = Modifier.weight(1f)
            )
            SummaryStat(
                label = "SETS",
                value = totalSets.toString(),
                modifier = Modifier.weight(1f)
            )
            SummaryStat(
                label = "VOLUME",
                value = "$totalVolume kg",
                modifier = Modifier.weight(1f)
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun SummaryStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun ExerciseDetailItem(
    exerciseWithSets: WorkoutExerciseWithSets,
    modifier: Modifier = Modifier
) {
    val completedSets = exerciseWithSets.sets
        .filter { it.completed }
        .sortedBy { it.setIndex }

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = exerciseWithSets.workoutExercise.exerciseName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            if (completedSets.isEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No completed sets",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    completedSets.forEach { set ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${set.setIndex + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(24.dp)
                            )
                            Text(
                                text = "${formatWeightDisplay(set.weightThousandths)} kg",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(80.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "× ${set.reps}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
