package com.example.nocapfit.ui.screens.workouthistory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nocapfit.data.db.relation.WorkoutWithExercises
import com.example.nocapfit.ui.components.EmptyState
import com.example.nocapfit.ui.components.SubtleSectionHeader
import com.example.nocapfit.ui.navigation.Screen
import com.example.nocapfit.ui.util.formatDateTime
import com.example.nocapfit.ui.util.formatDuration
import com.example.nocapfit.ui.util.formatMonth
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: WorkoutHistoryViewModel = hiltViewModel()
) {
    val completedWorkouts by viewModel.completedWorkouts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasActiveWorkout by viewModel.hasActiveWorkout.collectAsState()

    val finished = completedWorkouts.filter { it.workout.endTime != null }
        .sortedByDescending { it.workout.startTime }

    val grouped = remember(finished) {
        finished.groupBy { formatMonth(it.workout.startTime) }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isScrolled by remember { derivedStateOf { scrollBehavior.state.collapsedFraction > 0.5f } }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
            if (!hasActiveWorkout) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Screen.AddWorkout.route) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("New Workout") },
                    expanded = !isScrolled
                )
            }
        }
    ) { padding ->
        WorkoutHistoryContent(
            isLoading = isLoading,
            finished = finished,
            grouped = grouped,
            onStartWorkout = { navController.navigate(Screen.AddWorkout.route) },
            onWorkoutClick = { workoutId ->
                navController.navigate(Screen.WorkoutDetail.createRoute(workoutId))
            },
            modifier = Modifier.padding(padding),
            hasActiveWorkout = hasActiveWorkout
        )
    }
}

@Composable
internal fun WorkoutHistoryContent(
    isLoading: Boolean,
    finished: List<WorkoutWithExercises>,
    grouped: Map<String, List<WorkoutWithExercises>>,
    onStartWorkout: () -> Unit,
    onWorkoutClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    hasActiveWorkout: Boolean = false
) {
    when {
        isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
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
                actionLabel = if (hasActiveWorkout) null else "Start Workout",
                onAction = if (hasActiveWorkout) null else onStartWorkout,
                modifier = modifier
            )
        }
        else -> {
            LazyColumn(modifier = modifier.fillMaxSize()) {
                grouped.forEach { (dateHeader, workouts) ->
                    item(key = "header-$dateHeader") {
                        SubtleSectionHeader(text = dateHeader.uppercase(Locale.ROOT))
                    }
                    items(workouts, key = { it.workout.id }) { workoutWithExercises ->
                        WorkoutHistoryItem(
                            workoutWithExercises = workoutWithExercises,
                            onClick = { onWorkoutClick(workoutWithExercises.workout.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
internal fun WorkoutHistoryItem(
    workoutWithExercises: WorkoutWithExercises,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val workout = workoutWithExercises.workout
    val dateTimeText = formatDateTime(workout.startTime)

    val durationMs = (workout.endTime ?: workout.startTime) - workout.startTime
    val durationText = formatDuration(durationMs)

    val sortedExercises = workoutWithExercises.exercises
        .sortedBy { it.workoutExercise.orderIndex }
    val exerciseNames = sortedExercises.take(3)
        .joinToString(", ") { it.workoutExercise.exerciseName }
    val moreCount = (sortedExercises.size - 3).coerceAtLeast(0)
    val exerciseSummary = exerciseNames + if (moreCount > 0) " +$moreCount more" else ""

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = workout.programName ?: "Free Workout",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = dateTimeText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (exerciseSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = exerciseSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
