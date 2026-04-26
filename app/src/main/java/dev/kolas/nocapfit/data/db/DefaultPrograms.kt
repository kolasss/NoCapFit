package dev.kolas.nocapfit.data.db

data class DefaultProgramData(
    val name: String,
    val exercises: List<DefaultProgramExerciseData>,
)

data class DefaultProgramExerciseData(
    val exerciseName: String,
    val sets: Int,
    val reps: Int,
    val restTimeSeconds: Int,
)

val DEFAULT_PROGRAMS: List<DefaultProgramData> = listOf(
    DefaultProgramData(
        name = "Day 1: Push (Chest, Shoulders, Triceps)",
        exercises = listOf(
            DefaultProgramExerciseData("Barbell Bench Press", sets = 4, reps = 8, restTimeSeconds = 150),
            DefaultProgramExerciseData("Overhead Barbell Press", sets = 3, reps = 10, restTimeSeconds = 120),
            DefaultProgramExerciseData("Incline Dumbbell Press", sets = 3, reps = 12, restTimeSeconds = 90),
            DefaultProgramExerciseData("Dumbbell Lateral Raise", sets = 3, reps = 15, restTimeSeconds = 90),
            DefaultProgramExerciseData("Tricep Rope Pushdown", sets = 3, reps = 15, restTimeSeconds = 90),
        )
    ),
    DefaultProgramData(
        name = "Day 2: Pull (Back, Biceps, Core)",
        exercises = listOf(
            DefaultProgramExerciseData("Lat Pulldown", sets = 4, reps = 10, restTimeSeconds = 120),
            DefaultProgramExerciseData("Seated Cable Row", sets = 3, reps = 12, restTimeSeconds = 90),
            DefaultProgramExerciseData("Face Pulls", sets = 3, reps = 20, restTimeSeconds = 60),
            DefaultProgramExerciseData("Barbell Bicep Curl", sets = 3, reps = 12, restTimeSeconds = 90),
            DefaultProgramExerciseData("Bird-Dog", sets = 3, reps = 10, restTimeSeconds = 60),
        )
    ),
    DefaultProgramData(
        name = "Day 3: Legs (Quads, Hamstrings, Glutes)",
        exercises = listOf(
            DefaultProgramExerciseData("Barbell Back Squat", sets = 4, reps = 8, restTimeSeconds = 180),
            DefaultProgramExerciseData("Romanian Deadlift (RDL)", sets = 3, reps = 10, restTimeSeconds = 120),
            DefaultProgramExerciseData("Leg Press", sets = 3, reps = 12, restTimeSeconds = 90),
            DefaultProgramExerciseData("Seated Calf Raise", sets = 4, reps = 15, restTimeSeconds = 60),
            DefaultProgramExerciseData("Plank", sets = 3, reps = 1, restTimeSeconds = 60),
        )
    ),
)
