package com.example.nocapfit.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.nocapfit.data.db.DEFAULT_EXERCISES
import com.example.nocapfit.data.db.DEFAULT_PROGRAMS
import com.example.nocapfit.data.db.NoCapFitDatabase
import com.example.nocapfit.data.db.dao.ActiveTimerDao
import com.example.nocapfit.data.db.dao.ExerciseDao
import com.example.nocapfit.data.db.dao.ProfileDao
import com.example.nocapfit.data.db.dao.ProgramDao
import com.example.nocapfit.data.db.dao.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    const val DATABASE_NAME = "nocapfit.db"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NoCapFitDatabase {
        return Room.databaseBuilder(
            context,
            NoCapFitDatabase::class.java,
            DATABASE_NAME
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL("INSERT INTO profiles (name) VALUES ('Default')")
                    DEFAULT_EXERCISES.forEach { exercise ->
                        db.execSQL(
                            "INSERT INTO exercises (profileId, name, description, tags) VALUES (?, ?, ?, ?)",
                            arrayOf<Any>(1L, exercise.name, exercise.description, exercise.tags)
                        )
                    }
                    seedDefaultPrograms(db)
                }
            })
            .build()
    }

    private fun seedDefaultPrograms(db: SupportSQLiteDatabase) {
        DEFAULT_PROGRAMS.forEach { program ->
            db.execSQL(
                "INSERT INTO programs (profileId, name) VALUES (?, ?)",
                arrayOf<Any>(1L, program.name)
            )
            val programId = db.query("SELECT last_insert_rowid()").use { cursor ->
                cursor.moveToFirst()
                cursor.getLong(0)
            }

            program.exercises.forEachIndexed { exerciseIndex, exercise ->
                db.execSQL(
                    "INSERT INTO program_exercises (programId, exerciseId, orderIndex) " +
                        "VALUES (?, (SELECT id FROM exercises WHERE name = ? AND profileId = 1), ?)",
                    arrayOf<Any>(programId, exercise.exerciseName, exerciseIndex)
                )
                val programExerciseId = db.query("SELECT last_insert_rowid()").use { cursor ->
                    cursor.moveToFirst()
                    cursor.getLong(0)
                }

                for (setIndex in 0 until exercise.sets) {
                    db.execSQL(
                        "INSERT INTO program_exercise_sets " +
                            "(programExerciseId, setIndex, weightThousandths, reps, restTimeSeconds) " +
                            "VALUES (?, ?, ?, ?, ?)",
                        arrayOf<Any>(
                            programExerciseId,
                            setIndex,
                            0,
                            exercise.reps,
                            exercise.restTimeSeconds
                        )
                    )
                }
            }
        }
    }

    @Provides
    fun provideProfileDao(db: NoCapFitDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideExerciseDao(db: NoCapFitDatabase): ExerciseDao = db.exerciseDao()

    @Provides
    fun provideProgramDao(db: NoCapFitDatabase): ProgramDao = db.programDao()

    @Provides
    fun provideWorkoutDao(db: NoCapFitDatabase): WorkoutDao = db.workoutDao()

    @Provides
    fun provideActiveTimerDao(db: NoCapFitDatabase): ActiveTimerDao = db.activeTimerDao()
}
