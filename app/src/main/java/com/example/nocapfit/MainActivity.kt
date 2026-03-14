package com.example.nocapfit

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nocapfit.data.preferences.ThemeMode
import com.example.nocapfit.data.preferences.ThemePreferences
import com.example.nocapfit.data.repository.WorkoutRepository
import com.example.nocapfit.ui.components.BottomNavBar
import com.example.nocapfit.ui.components.MiniWorkoutPanel
import com.example.nocapfit.ui.navigation.NavGraph
import com.example.nocapfit.ui.navigation.Screen
import com.example.nocapfit.ui.theme.NoCapFitTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var workoutRepository: WorkoutRepository

    @Inject
    lateinit var themePreferences: ThemePreferences

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no action needed on result */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        setContent {
            val themeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            NoCapFitTheme(themeMode = themeMode) {
                MainContent(workoutRepository)
            }
        }
    }
}

@Composable
private fun MainContent(workoutRepository: WorkoutRepository) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = listOf(
        Screen.WorkoutHistory.route,
        Screen.ExerciseList.route,
        Screen.ProgramList.route
    )

    var isWorkoutMinimized by rememberSaveable { mutableStateOf(false) }
    var minimizedWorkoutId by rememberSaveable { mutableLongStateOf(-1L) }
    var minimizedWorkoutStartTime by rememberSaveable { mutableLongStateOf(0L) }
    var minimizedWorkoutName by rememberSaveable { mutableStateOf<String?>(null) }

    val isOnWorkoutScreen = currentRoute == Screen.WorkoutInProgress.route
    val showBottomBar = currentRoute in bottomNavRoutes || isWorkoutMinimized
    val showMiniPanel = isWorkoutMinimized && !isOnWorkoutScreen

    LaunchedEffect(Unit) {
        val activeWorkout = workoutRepository.getActiveWorkout()
        if (activeWorkout != null) {
            // Start minimized rather than auto-navigating
            isWorkoutMinimized = true
            minimizedWorkoutId = activeWorkout.id
            minimizedWorkoutStartTime = activeWorkout.startTime
            minimizedWorkoutName = activeWorkout.programName
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                Column {
                    AnimatedVisibility(
                        visible = showMiniPanel,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        MiniWorkoutPanel(
                            workoutName = minimizedWorkoutName ?: "Workout",
                            startTimeMs = minimizedWorkoutStartTime,
                            onClick = {
                                isWorkoutMinimized = false
                                navController.navigate(
                                    Screen.WorkoutInProgress.createRoute(minimizedWorkoutId)
                                ) {
                                    popUpTo(Screen.WorkoutHistory.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }  // AnimatedVisibility
                    BottomNavBar(
                        currentRoute = currentRoute,
                        onNavigate = { screen ->
                            navController.navigate(screen.route) {
                                popUpTo(Screen.WorkoutHistory.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            onMinimizeWorkout = { workoutId ->
                isWorkoutMinimized = true
                minimizedWorkoutId = workoutId
                // Navigate back to history
                navController.navigate(Screen.WorkoutHistory.route) {
                    popUpTo(Screen.WorkoutHistory.route) { inclusive = true }
                }
            }
        )
    }

    // When navigating to workout screen, clear minimized state
    LaunchedEffect(isOnWorkoutScreen) {
        if (isOnWorkoutScreen) {
            isWorkoutMinimized = false
        }
    }

    // When workout finishes/cancels, clear minimized state
    LaunchedEffect(isWorkoutMinimized) {
        if (isWorkoutMinimized && minimizedWorkoutId > 0) {
            val workout = workoutRepository.getById(minimizedWorkoutId)
            if (workout == null || workout.endTime != null) {
                isWorkoutMinimized = false
                minimizedWorkoutId = -1L
            } else {
                minimizedWorkoutStartTime = workout.startTime
                minimizedWorkoutName = workout.programName
            }
        }
    }
}
