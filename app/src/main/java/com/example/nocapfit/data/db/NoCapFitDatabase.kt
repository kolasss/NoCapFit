package com.example.nocapfit.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.nocapfit.data.db.dao.ActiveTimerDao
import com.example.nocapfit.data.db.dao.ExerciseDao
import com.example.nocapfit.data.db.dao.ProfileDao
import com.example.nocapfit.data.db.dao.ProgramDao
import com.example.nocapfit.data.db.dao.WorkoutDao
import com.example.nocapfit.data.db.entity.ActiveTimer
import com.example.nocapfit.data.db.entity.Exercise
import com.example.nocapfit.data.db.entity.Profile
import com.example.nocapfit.data.db.entity.Program
import com.example.nocapfit.data.db.entity.ProgramExercise
import com.example.nocapfit.data.db.entity.ProgramExerciseSet
import com.example.nocapfit.data.db.entity.Workout
import com.example.nocapfit.data.db.entity.WorkoutExercise
import com.example.nocapfit.data.db.entity.WorkoutSet

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
    version = 1,
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
