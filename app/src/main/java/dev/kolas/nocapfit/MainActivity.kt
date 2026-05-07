package dev.kolas.nocapfit

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.kolas.nocapfit.data.preferences.ThemeMode
import dev.kolas.nocapfit.data.preferences.ThemePreferences
import dev.kolas.nocapfit.ui.components.BottomNavBar
import dev.kolas.nocapfit.ui.components.MiniWorkoutPanel
import dev.kolas.nocapfit.ui.navigation.NavGraph
import dev.kolas.nocapfit.ui.navigation.Screen
import dev.kolas.nocapfit.ui.screens.main.ActiveWorkoutViewModel
import dev.kolas.nocapfit.ui.theme.NoCapFitTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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
            val dynamicColor by themePreferences.dynamicColor.collectAsState(initial = false)
            NoCapFitTheme(themeMode = themeMode, dynamicColor = dynamicColor) {
                MainContent()
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
private fun MainContent(
    activeWorkoutViewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val activeWorkout by activeWorkoutViewModel.activeWorkout.collectAsState()

    val isOnWorkoutScreen = currentRoute == Screen.WorkoutInProgress.route
    val isMinimized = activeWorkout != null && !isOnWorkoutScreen
    val showBottomBar = currentRoute in BOTTOM_NAV_ROUTES || isMinimized

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                MainBottomBar(
                    showMiniPanel = isMinimized,
                    minimizedWorkoutName = activeWorkout?.programName,
                    minimizedWorkoutStartTime = activeWorkout?.startTime ?: 0L,
                    currentRoute = currentRoute,
                    onResume = {
                        val id = activeWorkout?.id ?: return@MainBottomBar
                        navController.navigateBottomNav(Screen.WorkoutInProgress.createRoute(id))
                    },
                    onNavigate = { screen ->
                        navController.navigateBottomNav(screen.route)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            onMinimizeWorkout = {
                navController.navigate(Screen.WorkoutHistory.route) {
                    popUpTo(Screen.WorkoutHistory.route) { inclusive = true }
                }
            }
        )
    }
}

private fun NavController.navigateBottomNav(route: String) {
    navigate(route) {
        popUpTo(Screen.WorkoutHistory.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
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
