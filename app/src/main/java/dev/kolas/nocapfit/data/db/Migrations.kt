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

val MIGRATION_3_4 = Migration(3, 4) { db: SupportSQLiteDatabase ->
    db.execSQL("CREATE INDEX IF NOT EXISTS index_workouts_programId ON workouts(programId)")
    db.execSQL(
        "CREATE INDEX IF NOT EXISTS index_workout_exercises_exerciseId " +
            "ON workout_exercises(exerciseId)"
    )
}

// active_timers becomes "row exists = running": terminal (COMPLETED/CANCELLED) rows are dropped
// along with the status and notificationId columns.
val MIGRATION_4_5 = Migration(4, 5) { db: SupportSQLiteDatabase ->
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS active_timers_new (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "workoutId INTEGER NOT NULL, " +
            "workoutSetId INTEGER NOT NULL, " +
            "startedAtEpochMs INTEGER NOT NULL, " +
            "endAtEpochMs INTEGER NOT NULL)"
    )
    db.execSQL(
        "INSERT INTO active_timers_new (id, workoutId, workoutSetId, startedAtEpochMs, endAtEpochMs) " +
            "SELECT id, workoutId, workoutSetId, startedAtEpochMs, endAtEpochMs " +
            "FROM active_timers WHERE status = 'RUNNING'"
    )
    db.execSQL("DROP TABLE active_timers")
    db.execSQL("ALTER TABLE active_timers_new RENAME TO active_timers")
}
