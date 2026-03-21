package com.example.nocapfit.ui.screens.workoutdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nocapfit.data.db.relation.WorkoutExerciseWithSets
import com.example.nocapfit.ui.util.formatDateTime
import com.example.nocapfit.ui.util.formatDuration
import com.example.nocapfit.util.MILLIS_PER_SECOND
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: WorkoutDetailViewModel = hiltViewModel()
) {
    val workoutWithExercises by viewModel.workoutWithExercises.collectAsState()
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
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        WorkoutDetailContent(
            data = data,
            modifier = Modifier.padding(padding)
        )
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = dateTimeText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            WorkoutSummaryRow(
                durationText = durationText,
                totalSets = totalSets,
                totalVolume = totalVolume
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(
            data.exercises.sortedBy { it.workoutExercise.orderIndex },
            key = { it.workoutExercise.id }
        ) { exerciseWithSets ->
            ExerciseDetailCard(exerciseWithSets)
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun WorkoutSummaryRow(
    durationText: String,
    totalSets: Int,
    totalVolume: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            label = "Duration",
            value = durationText,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Sets",
            value = totalSets.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Volume",
            value = "$totalVolume kg",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ExerciseDetailCard(exerciseWithSets: WorkoutExerciseWithSets) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
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
    val value = BigDecimal(weightThousandths).divide(BigDecimal(MILLIS_PER_SECOND))
    return value.stripTrailingZeros().toPlainString()
}
