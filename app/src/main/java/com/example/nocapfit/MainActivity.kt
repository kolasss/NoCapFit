package com.example.nocapfit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nocapfit.data.repository.WorkoutRepository
import com.example.nocapfit.ui.components.BottomNavBar
import com.example.nocapfit.ui.navigation.NavGraph
import com.example.nocapfit.ui.navigation.Screen
import com.example.nocapfit.ui.theme.NoCapFitTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var workoutRepository: WorkoutRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoCapFitTheme {
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
    val showBottomBar = currentRoute in bottomNavRoutes

    LaunchedEffect(Unit) {
        val activeWorkout = workoutRepository.getActiveWorkout()
        if (activeWorkout != null) {
            navController.navigate(Screen.WorkoutInProgress.createRoute(activeWorkout.id)) {
                popUpTo(Screen.WorkoutHistory.route) { inclusive = false }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
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
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
