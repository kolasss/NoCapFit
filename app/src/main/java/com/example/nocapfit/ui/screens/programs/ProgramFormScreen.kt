package com.example.nocapfit.ui.screens.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.nocapfit.ui.components.ExerciseCard
import com.example.nocapfit.ui.components.ExercisePickerSheet
import com.example.nocapfit.ui.components.MmSsVisualTransformation
import com.example.nocapfit.ui.components.parseMmSsToSeconds
import com.example.nocapfit.ui.components.secondsToMmSsDigits
import com.example.nocapfit.ui.model.PreviousSetData
import com.example.nocapfit.ui.model.SetUiModel
import com.example.nocapfit.ui.model.formatPreviousSet
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
    val previousSets by viewModel.previousSets.collectAsState()
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
            previousSets = previousSets,
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
    previousSets: Map<Pair<Long, Int>, PreviousSetData>,
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
                    previousSets = previousSets,
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
    previousSets: Map<Pair<Long, Int>, PreviousSetData>,
    onMoveExercise: (Int, Int) -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onAddSet: (Int) -> Unit,
    onRemoveSet: (Int, Int) -> Unit,
    onUpdateSet: (Int, Int, SetEntry) -> Unit,
    onSetRestTimeForAll: (Int, String) -> Unit,
    onExerciseTitleClick: (Long) -> Unit
) {
    var showRestTimeDialog by remember { mutableStateOf(false) }
    val exId = exerciseEntry.exercise.id
    val setUiModels = remember(exerciseEntry.sets, exId, previousSets) {
        exerciseEntry.sets.mapIndexed { setIndex, setEntry ->
            SetUiModel(
                id = setIndex.toLong(),
                setIndex = setIndex,
                weightThousandths = ProgramFormViewModel.parseWeight(setEntry.weight),
                reps = setEntry.reps.toIntOrNull() ?: 0,
                restTimeSeconds = parseMmSsToSeconds(setEntry.restTimeSeconds),
                completed = false,
                previousText = previousSets[exId to setIndex]?.let { formatPreviousSet(it) }
            )
        }
    }
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
    ExerciseCard(
        exerciseName = exerciseEntry.exercise.name,
        sets = setUiModels,
        onAddSet = { onAddSet(exerciseIndex) },
        onRemoveExercise = { onRemoveExercise(exerciseIndex) },
        onWeightChange = { model, w ->
            val entry = exerciseEntry.sets[model.setIndex]
            onUpdateSet(exerciseIndex, model.setIndex, entry.copy(weight = ProgramFormViewModel.formatWeight(w)))
        },
        onRepsChange = { model, r ->
            val entry = exerciseEntry.sets[model.setIndex]
            onUpdateSet(exerciseIndex, model.setIndex, entry.copy(reps = if (r == 0) "" else r.toString()))
        },
        onRestTimeChange = { model, s ->
            val entry = exerciseEntry.sets[model.setIndex]
            onUpdateSet(exerciseIndex, model.setIndex, entry.copy(restTimeSeconds = secondsToMmSsDigits(s)))
        },
        onRemoveSet = { model -> onRemoveSet(exerciseIndex, model.setIndex) },
        onSetRestTimeForAll = { showRestTimeDialog = true },
        onExerciseTitleClick = { onExerciseTitleClick(exId) },
        showComplete = false,
        showAddSetButton = true,
        onMoveUp = onMoveUp,
        onMoveDown = onMoveDown
    )
    if (showRestTimeDialog) {
        RestTimeForAllDialog(
            onDismiss = { showRestTimeDialog = false },
            onConfirm = { digits ->
                onSetRestTimeForAll(exerciseIndex, digits)
                showRestTimeDialog = false
            }
        )
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
