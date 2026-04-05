package com.example.nocapfit.ui.screens.programs

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nocapfit.ui.components.ExercisePickerSheet
import com.example.nocapfit.ui.components.MmSsVisualTransformation
import com.example.nocapfit.ui.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramFormScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ProgramFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val availableExercises by viewModel.availableExercises.collectAsState()
    val scope = rememberCoroutineScope()
    var showExercisePicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            ProgramFormTopBar(
                isEditing = viewModel.isEditing,
                isSaving = uiState.isSaving,
                onBack = { navController.popBackStack() },
                onSave = {
                    scope.launch {
                        val success = viewModel.save()
                        if (success) {
                            navController.popBackStack()
                        }
                    }
                }
            )
        }
    ) { padding ->
        ProgramFormContent(
            uiState = uiState,
            onNameChange = viewModel::updateName,
            onMoveExercise = viewModel::moveExercise,
            onRemoveExercise = viewModel::removeExercise,
            onAddSet = viewModel::addSet,
            onRemoveSet = viewModel::removeSet,
            onUpdateSet = viewModel::updateSet,
            onSetRestTimeForAll = viewModel::setRestTimeForAll,
            onExerciseTitleClick = { exerciseId ->
                navController.navigate(Screen.ExerciseDetail.createRoute(exerciseId))
            },
            onShowExercisePicker = { showExercisePicker = true },
            modifier = Modifier.padding(padding)
        )

        if (showExercisePicker) {
            ExercisePickerSheet(
                exercises = availableExercises,
                onSelectExercise = { exercise ->
                    viewModel.addExercise(exercise)
                    showExercisePicker = false
                },
                onDismiss = { showExercisePicker = false }
            )
        }
    }
}

@Composable
internal fun ProgramFormContent(
    uiState: ProgramFormUiState,
    onNameChange: (String) -> Unit,
    onMoveExercise: (Int, Int) -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onAddSet: (Int) -> Unit,
    onRemoveSet: (Int, Int) -> Unit,
    onUpdateSet: (Int, Int, SetEntry) -> Unit,
    onSetRestTimeForAll: (Int, String) -> Unit,
    onExerciseTitleClick: (Long) -> Unit,
    onShowExercisePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(contentType = "name") {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    label = { Text("Program Name") },
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let { error ->
                        {
                            Text(error)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (uiState.exercises.isNotEmpty()) {
                item(contentType = "header") {
                    Text(
                        text = "Exercises",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
            }

            itemsIndexed(
                uiState.exercises,
                key = { _, entry -> entry.exercise.id },
                contentType = { _, _ -> "exercise" }
            ) { exerciseIndex, exerciseEntry ->
                ExerciseCardItem(
                    exerciseIndex = exerciseIndex,
                    exerciseEntry = exerciseEntry,
                    lastIndex = uiState.exercises.lastIndex,
                    exerciseCount = uiState.exercises.size,
                    onMoveExercise = onMoveExercise,
                    onRemoveExercise = onRemoveExercise,
                    onAddSet = onAddSet,
                    onRemoveSet = onRemoveSet,
                    onUpdateSet = onUpdateSet,
                    onSetRestTimeForAll = onSetRestTimeForAll,
                    onExerciseTitleClick = onExerciseTitleClick
                )
            }

            item(contentType = "controls") {
                Spacer(modifier = Modifier.height(4.dp))
                FilledTonalButton(
                    onClick = onShowExercisePicker,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Exercise")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProgramFormTopBar(
    isEditing: Boolean,
    isSaving: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    TopAppBar(
        title = {
            Text(if (isEditing) "Edit Program" else "New Program")
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            TextButton(
                onClick = onSave,
                enabled = !isSaving
            ) {
                Text("Save")
            }
        }
    )
}

@Composable
private fun ExerciseCardItem(
    exerciseIndex: Int,
    exerciseEntry: ExerciseEntry,
    lastIndex: Int,
    exerciseCount: Int,
    onMoveExercise: (Int, Int) -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onAddSet: (Int) -> Unit,
    onRemoveSet: (Int, Int) -> Unit,
    onUpdateSet: (Int, Int, SetEntry) -> Unit,
    onSetRestTimeForAll: (Int, String) -> Unit,
    onExerciseTitleClick: (Long) -> Unit
) {
    val onMoveUp = remember(exerciseIndex) {
        if (exerciseIndex > 0) {
            { onMoveExercise(exerciseIndex, exerciseIndex - 1) }
        } else {
            null
        }
    }
    val onMoveDown = remember(exerciseIndex, exerciseCount) {
        if (exerciseIndex < lastIndex) {
            { onMoveExercise(exerciseIndex, exerciseIndex + 1) }
        } else {
            null
        }
    }
    val onRemove = remember(exerciseIndex) { { onRemoveExercise(exerciseIndex) } }
    val onAdd = remember(exerciseIndex) { { onAddSet(exerciseIndex) } }
    val onRemoveSetCallback = remember(exerciseIndex) {
        {
                setIndex: Int ->
            onRemoveSet(exerciseIndex, setIndex)
        }
    }
    val onUpdateSetCallback = remember(exerciseIndex) {
        {
                setIndex: Int, setEntry: SetEntry ->
            onUpdateSet(exerciseIndex, setIndex, setEntry)
        }
    }
    val onSetRestTimeForAllCallback = remember(exerciseIndex) {
        {
                digits: String ->
            onSetRestTimeForAll(exerciseIndex, digits)
        }
    }
    ExerciseCard(
        exerciseEntry = exerciseEntry,
        onMoveUp = onMoveUp,
        onMoveDown = onMoveDown,
        onRemoveExercise = onRemove,
        onAddSet = onAdd,
        onRemoveSet = onRemoveSetCallback,
        onUpdateSet = onUpdateSetCallback,
        onSetRestTimeForAll = onSetRestTimeForAllCallback,
        onExerciseTitleClick = remember(exerciseEntry.exercise.id) {
            {
                onExerciseTitleClick(exerciseEntry.exercise.id)
            }
        }
    )
}

@Composable
private fun ExerciseCard(
    exerciseEntry: ExerciseEntry,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    onRemoveExercise: () -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onUpdateSet: (Int, SetEntry) -> Unit,
    onSetRestTimeForAll: (String) -> Unit,
    onExerciseTitleClick: () -> Unit
) {
    var showRestTimeDialog by remember { mutableStateOf(false) }
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exerciseEntry.exercise.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onExerciseTitleClick)
                )
                ExerciseOverflowMenu(
                    onMoveUp = onMoveUp,
                    onMoveDown = onMoveDown,
                    onSetRestTimeForAll = { showRestTimeDialog = true },
                    onRemoveExercise = onRemoveExercise
                )
            }

            exerciseEntry.sets.forEachIndexed { setIndex, setEntry ->
                if (setIndex > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
                val onUpdateCallback = remember(setIndex) {
                    {
                            updated: SetEntry ->
                        onUpdateSet(setIndex, updated)
                    }
                }
                val onRemoveCallback = remember(setIndex) {
                    {
                        onRemoveSet(setIndex)
                    }
                }
                SetRow(
                    setIndex = setIndex,
                    setEntry = setEntry,
                    showRemove = exerciseEntry.sets.size > 1,
                    onUpdate = onUpdateCallback,
                    onRemove = onRemoveCallback
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = onAddSet,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Set")
            }
        }
    }
    if (showRestTimeDialog) {
        RestTimeForAllDialog(
            onDismiss = { showRestTimeDialog = false },
            onConfirm = { digits ->
                onSetRestTimeForAll(digits)
                showRestTimeDialog = false
            }
        )
    }
}

@Composable
private fun SetRow(
    setIndex: Int,
    setEntry: SetEntry,
    showRemove: Boolean,
    onUpdate: (SetEntry) -> Unit,
    onRemove: () -> Unit
) {
    Column {
        Text(
            text = "Set ${setIndex + 1}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = setEntry.weight,
                onValueChange = { onUpdate(setEntry.copy(weight = it)) },
                label = { Text("kg") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = setEntry.reps,
                onValueChange = { onUpdate(setEntry.copy(reps = it)) },
                label = { Text("Reps") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            if (showRemove) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove set",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = setEntry.restTimeSeconds,
                onValueChange = { newValue ->
                    val filtered = newValue.filter { it.isDigit() }.take(4)
                    onUpdate(setEntry.copy(restTimeSeconds = filtered))
                },
                label = { Text("Rest") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = MmSsVisualTransformation(),
                modifier = Modifier.width(120.dp)
            )
        }
    }
}

@Composable
private fun ExerciseOverflowMenu(
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    onSetRestTimeForAll: () -> Unit,
    onRemoveExercise: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Exercise options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (onMoveUp != null) {
                DropdownMenuItem(
                    text = { Text("Move Up") },
                    onClick = {
                        expanded = false
                        onMoveUp()
                    }
                )
            }
            if (onMoveDown != null) {
                DropdownMenuItem(
                    text = { Text("Move Down") },
                    onClick = {
                        expanded = false
                        onMoveDown()
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Set Rest Time for All Sets") },
                onClick = {
                    expanded = false
                    onSetRestTimeForAll()
                }
            )
            DropdownMenuItem(
                text = { Text("Remove Exercise") },
                onClick = {
                    expanded = false
                    onRemoveExercise()
                }
            )
        }
    }
}

@Composable
private fun RestTimeForAllDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var restTimeDigits by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Rest Time for All Sets") },
        text = {
            OutlinedTextField(
                value = restTimeDigits,
                onValueChange = { newValue ->
                    restTimeDigits = newValue.filter { it.isDigit() }.take(4)
                },
                label = { Text("Rest Time") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = MmSsVisualTransformation()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(restTimeDigits) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
