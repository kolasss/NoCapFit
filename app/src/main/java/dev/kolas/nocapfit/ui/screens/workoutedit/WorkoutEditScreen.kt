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
import dev.kolas.nocapfit.ui.components.ExerciseCard
import dev.kolas.nocapfit.ui.components.ExercisePickerSheet
import dev.kolas.nocapfit.ui.model.SetUiModel
import dev.kolas.nocapfit.ui.model.WorkoutExerciseRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutEditScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: WorkoutEditViewModel = hiltViewModel()
) {
    val workout by viewModel.workout.collectAsState()
    val rows by viewModel.exerciseRows.collectAsState()
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
            dataLoaded = workout != null,
            rows = rows,
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
    dataLoaded: Boolean,
    rows: List<WorkoutExerciseRow>,
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
    if (!dataLoaded) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

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
            rows,
            key = { _, item -> item.workoutExercise.id }
        ) { index, row ->
            WorkoutEditExerciseItem(
                row = row,
                index = index,
                lastIndex = rows.lastIndex,
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

@Suppress("LongParameterList", "LongMethod")
@Composable
private fun WorkoutEditExerciseItem(
    row: WorkoutExerciseRow,
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
    val id = row.workoutExercise.id
    val setsById = row.setsById

    val weightChange = remember<(SetUiModel, Int) -> Unit>(setsById, onUpdateSet) {
        {
                model, w ->
            setsById[model.id]?.let { onUpdateSet(it, w) }
        }
    }
    val repsChange = remember<(SetUiModel, Int) -> Unit>(setsById, onUpdateReps) {
        {
                model, r ->
            setsById[model.id]?.let { onUpdateReps(it, r) }
        }
    }
    val toggleComplete = remember<(SetUiModel) -> Unit>(setsById, onToggleComplete) {
        {
                model ->
            setsById[model.id]?.let { onToggleComplete(it) }
        }
    }
    val addSet = remember(id, onAddSet) { { onAddSet(id) } }
    val removeExercise = remember(id, onRemoveExercise) { { onRemoveExercise(id) } }
    val updateNote = remember<(String?) -> Unit>(id, onUpdateNote) {
        {
                note ->
            onUpdateNote(id, note)
        }
    }

    val canMoveUp = index > 0
    val canMoveDown = index < lastIndex
    val moveUp = remember(canMoveUp, id, onMoveExercise) {
        if (canMoveUp) ({ onMoveExercise(id, -1) }) else null
    }
    val moveDown = remember(canMoveDown, id, onMoveExercise) {
        if (canMoveDown) ({ onMoveExercise(id, 1) }) else null
    }

    ExerciseCard(
        exerciseName = row.workoutExercise.exerciseName,
        sets = row.sets,
        onAddSet = addSet,
        onRemoveExercise = removeExercise,
        onWeightChange = weightChange,
        onRepsChange = repsChange,
        onToggleComplete = toggleComplete,
        showRestTime = false,
        onMoveUp = moveUp,
        onMoveDown = moveDown,
        showBottomDivider = canMoveDown,
        note = row.workoutExercise.note,
        onUpdateNote = updateNote
    )
}
