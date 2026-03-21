package com.example.nocapfit.ui.screens.programs

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nocapfit.data.db.relation.ProgramWithExercises
import com.example.nocapfit.ui.components.EmptyState
import com.example.nocapfit.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramListScreen(
    navController: NavController,
    viewModel: ProgramListViewModel = hiltViewModel()
) {
    val programs by viewModel.programs.collectAsState()
    var programToDelete by remember { mutableStateOf<ProgramWithExercises?>(null) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isScrolled by remember { derivedStateOf { scrollBehavior.state.collapsedFraction > 0.5f } }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Programs") },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.ProgramForm.createRoute()) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Program") },
                expanded = !isScrolled
            )
        }
    ) { padding ->
        if (programs.isEmpty()) {
            EmptyState(
                icon = Icons.AutoMirrored.Filled.ListAlt,
                title = "No programs yet",
                subtitle = "Create a program to organize your exercises",
                actionLabel = "New Program",
                onAction = { navController.navigate(Screen.ProgramForm.createRoute()) },
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(programs, key = { it.program.id }) { programWithExercises ->
                    ProgramListItem(
                        programWithExercises = programWithExercises,
                        onClick = {
                            navController.navigate(
                                Screen.ProgramForm.createRoute(programWithExercises.program.id)
                            )
                        },
                        onDeleteRequest = { programToDelete = programWithExercises },
                        modifier = Modifier.animateItem()
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        if (programToDelete != null) {
            AlertDialog(
                onDismissRequest = { programToDelete = null },
                title = { Text("Delete Program") },
                text = {
                    Text("Are you sure you want to delete \"${programToDelete!!.program.name}\"?")
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteProgram(programToDelete!!)
                        programToDelete = null
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { programToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgramListItem(
    programWithExercises: ProgramWithExercises,
    onClick: () -> Unit,
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDeleteRequest()
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                label = "swipe-bg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable(onClick = onClick)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = programWithExercises.program.name,
                    style = MaterialTheme.typography.titleMedium
                )
                val exerciseNames = programWithExercises.exercises
                    .sortedBy { it.programExercise.orderIndex }
                    .take(3)
                    .joinToString(", ") { it.exercise.name }
                val exerciseCount = programWithExercises.exercises.size
                Text(
                    text = exerciseNames.ifBlank { "$exerciseCount exercise${if (exerciseCount != 1) "s" else ""}" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (exerciseNames.isNotBlank() && exerciseCount > 3) {
                    Text(
                        text = "+${exerciseCount - 3} more",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
