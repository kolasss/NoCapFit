package dev.kolas.nocapfit.ui.screens.workoutedit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import dev.kolas.nocapfit.data.db.relation.WorkoutWithExercises
import dev.kolas.nocapfit.ui.components.ExerciseCard
import dev.kolas.nocapfit.ui.components.ExerciseNoteDialog
import dev.kolas.nocapfit.ui.components.ExercisePickerSheet
import dev.kolas.nocapfit.ui.model.SetUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutEditScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: WorkoutEditViewModel = hiltViewModel()
) {
    val workout by viewModel.workout.collectAsState()
    val programName by viewModel.programName.collectAsState()
    val availableExercises by viewModel.availableExercises.collectAsState()
    var showExercisePicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Edit Workout") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save { navController.popBackStack() } }
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        WorkoutEditContent(
            data = workout,
            programName = programName,
            onProgramNameChange = viewModel::updateProgramName,
            onMoveExercise = viewModel::moveExercise,
            onRemoveExercise = viewModel::removeExercise,
            onAddSet = viewModel::addSet,
            onUpdateSet = { ws, w -> viewModel.updateSet(ws.copy(weightThousandths = w)) },
            onUpdateReps = { ws, r -> viewModel.updateSet(ws.copy(reps = r)) },
            onToggleComplete = viewModel::toggleSetCompleted,
            onUpdateNote = viewModel::updateExerciseNote,
            onAddExerciseClick = { showExercisePicker = true },
            modifier = Modifier.padding(padding)
        )
    }

    if (showExercisePicker) {
        ExercisePickerSheet(
            exercises = availableExercises,
            onSelectExercise = { exercise ->
                viewModel.addExercise(exercise.id, exercise.name)
                showExercisePicker = false
            },
            onDismiss = { showExercisePicker = false }
        )
    }
}

@Suppress("LongParameterList")
@Composable
internal fun WorkoutEditContent(
    data: WorkoutWithExercises?,
    programName: String,
    onProgramNameChange: (String) -> Unit,
    onMoveExercise: (Long, Int) -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onAddSet: (Long) -> Unit,
    onUpdateSet: (dev.kolas.nocapfit.data.db.entity.WorkoutSet, Int) -> Unit,
    onUpdateReps: (dev.kolas.nocapfit.data.db.entity.WorkoutSet, Int) -> Unit,
    onToggleComplete: (dev.kolas.nocapfit.data.db.entity.WorkoutSet) -> Unit,
    onUpdateNote: (Long, String?) -> Unit,
    onAddExerciseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (data == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val sortedExercises = data.exercises.sortedBy { it.workoutExercise.orderIndex }

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item {
            OutlinedTextField(
                value = programName,
                onValueChange = onProgramNameChange,
                label = { Text("Program Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        itemsIndexed(
            sortedExercises,
            key = { _, item -> item.workoutExercise.id }
        ) { index, exerciseWithSets ->
            WorkoutEditExerciseItem(
                exerciseWithSets = exerciseWithSets,
                index = index,
                lastIndex = sortedExercises.lastIndex,
                onMoveExercise = onMoveExercise,
                onRemoveExercise = onRemoveExercise,
                onAddSet = onAddSet,
                onUpdateSet = onUpdateSet,
                onUpdateReps = onUpdateReps,
                onToggleComplete = onToggleComplete,
                onUpdateNote = onUpdateNote
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(onClick = onAddExerciseClick) {
                    Text("+ Add Exercise")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun WorkoutEditExerciseItem(
    exerciseWithSets: dev.kolas.nocapfit.data.db.relation.WorkoutExerciseWithSets,
    index: Int,
    lastIndex: Int,
    onMoveExercise: (Long, Int) -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onAddSet: (Long) -> Unit,
    onUpdateSet: (dev.kolas.nocapfit.data.db.entity.WorkoutSet, Int) -> Unit,
    onUpdateReps: (dev.kolas.nocapfit.data.db.entity.WorkoutSet, Int) -> Unit,
    onToggleComplete: (dev.kolas.nocapfit.data.db.entity.WorkoutSet) -> Unit,
    onUpdateNote: (Long, String?) -> Unit
) {
    val id = exerciseWithSets.workoutExercise.id
    val sets = exerciseWithSets.sets
    val setsById = remember(sets) { sets.associateBy { it.id } }
    var showNoteDialog by remember { mutableStateOf(false) }
    val setUiModels = remember(sets) {
        sets.map { ws ->
            SetUiModel(
                id = ws.id,
                setIndex = ws.setIndex,
                weightThousandths = ws.weightThousandths,
                reps = ws.reps,
                restTimeSeconds = ws.restTimeSeconds,
                completed = ws.completed
            )
        }
    }
    ExerciseCard(
        exerciseName = exerciseWithSets.workoutExercise.exerciseName,
        sets = setUiModels,
        onAddSet = { onAddSet(id) },
        onRemoveExercise = { onRemoveExercise(id) },
        onWeightChange = { model, w ->
            setsById[model.id]?.let { onUpdateSet(it, w) }
        },
        onRepsChange = { model, r ->
            setsById[model.id]?.let { onUpdateReps(it, r) }
        },
        onToggleComplete = { model ->
            setsById[model.id]?.let { onToggleComplete(it) }
        },
        showRestTime = false,
        onMoveUp = if (index > 0) { { onMoveExercise(id, -1) } } else null,
        onMoveDown = if (index < lastIndex) {
            { onMoveExercise(id, 1) }
        } else {
            null
        },
        showBottomDivider = index < lastIndex,
        note = exerciseWithSets.workoutExercise.note,
        onEditNote = { showNoteDialog = true }
    )
    if (showNoteDialog) {
        ExerciseNoteDialog(
            initialValue = exerciseWithSets.workoutExercise.note,
            onConfirm = { note ->
                onUpdateNote(id, note)
                showNoteDialog = false
            },
            onDismiss = { showNoteDialog = false }
        )
    }
}
