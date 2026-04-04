package com.example.nocapfit.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nocapfit.ui.screens.addworkout.AddWorkoutScreen
import com.example.nocapfit.ui.screens.exercises.ExerciseDetailScreen
import com.example.nocapfit.ui.screens.exercises.ExerciseListScreen
import com.example.nocapfit.ui.screens.programs.ProgramFormScreen
import com.example.nocapfit.ui.screens.programs.ProgramListScreen
import com.example.nocapfit.ui.screens.settings.SettingsScreen
import com.example.nocapfit.ui.screens.workout.WorkoutInProgressScreen
import com.example.nocapfit.ui.screens.workoutdetail.WorkoutDetailScreen
import com.example.nocapfit.ui.screens.workoutedit.WorkoutEditScreen
import com.example.nocapfit.ui.screens.workouthistory.WorkoutHistoryScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onMinimizeWorkout: ((Long) -> Unit)? = null
) {
    NavHost(
        navController = navController,
        startDestination = Screen.WorkoutHistory.route,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(Screen.WorkoutHistory.route) {
            WorkoutHistoryScreen(navController = navController)
        }
        composable(Screen.ExerciseList.route) {
            ExerciseListScreen(navController = navController)
        }
        composable(
            route = Screen.ExerciseDetail.route,
            arguments = listOf(navArgument("exerciseId") { type = NavType.LongType })
        ) {
            ExerciseDetailScreen(navController = navController)
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
            ProgramFormScreen(navController = navController)
        }
        composable(
            route = Screen.WorkoutInProgress.route,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
        ) {
            WorkoutInProgressScreen(
                navController = navController,
                onMinimize = onMinimizeWorkout
            )
        }
        composable(
            route = Screen.WorkoutDetail.route,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
        ) {
            WorkoutDetailScreen(navController = navController)
        }
        composable(
            route = Screen.WorkoutEdit.route,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
        ) {
            WorkoutEditScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
