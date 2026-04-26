package dev.kolas.nocapfit.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.kolas.nocapfit.data.db.dao.ActiveTimerDao
import dev.kolas.nocapfit.data.db.dao.ExerciseDao
import dev.kolas.nocapfit.data.db.dao.ProfileDao
import dev.kolas.nocapfit.data.db.dao.ProgramDao
import dev.kolas.nocapfit.data.db.dao.WorkoutDao
import dev.kolas.nocapfit.data.db.entity.ActiveTimer
import dev.kolas.nocapfit.data.db.entity.Exercise
import dev.kolas.nocapfit.data.db.entity.Profile
import dev.kolas.nocapfit.data.db.entity.Program
import dev.kolas.nocapfit.data.db.entity.ProgramExercise
import dev.kolas.nocapfit.data.db.entity.ProgramExerciseSet
import dev.kolas.nocapfit.data.db.entity.Workout
import dev.kolas.nocapfit.data.db.entity.WorkoutExercise
import dev.kolas.nocapfit.data.db.entity.WorkoutSet

@Database(
    entities = [
        Profile::class,
        Exercise::class,
        Program::class,
        ProgramExercise::class,
        ProgramExerciseSet::class,
        Workout::class,
        WorkoutExercise::class,
        WorkoutSet::class,
        ActiveTimer::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class NoCapFitDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun programDao(): ProgramDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun activeTimerDao(): ActiveTimerDao
}
