package com.example.nocapfit.data.backup

import android.content.Context
import android.net.Uri
import com.example.nocapfit.data.db.NoCapFitDatabase
import com.example.nocapfit.di.DatabaseModule
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val SQLITE_HEADER = "SQLite format 3\u0000"

@Singleton
class BackupManager @Inject constructor(
    private val database: NoCapFitDatabase,
    @param:ApplicationContext private val context: Context
) {

    suspend fun exportDatabase(destinationUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // VACUUM INTO creates a complete standalone copy including all WAL data
            // without closing the database, so Flow-based queries keep working
            val tempFile = File(context.cacheDir, "backup_export.db")
            try {
                tempFile.delete()
                database.openHelper.writableDatabase
                    .execSQL("VACUUM INTO '${tempFile.absolutePath}'")

                val outputStream = context.contentResolver.openOutputStream(destinationUri)
                    ?: throw IOException("Cannot open output stream")

                outputStream.use { out ->
                    tempFile.inputStream().use { input ->
                        input.copyTo(out)
                    }
                }
                Unit
            } finally {
                tempFile.delete()
            }
        }
    }

    suspend fun validateBackup(sourceUri: Uri): Result<ByteArray> = withContext(Dispatchers.IO) {
        runCatching {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: throw IOException("Cannot open input stream")

            val bytes = inputStream.use { it.readBytes() }

            require(
                bytes.size >= SQLITE_HEADER.length &&
                    String(bytes, 0, SQLITE_HEADER.length, Charsets.US_ASCII) == SQLITE_HEADER
            ) { "Not a valid database file" }

            bytes
        }
    }

    fun applyBackup(bytes: ByteArray) {
        database.close()

        val dbFile = context.getDatabasePath(DatabaseModule.DATABASE_NAME)
        File(dbFile.path + "-wal").delete()
        File(dbFile.path + "-shm").delete()

        dbFile.outputStream().use { out ->
            out.write(bytes)
        }
    }

    suspend fun hasActiveWorkout(): Boolean = withContext(Dispatchers.IO) {
        database.workoutDao().getActiveWorkout() != null
    }
}
