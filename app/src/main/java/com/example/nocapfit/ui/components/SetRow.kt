package com.example.nocapfit.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.nocapfit.util.WEIGHT_DIVISOR
import com.example.nocapfit.util.WEIGHT_MULTIPLIER

@Composable
fun SetRow(
    setNumber: Int,
    workoutSet: WorkoutSet,
    onWeightChange: (Int) -> Unit,
    onRepsChange: (Int) -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier,
    previousText: String? = null
) {
    var weightText by remember(workoutSet.id, workoutSet.weightThousandths) {
        val kg = workoutSet.weightThousandths / WEIGHT_DIVISOR
        mutableStateOf(if (kg == 0.0) "" else formatWeight(kg))
    }
    var repsText by remember(workoutSet.id, workoutSet.reps) {
        mutableStateOf(if (workoutSet.reps == 0) "" else workoutSet.reps.toString())
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
        SetRowContent(
            setNumber = setNumber,
            previousText = previousText,
            weightText = weightText,
            repsText = repsText,
            completed = workoutSet.completed,
            onWeightTextChange = { newValue ->
                weightText = newValue
                val parsed = newValue.toDoubleOrNull()
                if (parsed != null) {
                    onWeightChange((parsed * WEIGHT_MULTIPLIER).toInt())
                } else if (newValue.isEmpty()) onWeightChange(0)
            },
            onRepsTextChange = { newValue ->
                repsText = newValue
                val parsed = newValue.toIntOrNull()
                if (parsed != null) {
                    onRepsChange(parsed)
                } else if (newValue.isEmpty()) onRepsChange(0)
            },
            onToggleComplete = onToggleComplete
        )
    }
}

@Composable
private fun SetRowContent(
    setNumber: Int,
    previousText: String?,
    weightText: String,
    repsText: String,
    completed: Boolean,
    onWeightTextChange: (String) -> Unit,
    onRepsTextChange: (String) -> Unit,
    onToggleComplete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$setNumber",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(20.dp)
        )
        Text(
            text = previousText ?: "-",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        CompactInput(
            value = weightText,
            onValueChange = onWeightTextChange,
            keyboardType = KeyboardType.Decimal,
            modifier = Modifier.width(56.dp)
        )
        CompactInput(
            value = repsText,
            onValueChange = onRepsTextChange,
            keyboardType = KeyboardType.Number,
            modifier = Modifier.padding(start = 4.dp).width(56.dp)
        )
        FilledIconToggleButton(
            checked = completed,
            onCheckedChange = { onToggleComplete() }
        ) {
            Icon(Icons.Default.Check, contentDescription = "Complete set")
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
