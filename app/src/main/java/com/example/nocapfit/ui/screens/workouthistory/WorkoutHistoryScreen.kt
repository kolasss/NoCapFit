package com.example.nocapfit.ui.screens.workouthistory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nocapfit.data.db.relation.WorkoutWithExercises
import com.example.nocapfit.ui.navigation.Screen
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    navController: NavController,
    viewModel: WorkoutHistoryViewModel = hiltViewModel()
) {
    val completedWorkouts by viewModel.completedWorkouts.collectAsState()

    val finished = completedWorkouts.filter { it.workout.endTime != null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Settings.route)
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.AddWorkout.route)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Workout")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (finished.isEmpty()) {
                item {
                    Text(
                        text = "No completed workouts yet",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 32.dp)
                    )
                }
            }

            items(finished) { workoutWithExercises ->
                WorkoutHistoryItem(
                    workoutWithExercises = workoutWithExercises,
                    onClick = {
                        navController.navigate(
                            Screen.WorkoutDetail.createRoute(workoutWithExercises.workout.id)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun WorkoutHistoryItem(
    workoutWithExercises: WorkoutWithExercises,
    onClick: () -> Unit
) {
    val workout = workoutWithExercises.workout
    val localDateTime = Instant.fromEpochMilliseconds(workout.startTime)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val dateText = "${localDateTime.year}-${
        localDateTime.month.toString().padStart(2, '0')
    }-${localDateTime.day.toString().padStart(2, '0')} ${
        localDateTime.hour.toString().padStart(2, '0')
    }:${localDateTime.minute.toString().padStart(2, '0')}"

    val durationMs = (workout.endTime ?: workout.startTime) - workout.startTime
    val durationSeconds = durationMs / 1000
    val hours = durationSeconds / 3600
    val minutes = (durationSeconds % 3600) / 60
    val seconds = durationSeconds % 60
    val durationText = "%02d:%02d:%02d".format(hours, minutes, seconds)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = workout.programName ?: "Free Workout",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Duration: $durationText",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${workoutWithExercises.exercises.size} exercises",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
