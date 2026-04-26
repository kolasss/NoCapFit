package dev.kolas.nocapfit.ui.screens.programs

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import dev.kolas.nocapfit.ui.components.ExerciseCard
import dev.kolas.nocapfit.ui.components.ExercisePickerSheet
import dev.kolas.nocapfit.ui.components.RestTimeForAllDialog
import dev.kolas.nocapfit.ui.components.parseMmSsToSeconds
import dev.kolas.nocapfit.ui.components.secondsToMmSsDigits
import dev.kolas.nocapfit.ui.model.PreviousSetLookup
import dev.kolas.nocapfit.ui.model.SetUiModel
import dev.kolas.nocapfit.ui.model.formatPreviousSet
import dev.kolas.nocapfit.ui.navigation.Screen
import dev.kolas.nocapfit.ui.util.formatWeightInput
import dev.kolas.nocapfit.ui.util.parseWeight
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
    previousSets: PreviousSetLookup,
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
            modifier = modifier.fillMaxSize()
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (uiState.exercises.isNotEmpty()) {
                item(contentType = "header") {
                    Text(
                        text = "Exercises",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
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
                FilledTonalButton(
                    onClick = onShowExercisePicker,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
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
    previousSets: PreviousSetLookup,
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
                weightThousandths = parseWeight(setEntry.weight),
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
            onUpdateSet(exerciseIndex, model.setIndex, entry.copy(weight = formatWeightInput(w)))
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
        onMoveDown = onMoveDown,
        showBottomDivider = exerciseIndex < lastIndex
    )
    if (showRestTimeDialog) {
        RestTimeForAllDialog(
            onDismiss = { showRestTimeDialog = false },
            onConfirm = { seconds ->
                onSetRestTimeForAll(exerciseIndex, secondsToMmSsDigits(seconds))
                showRestTimeDialog = false
            }
        )
    }
}
