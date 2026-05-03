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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import dev.kolas.nocapfit.ui.components.ExerciseCard
import dev.kolas.nocapfit.ui.components.ExercisePickerSheet
import dev.kolas.nocapfit.ui.components.parseMmSsToSeconds
import dev.kolas.nocapfit.ui.components.secondsToMmSsDigits
import dev.kolas.nocapfit.ui.model.PreviousSetLookup
import dev.kolas.nocapfit.ui.model.PreviousTextsForExercise
import dev.kolas.nocapfit.ui.model.SetUiModel
import dev.kolas.nocapfit.ui.model.buildPreviousTextsByExercise
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
    previousSets: PreviousSetLookup,
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
        val previousTextsByExercise = remember(previousSets) {
            buildPreviousTextsByExercise(previousSets)
        }
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
                    previousTexts = previousTextsByExercise.forExercise(exerciseEntry.exercise.id),
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
    exerciseEntry: ExerciseEntry,
    lastIndex: Int,
    previousTexts: PreviousTextsForExercise,
    onMoveExercise: (Int, Int) -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onAddSet: (Int) -> Unit,
    onRemoveSet: (Int, Int) -> Unit,
    onUpdateSet: (Int, Int, SetEntry) -> Unit,
    onSetRestTimeForAll: (Int, String) -> Unit,
    onUpdateNote: (Int, String?) -> Unit,
    onExerciseTitleClick: (Long) -> Unit
) {
    val exId = exerciseEntry.exercise.id
    val setUiModels = remember(exerciseEntry.sets, previousTexts) {
        exerciseEntry.sets.mapIndexed { setIndex, setEntry ->
            SetUiModel(
                id = setIndex.toLong(),
                setIndex = setIndex,
                weightThousandths = parseWeight(setEntry.weight),
                reps = setEntry.reps.toIntOrNull() ?: 0,
                restTimeSeconds = parseMmSsToSeconds(setEntry.restTimeSeconds),
                completed = false,
                previousText = previousTexts[setIndex]
            )
        }
    }

    val currentIndex by rememberUpdatedState(exerciseIndex)
    val currentEntry by rememberUpdatedState(exerciseEntry)
    val currentOnMoveExercise by rememberUpdatedState(onMoveExercise)
    val currentOnRemoveExercise by rememberUpdatedState(onRemoveExercise)
    val currentOnAddSet by rememberUpdatedState(onAddSet)
    val currentOnRemoveSet by rememberUpdatedState(onRemoveSet)
    val currentOnUpdateSet by rememberUpdatedState(onUpdateSet)
    val currentOnSetRestTimeForAll by rememberUpdatedState(onSetRestTimeForAll)
    val currentOnUpdateNote by rememberUpdatedState(onUpdateNote)
    val currentOnExerciseTitleClick by rememberUpdatedState(onExerciseTitleClick)

    val onAddSetCb = remember { { currentOnAddSet(currentIndex) } }
    val onRemoveExerciseCb = remember { { currentOnRemoveExercise(currentIndex) } }
    val onWeightChangeCb = remember<(SetUiModel, Int) -> Unit> {
        {
                model, w ->
            val entry = currentEntry.sets[model.setIndex]
            currentOnUpdateSet(currentIndex, model.setIndex, entry.copy(weight = formatWeightInput(w)))
        }
    }
    val onRepsChangeCb = remember<(SetUiModel, Int) -> Unit> {
        {
                model, r ->
            val entry = currentEntry.sets[model.setIndex]
            currentOnUpdateSet(
                currentIndex,
                model.setIndex,
                entry.copy(reps = if (r == 0) "" else r.toString())
            )
        }
    }
    val onRestTimeChangeCb = remember<(SetUiModel, Int) -> Unit> {
        {
                model, s ->
            val entry = currentEntry.sets[model.setIndex]
            currentOnUpdateSet(
                currentIndex,
                model.setIndex,
                entry.copy(restTimeSeconds = secondsToMmSsDigits(s))
            )
        }
    }
    val onRemoveSetCb = remember<(SetUiModel) -> Unit> {
        {
                model ->
            currentOnRemoveSet(currentIndex, model.setIndex)
        }
    }
    val onSetRestTimeForAllCb = remember<(Int) -> Unit> {
        {
                seconds ->
            currentOnSetRestTimeForAll(currentIndex, secondsToMmSsDigits(seconds))
        }
    }
    val onExerciseTitleClickCb = remember { { currentOnExerciseTitleClick(exId) } }
    val onUpdateNoteCb = remember<(String?) -> Unit> {
        {
                note ->
            currentOnUpdateNote(currentIndex, note)
        }
    }

    val canMoveUp = exerciseIndex > 0
    val canMoveDown = exerciseIndex < lastIndex
    val onMoveUp = remember(canMoveUp) {
        if (canMoveUp) {
            { currentOnMoveExercise(currentIndex, currentIndex - 1) }
        } else {
            null
        }
    }
    val onMoveDown = remember(canMoveDown) {
        if (canMoveDown) {
            { currentOnMoveExercise(currentIndex, currentIndex + 1) }
        } else {
            null
        }
    }

    ExerciseCard(
        exerciseName = exerciseEntry.exercise.name,
        sets = setUiModels,
        onAddSet = onAddSetCb,
        onRemoveExercise = onRemoveExerciseCb,
        onWeightChange = onWeightChangeCb,
        onRepsChange = onRepsChangeCb,
        onRestTimeChange = onRestTimeChangeCb,
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
