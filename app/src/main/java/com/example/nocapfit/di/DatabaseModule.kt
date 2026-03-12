package com.example.nocapfit.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NoCapFitDatabase {
        return Room.databaseBuilder(
            context,
            NoCapFitDatabase::class.java,
            "nocapfit.db"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL("INSERT INTO profiles (name) VALUES ('Default')")
                }
            })
            .build()
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
