package com.example.nocapfit.ui.screens.workoutdetail

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nocapfit.data.db.relation.WorkoutExerciseWithSets
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    navController: NavController,
    viewModel: WorkoutDetailViewModel = hiltViewModel()
) {
    val workoutWithExercises by viewModel.workoutWithExercises.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val data = workoutWithExercises
        if (data == null) {
            Text(
                text = "Loading...",
                modifier = Modifier.padding(padding).padding(16.dp)
            )
            return@Scaffold
        }

        val workout = data.workout
        val localDateTime = Instant.fromEpochMilliseconds(workout.startTime)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val dateText = "${localDateTime.year}-${
            localDateTime.monthNumber.toString().padStart(2, '0')
        }-${localDateTime.dayOfMonth.toString().padStart(2, '0')} ${
            localDateTime.hour.toString().padStart(2, '0')
        }:${localDateTime.minute.toString().padStart(2, '0')}"

        val durationMs = (workout.endTime ?: workout.startTime) - workout.startTime
        val durationSeconds = durationMs / 1000
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        val seconds = durationSeconds % 60
        val durationText = "%02d:%02d:%02d".format(hours, minutes, seconds)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = workout.programName ?: "Free Workout",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Duration: $durationText",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(data.exercises.sortedBy { it.workoutExercise.orderIndex }) { exerciseWithSets ->
                ExerciseDetailCard(exerciseWithSets)
            }
        }
    }
}

@Composable
private fun ExerciseDetailCard(exerciseWithSets: WorkoutExerciseWithSets) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = exerciseWithSets.workoutExercise.exerciseName,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            val completedSets = exerciseWithSets.sets
                .filter { it.completed }
                .sortedBy { it.setIndex }

            if (completedSets.isEmpty()) {
                Text(
                    text = "No completed sets",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            completedSets.forEach { set ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Set ${set.setIndex + 1}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${formatWeight(set.weightThousandths)} kg x ${set.reps} reps",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private fun formatWeight(weightThousandths: Int): String {
    val value = BigDecimal(weightThousandths).divide(BigDecimal(1000))
    return value.stripTrailingZeros().toPlainString()
}
