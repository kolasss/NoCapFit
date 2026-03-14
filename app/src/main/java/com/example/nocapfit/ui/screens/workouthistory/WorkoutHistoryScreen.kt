package com.example.nocapfit.ui.screens.workouthistory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nocapfit.data.db.relation.WorkoutWithExercises
import com.example.nocapfit.ui.components.EmptyState
import com.example.nocapfit.ui.navigation.Screen
import com.example.nocapfit.ui.util.formatDate
import com.example.nocapfit.ui.util.formatDateTime
import com.example.nocapfit.ui.util.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    navController: NavController,
    viewModel: WorkoutHistoryViewModel = hiltViewModel()
) {
    val completedWorkouts by viewModel.completedWorkouts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val finished = completedWorkouts.filter { it.workout.endTime != null }
        .sortedByDescending { it.workout.startTime }

    val grouped = remember(finished) {
        finished.groupBy { formatDate(it.workout.startTime) }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isScrolled by remember { derivedStateOf { scrollBehavior.state.collapsedFraction > 0.5f } }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("History") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Settings.route)
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.AddWorkout.route) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Workout") },
                expanded = !isScrolled
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            finished.isEmpty() -> {
                EmptyState(
                    icon = Icons.Default.History,
                    title = "No workouts yet",
                    subtitle = "Start your first workout to see it here",
                    actionLabel = "Start Workout",
                    onAction = { navController.navigate(Screen.AddWorkout.route) },
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grouped.forEach { (dateHeader, workouts) ->
                        item(key = "header-$dateHeader") {
                            Text(
                                text = dateHeader,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                            )
                        }
                        items(workouts, key = { it.workout.id }) { workoutWithExercises ->
                            WorkoutHistoryItem(
                                workoutWithExercises = workoutWithExercises,
                                onClick = {
                                    navController.navigate(
                                        Screen.WorkoutDetail.createRoute(workoutWithExercises.workout.id)
                                    )
                                },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryItem(
    workoutWithExercises: WorkoutWithExercises,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val workout = workoutWithExercises.workout
    val dateTimeText = formatDateTime(workout.startTime)

    val durationMs = (workout.endTime ?: workout.startTime) - workout.startTime
    val durationText = formatDuration(durationMs)

    val exerciseNames = workoutWithExercises.exercises
        .sortedBy { it.workoutExercise.orderIndex }
        .take(3)
        .joinToString(", ") { it.workoutExercise.exerciseName }
    val moreCount = (workoutWithExercises.exercises.size - 3).coerceAtLeast(0)

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Top),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.programName ?: "Free Workout",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateTimeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (exerciseNames.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = exerciseNames + if (moreCount > 0) " +$moreCount more" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text(durationText, style = MaterialTheme.typography.labelSmall) }
                    )
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                "${workoutWithExercises.exercises.size} exercises",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        }
    }
}
