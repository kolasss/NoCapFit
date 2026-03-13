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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.nocapfit.ui.components.MmSsVisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nocapfit.data.db.entity.Exercise
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramFormScreen(
    navController: NavController,
    viewModel: ProgramFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val availableExercises by viewModel.availableExercises.collectAsState()
    val scope = rememberCoroutineScope()
    var showExercisePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (viewModel.isEditing) "Edit Program" else "New Program")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val success = viewModel.save()
                                if (success) {
                                    navController.popBackStack()
                                }
                            }
                        },
                        enabled = !uiState.isSaving
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::updateName,
                        label = { Text("Program Name") },
                        isError = uiState.nameError != null,
                        supportingText = uiState.nameError?.let { error ->
                            { Text(error) }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                itemsIndexed(uiState.exercises) { exerciseIndex, exerciseEntry ->
                    ExerciseCard(
                        exerciseEntry = exerciseEntry,
                        onRemoveExercise = { viewModel.removeExercise(exerciseIndex) },
                        onAddSet = { viewModel.addSet(exerciseIndex) },
                        onRemoveSet = { setIndex -> viewModel.removeSet(exerciseIndex, setIndex) },
                        onUpdateSet = { setIndex, setEntry ->
                            viewModel.updateSet(exerciseIndex, setIndex, setEntry)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    FilledTonalButton(
                        onClick = { showExercisePicker = true },
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

        if (showExercisePicker) {
            ExercisePickerBottomSheet(
                exercises = availableExercises,
                onExerciseSelected = { exercise ->
                    viewModel.addExercise(exercise)
                    showExercisePicker = false
                },
                onDismiss = { showExercisePicker = false }
            )
        }
    }
}

@Composable
private fun ExerciseCard(
    exerciseEntry: ExerciseEntry,
    onRemoveExercise: () -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onUpdateSet: (Int, SetEntry) -> Unit
) {
    Card(
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
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRemoveExercise) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove exercise",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            exerciseEntry.sets.forEachIndexed { setIndex, setEntry ->
                if (setIndex > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
                SetRow(
                    setIndex = setIndex,
                    setEntry = setEntry,
                    showRemove = exerciseEntry.sets.size > 1,
                    onUpdate = { updated -> onUpdateSet(setIndex, updated) },
                    onRemove = { onRemoveSet(setIndex) }
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisePickerBottomSheet(
    exercises: List<Exercise>,
    onExerciseSelected: (Exercise) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Select Exercise",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (exercises.isEmpty()) {
                Text(
                    text = "No exercises available. Create exercises first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn {
                    items(exercises.size) { index ->
                        val exercise = exercises[index]
                        ListItem(
                            headlineContent = { Text(exercise.name) },
                            supportingContent = if (exercise.description.isNotBlank()) {
                                { Text(exercise.description) }
                            } else null,
                            modifier = Modifier.clickable { onExerciseSelected(exercise) }
                        )
                    }
                }
            }
        }
    }
}
