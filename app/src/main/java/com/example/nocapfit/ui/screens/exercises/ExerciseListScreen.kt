package com.example.nocapfit.ui.screens.exercises

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nocapfit.data.db.entity.Exercise
import com.example.nocapfit.ui.components.EmptyState
import com.example.nocapfit.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ExerciseListViewModel = hiltViewModel()
) {
    val exercises by viewModel.exercises.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()

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
            onExerciseClick = { exercise ->
                navController.navigate(Screen.ExerciseDetail.createRoute(exercise.id))
            },
            modifier = Modifier.padding(padding)
        )
    }

    if (showAddDialog) {
        ExerciseFormSheet(
            title = "Add Exercise",
            initialName = "",
            initialDescription = "",
            initialTags = "",
            onDismiss = { viewModel.dismissAddDialog() },
            onConfirm = { name, description, tags ->
                viewModel.addExercise(name, description, tags)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExerciseListContent(
    exercises: List<Exercise>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onShowAddDialog: () -> Unit,
    onExerciseClick: (Exercise) -> Unit,
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
                        onClick = { onExerciseClick(exercise) },
                        modifier = Modifier.animateItem()
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
internal fun ExerciseItem(
    exercise: Exercise,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium
            )

            if (exercise.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = exercise.tags,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
