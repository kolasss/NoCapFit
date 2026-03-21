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
import androidx.compose.runtime.rememberUpdatedState
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

private val BOTTOM_NAV_ROUTES = listOf(
    Screen.WorkoutHistory.route,
    Screen.ExerciseList.route,
    Screen.ProgramList.route
)

@Composable
private fun MainContent(workoutRepository: WorkoutRepository) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var isWorkoutMinimized by rememberSaveable { mutableStateOf(false) }
    var minimizedWorkoutId by rememberSaveable { mutableLongStateOf(-1L) }
    var minimizedWorkoutStartTime by rememberSaveable { mutableLongStateOf(0L) }
    var minimizedWorkoutName by rememberSaveable { mutableStateOf<String?>(null) }

    val isOnWorkoutScreen = currentRoute == Screen.WorkoutInProgress.route
    val showBottomBar = currentRoute in BOTTOM_NAV_ROUTES || isWorkoutMinimized
    val showMiniPanel = isWorkoutMinimized && !isOnWorkoutScreen

    MinimizedWorkoutEffects(
        workoutRepository = workoutRepository,
        isOnWorkoutScreen = isOnWorkoutScreen,
        isWorkoutMinimized = isWorkoutMinimized,
        minimizedWorkoutId = minimizedWorkoutId,
        onRestore = { workout ->
            isWorkoutMinimized = true
            minimizedWorkoutId = workout.id
            minimizedWorkoutStartTime = workout.startTime
            minimizedWorkoutName = workout.programName
        },
        onClear = {
            isWorkoutMinimized = false
            minimizedWorkoutId = -1L
        },
        onUpdate = { startTime, name ->
            minimizedWorkoutStartTime = startTime
            minimizedWorkoutName = name
        },
        onClearWorkoutScreen = { isWorkoutMinimized = false }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                MainBottomBar(
                    showMiniPanel = showMiniPanel,
                    minimizedWorkoutName = minimizedWorkoutName,
                    minimizedWorkoutStartTime = minimizedWorkoutStartTime,
                    currentRoute = currentRoute,
                    onResume = {
                        isWorkoutMinimized = false
                        navController.navigate(
                            Screen.WorkoutInProgress.createRoute(minimizedWorkoutId)
                        ) {
                            popUpTo(Screen.WorkoutHistory.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
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
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            onMinimizeWorkout = { workoutId ->
                isWorkoutMinimized = true
                minimizedWorkoutId = workoutId
                navController.navigate(Screen.WorkoutHistory.route) {
                    popUpTo(Screen.WorkoutHistory.route) { inclusive = true }
                }
            }
        )
    }
}

@Composable
private fun MinimizedWorkoutEffects(
    workoutRepository: WorkoutRepository,
    isOnWorkoutScreen: Boolean,
    isWorkoutMinimized: Boolean,
    minimizedWorkoutId: Long,
    onRestore: (com.example.nocapfit.data.db.entity.Workout) -> Unit,
    onClear: () -> Unit,
    onUpdate: (startTime: Long, name: String?) -> Unit,
    onClearWorkoutScreen: () -> Unit
) {
    val currentOnRestore by rememberUpdatedState(onRestore)
    val currentOnClear by rememberUpdatedState(onClear)
    val currentOnUpdate by rememberUpdatedState(onUpdate)
    val currentOnClearWorkoutScreen by rememberUpdatedState(onClearWorkoutScreen)

    LaunchedEffect(Unit) {
        val activeWorkout = workoutRepository.getActiveWorkout()
        if (activeWorkout != null) {
            currentOnRestore(activeWorkout)
        }
    }

    LaunchedEffect(isOnWorkoutScreen) {
        if (isOnWorkoutScreen) currentOnClearWorkoutScreen()
    }

    LaunchedEffect(isWorkoutMinimized) {
        if (isWorkoutMinimized && minimizedWorkoutId > 0) {
            val workout = workoutRepository.getById(minimizedWorkoutId)
            if (workout == null || workout.endTime != null) {
                currentOnClear()
            } else {
                currentOnUpdate(workout.startTime, workout.programName)
            }
        }
    }
}

@Composable
private fun MainBottomBar(
    showMiniPanel: Boolean,
    minimizedWorkoutName: String?,
    minimizedWorkoutStartTime: Long,
    currentRoute: String?,
    onResume: () -> Unit,
    onNavigate: (Screen) -> Unit
) {
    Column {
        AnimatedVisibility(
            visible = showMiniPanel,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            MiniWorkoutPanel(
                workoutName = minimizedWorkoutName ?: "Workout",
                startTimeMs = minimizedWorkoutStartTime,
                onClick = onResume
            )
        }
        BottomNavBar(
            currentRoute = currentRoute,
            onNavigate = onNavigate
        )
    }
}
