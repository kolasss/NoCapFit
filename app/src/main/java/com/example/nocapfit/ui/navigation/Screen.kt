package com.example.nocapfit.ui.navigation

sealed class Screen(val route: String) {
    data object WorkoutHistory : Screen("workout_history")
    data object ExerciseList : Screen("exercise_list")
    data object ProgramList : Screen("program_list")
    data object AddWorkout : Screen("add_workout")
    data object ProgramForm : Screen("program_form/{programId}") {
        fun createRoute(programId: Long? = null) = "program_form/${programId ?: -1}"
    }
    data object WorkoutInProgress : Screen("workout_in_progress/{workoutId}") {
        fun createRoute(workoutId: Long) = "workout_in_progress/$workoutId"
    }
    data object WorkoutDetail : Screen("workout_detail/{workoutId}") {
        fun createRoute(workoutId: Long) = "workout_detail/$workoutId"
    }
    data object WorkoutEdit : Screen("workout_edit/{workoutId}") {
        fun createRoute(workoutId: Long) = "workout_edit/$workoutId"
    }
    data object ExerciseDetail : Screen("exercise_detail/{exerciseId}") {
        fun createRoute(exerciseId: Long) = "exercise_detail/$exerciseId"
    }
    data object ExerciseForm : Screen("exercise_form/{exerciseId}") {
        fun createRoute(exerciseId: Long? = null) = "exercise_form/${exerciseId ?: -1}"
    }
    data object Settings : Screen("settings")
}
