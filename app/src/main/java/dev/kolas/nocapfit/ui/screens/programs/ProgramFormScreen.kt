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
import dev.kolas.nocapfit.ui.components.secondsToMmSsDigits
import dev.kolas.nocapfit.ui.model.ProgramExerciseRow
import dev.kolas.nocapfit.ui.model.SetUiModel
import dev.kolas.nocapfit.ui.navigation.Screen
import dev.kolas.nocapfit.ui.util.formatWeightInput
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramFormScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ProgramFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val rows by viewModel.exerciseRows.collectAsState()
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
            rows = rows,
            onNameChange = viewModel::updateName,
            onMoveExercise = viewModel::moveExercise,
            onRemoveExercise = viewModel::removeExercise,
            onAddSet = viewModel::addSet,
            onRemoveSet = viewModel::removeSet,
            onUpdateSet = viewModel::updateSet,
            onSetRestTimeForAll = viewModel::setRestTimeForAll,
            onUpdateNote = viewModel::updateExerciseNote,
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

@Suppress("LongParameterList")
@Composable
internal fun ProgramFormContent(
    uiState: ProgramFormUiState,
    rows: List<ProgramExerciseRow>,
    onNameChange: (String) -> Unit,
    onMoveExercise: (Int, Int) -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onAddSet: (Int) -> Unit,
    onRemoveSet: (Int, Int) -> Unit,
    onUpdateSet: (Int, Int, SetEntry) -> Unit,
    onSetRestTimeForAll: (Int, String) -> Unit,
    onUpdateNote: (Int, String?) -> Unit,
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

            if (rows.isNotEmpty()) {
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
                rows,
                key = { _, row -> row.exerciseEntry.entryId },
                contentType = { _, _ -> "exercise" }
            ) { exerciseIndex, row ->
                ExerciseCardItem(
                    exerciseIndex = exerciseIndex,
                    row = row,
                    lastIndex = rows.lastIndex,
                    onMoveExercise = onMoveExercise,
                    onRemoveExercise = onRemoveExercise,
                    onAddSet = onAddSet,
                    onRemoveSet = onRemoveSet,
                    onUpdateSet = onUpdateSet,
                    onSetRestTimeForAll = onSetRestTimeForAll,
                    onUpdateNote = onUpdateNote,
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

@Suppress("LongParameterList", "LongMethod")
@Composable
private fun ExerciseCardItem(
    exerciseIndex: Int,
    row: ProgramExerciseRow,
    lastIndex: Int,
    onMoveExercise: (Int, Int) -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onAddSet: (Int) -> Unit,
    onRemoveSet: (Int, Int) -> Unit,
    onUpdateSet: (Int, Int, SetEntry) -> Unit,
    onSetRestTimeForAll: (Int, String) -> Unit,
    onUpdateNote: (Int, String?) -> Unit,
    onExerciseTitleClick: (Long) -> Unit
) {
    val exerciseEntry = row.exerciseEntry
    val exId = exerciseEntry.exercise.id
    val entrySets = exerciseEntry.sets

    val onWeightChange = remember<(SetUiModel, Int) -> Unit>(exerciseIndex, entrySets, onUpdateSet) {
        {
                model, w ->
            val entry = entrySets[model.setIndex]
            onUpdateSet(exerciseIndex, model.setIndex, entry.copy(weight = formatWeightInput(w)))
        }
    }
    val onRepsChange = remember<(SetUiModel, Int) -> Unit>(exerciseIndex, entrySets, onUpdateSet) {
        {
                model, r ->
            val entry = entrySets[model.setIndex]
            onUpdateSet(
                exerciseIndex,
                model.setIndex,
                entry.copy(reps = if (r == 0) "" else r.toString())
            )
        }
    }
    val onRestTimeChange = remember<(SetUiModel, Int) -> Unit>(exerciseIndex, entrySets, onUpdateSet) {
        {
                model, s ->
            val entry = entrySets[model.setIndex]
            onUpdateSet(
                exerciseIndex,
                model.setIndex,
                entry.copy(restTimeSeconds = secondsToMmSsDigits(s))
            )
        }
    }
    val onRemoveSetCb = remember<(SetUiModel) -> Unit>(exerciseIndex, onRemoveSet) {
        {
                model ->
            onRemoveSet(exerciseIndex, model.setIndex)
        }
    }
    val onSetRestTimeForAllCb = remember<(Int) -> Unit>(exerciseIndex, onSetRestTimeForAll) {
        {
                seconds ->
            onSetRestTimeForAll(exerciseIndex, secondsToMmSsDigits(seconds))
        }
    }
    val onAddSetCb = remember(exerciseIndex, onAddSet) { { onAddSet(exerciseIndex) } }
    val onRemoveExerciseCb = remember(exerciseIndex, onRemoveExercise) { { onRemoveExercise(exerciseIndex) } }
    val onExerciseTitleClickCb = remember(exId, onExerciseTitleClick) { { onExerciseTitleClick(exId) } }
    val onUpdateNoteCb = remember<(String?) -> Unit>(exerciseIndex, onUpdateNote) {
        {
                note ->
            onUpdateNote(exerciseIndex, note)
        }
    }

    val canMoveUp = exerciseIndex > 0
    val canMoveDown = exerciseIndex < lastIndex
    val onMoveUp = remember(canMoveUp, exerciseIndex, onMoveExercise) {
        if (canMoveUp) ({ onMoveExercise(exerciseIndex, exerciseIndex - 1) }) else null
    }
    val onMoveDown = remember(canMoveDown, exerciseIndex, onMoveExercise) {
        if (canMoveDown) ({ onMoveExercise(exerciseIndex, exerciseIndex + 1) }) else null
    }

    ExerciseCard(
        exerciseName = exerciseEntry.exercise.name,
        sets = row.sets,
        onAddSet = onAddSetCb,
        onRemoveExercise = onRemoveExerciseCb,
        onWeightChange = onWeightChange,
        onRepsChange = onRepsChange,
        onRestTimeChange = onRestTimeChange,
        onRemoveSet = onRemoveSetCb,
        onSetRestTimeForAll = onSetRestTimeForAllCb,
        onExerciseTitleClick = onExerciseTitleClickCb,
        showComplete = false,
        showAddSetButton = true,
        onMoveUp = onMoveUp,
        onMoveDown = onMoveDown,
        showBottomDivider = canMoveDown,
        note = exerciseEntry.note,
        onUpdateNote = onUpdateNoteCb
    )
}
