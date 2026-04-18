package com.example.nocapfit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nocapfit.ui.model.SetUiModel
import com.example.nocapfit.ui.util.formatWeightDisplay
import com.example.nocapfit.ui.util.parseWeight

@Composable
fun SetRow(
    setNumber: Int,
    set: SetUiModel,
    onWeightChange: (Int) -> Unit,
    onRepsChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    showComplete: Boolean = true,
    onToggleComplete: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null
) {
    var weightText by remember(set.id, set.weightThousandths) {
        mutableStateOf(if (set.weightThousandths == 0) "" else formatWeightDisplay(set.weightThousandths))
    }
    var repsText by remember(set.id, set.reps) {
        mutableStateOf(if (set.reps == 0) "" else set.reps.toString())
    }
    val completedColor = MaterialTheme.colorScheme.primaryContainer
    val defaultColor = MaterialTheme.colorScheme.surfaceContainerLow

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
    ) {
        SetRowContent(
            modifier = Modifier.drawBehind {
                drawRect(if (set.completed) completedColor else defaultColor)
            },
            setNumber = setNumber,
            previousText = set.previousText,
            weightText = weightText,
            repsText = repsText,
            completed = set.completed,
            showComplete = showComplete,
            onWeightTextChange = { newValue ->
                weightText = newValue
                if (newValue.isEmpty()) {
                    onWeightChange(0)
                } else if (newValue.toDoubleOrNull() != null) {
                    onWeightChange(parseWeight(newValue))
                }
            },
            onRepsTextChange = { newValue ->
                repsText = newValue
                val parsed = newValue.toIntOrNull()
                if (parsed != null) {
                    onRepsChange(parsed)
                } else if (newValue.isEmpty()) onRepsChange(0)
            },
            onToggleComplete = onToggleComplete,
            onRemove = onRemove
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
    showComplete: Boolean,
    onWeightTextChange: (String) -> Unit,
    onRepsTextChange: (String) -> Unit,
    onToggleComplete: (() -> Unit)?,
    onRemove: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    Row(
        modifier = modifier
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
        if (showComplete && onToggleComplete != null) {
            FilledIconToggleButton(
                checked = completed,
                onCheckedChange = {
                    focusManager.clearFocus()
                    onToggleComplete()
                }
            ) {
                Icon(Icons.Default.Check, contentDescription = "Complete set")
            }
        } else if (onRemove != null) {
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove set",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}
