package com.example.nocapfit.ui.screens.workoutedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.nocapfit.data.db.relation.WorkoutWithExercises
import com.example.nocapfit.ui.components.ExerciseCard
import com.example.nocapfit.ui.components.ExercisePickerSheet

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
                    IconButton(onClick = {
                        viewModel.saveProgramName()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        WorkoutEditContent(
            data = workout,
            programName = programName,
            onProgramNameChange = viewModel::updateProgramName,
            onRemoveExercise = viewModel::removeExercise,
            onAddSet = viewModel::addSet,
            onUpdateSet = { ws, w -> viewModel.updateSet(ws.copy(weightThousandths = w)) },
            onUpdateReps = { ws, r -> viewModel.updateSet(ws.copy(reps = r)) },
            onToggleComplete = viewModel::toggleSetCompleted,
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

@Composable
internal fun WorkoutEditContent(
    data: WorkoutWithExercises?,
    programName: String,
    onProgramNameChange: (String) -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onAddSet: (Long) -> Unit,
    onUpdateSet: (com.example.nocapfit.data.db.entity.WorkoutSet, Int) -> Unit,
    onUpdateReps: (com.example.nocapfit.data.db.entity.WorkoutSet, Int) -> Unit,
    onToggleComplete: (com.example.nocapfit.data.db.entity.WorkoutSet) -> Unit,
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
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = programName,
                onValueChange = onProgramNameChange,
                label = { Text("Program Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(sortedExercises, key = { it.workoutExercise.id }) { exerciseWithSets ->
            ExerciseCard(
                exerciseName = exerciseWithSets.workoutExercise.exerciseName,
                sets = exerciseWithSets.sets,
                workoutExerciseId = exerciseWithSets.workoutExercise.id,
                onRemoveExercise = onRemoveExercise,
                onAddSet = onAddSet,
                onWeightChange = { ws, w -> onUpdateSet(ws, w) },
                onRepsChange = { ws, r -> onUpdateReps(ws, r) },
                onToggleComplete = { ws -> onToggleComplete(ws) }
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
