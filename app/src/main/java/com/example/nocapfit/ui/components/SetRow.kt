package com.example.nocapfit.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nocapfit.data.db.entity.WorkoutSet

@Composable
fun SetRow(
    setNumber: Int,
    workoutSet: WorkoutSet,
    onWeightChange: (Int) -> Unit,
    onRepsChange: (Int) -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weightKg = workoutSet.weightThousandths / 1000.0
    var weightText by remember(workoutSet.id, workoutSet.weightThousandths) {
        mutableStateOf(
            if (weightKg == 0.0) "" else formatWeight(weightKg)
        )
    }
    var repsText by remember(workoutSet.id, workoutSet.reps) {
        mutableStateOf(
            if (workoutSet.reps == 0) "" else workoutSet.reps.toString()
        )
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (workoutSet.completed) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        label = "set-row-bg"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$setNumber",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(24.dp)
            )

            OutlinedTextField(
                value = weightText,
                onValueChange = { newValue ->
                    weightText = newValue
                    val parsed = newValue.toDoubleOrNull()
                    if (parsed != null) {
                        onWeightChange((parsed * 1000).toInt())
                    } else if (newValue.isEmpty()) {
                        onWeightChange(0)
                    }
                },
                label = { Text("kg") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = repsText,
                onValueChange = { newValue ->
                    repsText = newValue
                    val parsed = newValue.toIntOrNull()
                    if (parsed != null) {
                        onRepsChange(parsed)
                    } else if (newValue.isEmpty()) {
                        onRepsChange(0)
                    }
                },
                label = { Text("reps") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            FilledIconToggleButton(
                checked = workoutSet.completed,
                onCheckedChange = { onToggleComplete() }
            ) {
                Icon(Icons.Default.Check, contentDescription = "Complete set")
            }
        }
    }
}

private fun formatWeight(kg: Double): String {
    return if (kg == kg.toLong().toDouble()) {
        kg.toLong().toString()
    } else {
        kg.toBigDecimal().stripTrailingZeros().toPlainString()
    }
}
