package com.example.nocapfit.ui.screens.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.nocapfit.data.db.entity.Exercise
import com.example.nocapfit.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseListScreen(
    modifier: Modifier = Modifier,
    viewModel: ExerciseListViewModel = hiltViewModel()
) {
    val exercises by viewModel.exercises.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    val selectedExercise by viewModel.selectedExercise.collectAsState()

    var showDeleteConfirmation by remember { mutableStateOf<Exercise?>(null) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isScrolled by remember { derivedStateOf { scrollBehavior.state.collapsedFraction > 0.5f } }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Exercises") },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Exercise") },
                expanded = !isScrolled
            )
        }
    ) { padding ->
        ExerciseListContent(
            exercises = exercises,
            searchQuery = searchQuery,
            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
            onShowAddDialog = { viewModel.showAddDialog() },
            onEditExercise = { viewModel.showEditDialog(it) },
            onDeleteExercise = { showDeleteConfirmation = it },
            modifier = Modifier.padding(padding)
        )
    }

    ExerciseDialogs(
        showAddDialog = showAddDialog,
        showEditDialog = showEditDialog,
        selectedExercise = selectedExercise,
        showDeleteConfirmation = showDeleteConfirmation,
        onDismissAdd = { viewModel.dismissAddDialog() },
        onAddExercise = { name, description, tags ->
            viewModel.addExercise(name, description, tags)
        },
        onDismissEdit = { viewModel.dismissEditDialog() },
        onUpdateExercise = { viewModel.updateExercise(it) },
        onDeleteExercise = { exercise ->
            viewModel.deleteExercise(exercise)
            showDeleteConfirmation = null
        },
        onDismissDelete = { showDeleteConfirmation = null }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseListContent(
    exercises: List<Exercise>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onShowAddDialog: () -> Unit,
    onEditExercise: (Exercise) -> Unit,
    onDeleteExercise: (Exercise) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearch = { },
                    expanded = false,
                    onExpandedChange = { },
                    placeholder = { Text("Search exercises") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
            },
            expanded = false,
            onExpandedChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {}

        if (exercises.isEmpty() && searchQuery.isBlank()) {
            EmptyState(
                icon = Icons.Default.FitnessCenter,
                title = "No exercises yet",
                subtitle = "Add exercises to use in your workouts",
                actionLabel = "New Exercise",
                onAction = onShowAddDialog
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
            ) {
                items(exercises, key = { it.id }) { exercise ->
                    ExerciseItem(
                        exercise = exercise,
                        onEdit = { onEditExercise(exercise) },
                        onDelete = { onDeleteExercise(exercise) },
                        modifier = Modifier.animateItem()
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun ExerciseDialogs(
    showAddDialog: Boolean,
    showEditDialog: Boolean,
    selectedExercise: Exercise?,
    showDeleteConfirmation: Exercise?,
    onDismissAdd: () -> Unit,
    onAddExercise: (String, String, String) -> Unit,
    onDismissEdit: () -> Unit,
    onUpdateExercise: (Exercise) -> Unit,
    onDeleteExercise: (Exercise) -> Unit,
    onDismissDelete: () -> Unit
) {
    if (showAddDialog) {
        ExerciseFormSheet(
            title = "Add Exercise",
            initialName = "",
            initialDescription = "",
            initialTags = "",
            onDismiss = onDismissAdd,
            onConfirm = onAddExercise
        )
    }

    if (showEditDialog && selectedExercise != null) {
        ExerciseFormSheet(
            title = "Edit Exercise",
            initialName = selectedExercise.name,
            initialDescription = selectedExercise.description,
            initialTags = selectedExercise.tags,
            onDismiss = onDismissEdit,
            onConfirm = { name, description, tags ->
                onUpdateExercise(
                    selectedExercise.copy(
                        name = name.trim(),
                        description = description.trim(),
                        tags = tags.trim()
                    )
                )
            }
        )
    }

    showDeleteConfirmation?.let { exercise ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("Delete Exercise") },
            text = { Text("Are you sure you want to delete \"${exercise.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { onDeleteExercise(exercise) }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseItem(
    exercise: Exercise,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            if (exercise.description.isNotBlank()) {
                Text(
                    text = exercise.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (exercise.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    exercise.tags.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .forEach { tag ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(tag) }
                            )
                        }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseFormSheet(
    title: String,
    initialName: String,
    initialDescription: String,
    initialTags: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, tags: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }
    var tags by remember { mutableStateOf(initialTags) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags (comma separated)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(
                    onClick = { onConfirm(name, description, tags) },
                    enabled = name.isNotBlank()
                ) {
                    Text("Save")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
