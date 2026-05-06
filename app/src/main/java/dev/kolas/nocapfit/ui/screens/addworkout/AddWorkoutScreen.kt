package dev.kolas.nocapfit.ui.screens.addworkout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import dev.kolas.nocapfit.data.db.relation.ProgramWithExercises
import dev.kolas.nocapfit.ui.components.ConfirmDialog
import dev.kolas.nocapfit.ui.components.SubtleSectionHeader
import dev.kolas.nocapfit.ui.navigation.Screen
import dev.kolas.nocapfit.ui.util.formatRelativeDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AddWorkoutViewModel = hiltViewModel()
) {
    val programs by viewModel.programs.collectAsState()
    val lastWorkoutTimes by viewModel.lastWorkoutTimes.collectAsState()
    val profileLoaded by viewModel.profileLoaded.collectAsState()
    val scope = rememberCoroutineScope()
    var showActiveWorkoutDialog by remember { mutableStateOf(false) }
    var activeWorkoutId by remember { mutableLongStateOf(0L) }

    if (showActiveWorkoutDialog) {
        ActiveWorkoutDialog(
            onResume = {
                showActiveWorkoutDialog = false
                navController.navigate(Screen.WorkoutInProgress.createRoute(activeWorkoutId))
            },
            onDismiss = { showActiveWorkoutDialog = false }
        )
    }

    AddWorkoutContent(
        programs = programs,
        lastWorkoutTimes = lastWorkoutTimes,
        onBack = { navController.popBackStack() },
        onStartWorkout = { createWorkout ->
            if (!profileLoaded) return@AddWorkoutContent
            scope.launch {
                val existing = viewModel.getActiveWorkoutId()
                if (existing != null) {
                    activeWorkoutId = existing
                    showActiveWorkoutDialog = true
                    return@launch
                }
                val workoutId = createWorkout()
                navController.navigate(Screen.WorkoutInProgress.createRoute(workoutId))
            }
        },
        onCreateEmpty = { viewModel.createEmptyWorkout() },
        onCreateFromProgram = { viewModel.createWorkoutFromProgram(it) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWorkoutContent(
    programs: List<ProgramWithExercises>,
    lastWorkoutTimes: Map<Long, Long>,
    onBack: () -> Unit,
    onStartWorkout: (suspend () -> Long) -> Unit,
    onCreateEmpty: suspend () -> Long,
    onCreateFromProgram: suspend (Long) -> Long,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Start Workout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item(key = "header-quick-start") {
                SubtleSectionHeader(text = "QUICK START")
            }

            item(key = "quick-start") {
                QuickStartItem(onClick = { onStartWorkout(onCreateEmpty) })
            }

            if (programs.isNotEmpty()) {
                item(key = "header-from-program") {
                    SubtleSectionHeader(text = "FROM A PROGRAM")
                }

                items(programs, key = { it.program.id }) { programWithExercises ->
                    ProgramItem(
                        programWithExercises = programWithExercises,
                        lastWorkoutTime = lastWorkoutTimes[programWithExercises.program.id],
                        onClick = {
                            onStartWorkout { onCreateFromProgram(programWithExercises.program.id) }
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun QuickStartItem(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Empty Workout",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Start from scratch",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun ActiveWorkoutDialog(onResume: () -> Unit, onDismiss: () -> Unit) {
    ConfirmDialog(
        title = "Active Workout",
        message = "You already have a workout in progress. Resume it or cancel it first.",
        confirmLabel = "Resume",
        onConfirm = onResume,
        onDismiss = onDismiss
    )
}

@Composable
private fun ProgramItem(
    programWithExercises: ProgramWithExercises,
    lastWorkoutTime: Long?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val exerciseSummary = remember(programWithExercises.exercises) {
        programWithExercises.exercises
            .sortedBy { it.programExercise.orderIndex }
            .joinToString(", ") { it.exercise.name }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = programWithExercises.program.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (exerciseSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = exerciseSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (lastWorkoutTime != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatRelativeDate(lastWorkoutTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
