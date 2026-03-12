package com.example.nocapfit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nocapfit.data.db.entity.WorkoutSet

@Composable
fun ExerciseCard(
    exerciseName: String,
    sets: List<WorkoutSet>,
    onRemoveExercise: () -> Unit,
    onAddSet: () -> Unit,
    onWeightChange: (WorkoutSet, Int) -> Unit,
    onRepsChange: (WorkoutSet, Int) -> Unit,
    onToggleComplete: (WorkoutSet) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRemoveExercise) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove exercise")
                }
            }

            val sortedSets = sets.sortedBy { it.setIndex }
            sortedSets.forEachIndexed { index, workoutSet ->
                SetRow(
                    setNumber = index + 1,
                    workoutSet = workoutSet,
                    onWeightChange = { newWeight -> onWeightChange(workoutSet, newWeight) },
                    onRepsChange = { newReps -> onRepsChange(workoutSet, newReps) },
                    onToggleComplete = { onToggleComplete(workoutSet) }
                )
            }

            TextButton(
                onClick = onAddSet,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("+ Add Set")
            }
        }
    }
}
