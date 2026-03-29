package com.example.nocapfit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
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
    modifier: Modifier = Modifier,
    onRestTimeChange: ((WorkoutSet, Int) -> Unit)? = null,
    activeTimerSetId: Long? = null,
    timerEndAtEpochMs: Long = 0L
) {
    val accentColor = MaterialTheme.colorScheme.tertiaryContainer

    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .drawBehind {
                        drawRect(accentColor)
                    }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
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

                val sortedSets = remember(sets) { sets.sortedBy { it.setIndex } }
                sortedSets.forEachIndexed { index, workoutSet ->
                    SetRow(
                        setNumber = index + 1,
                        workoutSet = workoutSet,
                        onWeightChange = { newWeight -> onWeightChange(workoutSet, newWeight) },
                        onRepsChange = { newReps -> onRepsChange(workoutSet, newReps) },
                        onToggleComplete = { onToggleComplete(workoutSet) }
                    )
                    RestTimeRow(
                        restTimeSeconds = workoutSet.restTimeSeconds,
                        onRestTimeChange = if (onRestTimeChange != null) {
                            { newSeconds -> onRestTimeChange(workoutSet, newSeconds) }
                        } else {
                            null
                        },
                        isTimerActive = activeTimerSetId == workoutSet.id,
                        timerEndAtEpochMs = if (activeTimerSetId == workoutSet.id) timerEndAtEpochMs else 0L
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
}
