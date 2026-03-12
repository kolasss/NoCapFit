package com.example.nocapfit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nocapfit.ui.screens.addworkout.AddWorkoutScreen
import com.example.nocapfit.ui.screens.exercises.ExerciseListScreen
import com.example.nocapfit.ui.screens.programs.ProgramFormScreen
import com.example.nocapfit.ui.screens.programs.ProgramListScreen
import com.example.nocapfit.ui.screens.workout.WorkoutInProgressScreen
import com.example.nocapfit.ui.screens.workoutdetail.WorkoutDetailScreen
import com.example.nocapfit.ui.screens.workouthistory.WorkoutHistoryScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.WorkoutHistory.route,
        modifier = modifier
    ) {
        composable(Screen.WorkoutHistory.route) {
            WorkoutHistoryScreen(navController = navController)
        }
        composable(Screen.ExerciseList.route) {
            ExerciseListScreen(navController = navController)
        }
        composable(Screen.ProgramList.route) {
            ProgramListScreen(navController = navController)
        }
        composable(Screen.AddWorkout.route) {
            AddWorkoutScreen(navController = navController)
        }
        composable(
            route = Screen.ProgramForm.route,
            arguments = listOf(navArgument("programId") { type = NavType.LongType })
        ) {
            val programId = it.arguments?.getLong("programId") ?: -1L
            ProgramFormScreen(navController = navController, programId = programId)
        }
        composable(
            route = Screen.WorkoutInProgress.route,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
        ) {
            WorkoutInProgressScreen(navController = navController)
        }
        composable(
            route = Screen.WorkoutDetail.route,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
        ) {
            WorkoutDetailScreen(navController = navController)
        }
    }
}
