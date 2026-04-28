package dev.kolas.nocapfit.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = Migration(1, 2) { db: SupportSQLiteDatabase ->
    db.execSQL("ALTER TABLE workouts ADD COLUMN programId INTEGER DEFAULT NULL")
}

val MIGRATION_2_3 = Migration(2, 3) { db: SupportSQLiteDatabase ->
    db.execSQL("ALTER TABLE program_exercises ADD COLUMN note TEXT DEFAULT NULL")
    db.execSQL("ALTER TABLE workout_exercises ADD COLUMN note TEXT DEFAULT NULL")
}
