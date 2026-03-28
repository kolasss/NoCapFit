package com.example.nocapfit.data.backup

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.nocapfit.data.db.NoCapFitDatabase
import com.example.nocapfit.data.db.dao.WorkoutDao
import com.example.nocapfit.data.db.entity.Workout
import com.example.nocapfit.di.DatabaseModule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

class BackupManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val database = mockk<NoCapFitDatabase>(relaxUnitFun = true)
    private val context = mockk<Context>(relaxed = true)
    private val contentResolver = mockk<ContentResolver>()
    private val sqliteDb = mockk<SupportSQLiteDatabase>()
    private val cursor = mockk<Cursor>(relaxUnitFun = true)
    private val workoutDao = mockk<WorkoutDao>()

    private lateinit var dbFile: File
    private lateinit var backupManager: BackupManager

    @Before
    fun setUp() {
        dbFile = File(tempFolder.root, DatabaseModule.DATABASE_NAME)
        dbFile.writeText("SQLite format 3\u0000test data here")

        every { context.contentResolver } returns contentResolver
        every { context.getDatabasePath(DatabaseModule.DATABASE_NAME) } returns dbFile
        every { database.openHelper.writableDatabase } returns sqliteDb
        every { sqliteDb.query("PRAGMA wal_checkpoint(TRUNCATE)") } returns cursor
        every { database.workoutDao() } returns workoutDao

        backupManager = BackupManager(database, context)
    }

    @Test
    fun exportDatabase_checkpointsWalBeforeCopying() = runTest {
        val outputStream = ByteArrayOutputStream()
        val uri = mockk<Uri>()
        every { contentResolver.openOutputStream(uri) } returns outputStream

        val result = backupManager.exportDatabase(uri)

        assertTrue(result.isSuccess)
        verify { sqliteDb.query("PRAGMA wal_checkpoint(TRUNCATE)") }
        verify { cursor.close() }
        verify { database.close() }
        assertTrue(outputStream.size() > 0)
    }

    @Test
    fun exportDatabase_returnsFailureOnNullOutputStream() = runTest {
        val uri = mockk<Uri>()
        every { contentResolver.openOutputStream(uri) } returns null

        val result = backupManager.exportDatabase(uri)

        assertTrue(result.isFailure)
    }

    @Test
    fun importDatabase_rejectsInvalidSqliteHeader() = runTest {
        val uri = mockk<Uri>()
        val invalidData = "This is not a database file".toByteArray()
        every { contentResolver.openInputStream(uri) } returns ByteArrayInputStream(invalidData)

        val result = backupManager.importDatabase(uri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun importDatabase_closesDatabaseBeforeCopying() = runTest {
        val uri = mockk<Uri>()
        val validHeader = "SQLite format 3\u0000".toByteArray() + ByteArray(100)
        every { contentResolver.openInputStream(uri) } returns ByteArrayInputStream(validHeader)

        val result = backupManager.importDatabase(uri)

        assertTrue(result.isSuccess)
        verify { database.close() }
        assertTrue(dbFile.readBytes().contentEquals(validHeader))
    }

    @Test
    fun importDatabase_deletesWalAndShmFiles() = runTest {
        val walFile = File(dbFile.path + "-wal")
        val shmFile = File(dbFile.path + "-shm")
        walFile.writeText("wal data")
        shmFile.writeText("shm data")

        val uri = mockk<Uri>()
        val validHeader = "SQLite format 3\u0000".toByteArray() + ByteArray(100)
        every { contentResolver.openInputStream(uri) } returns ByteArrayInputStream(validHeader)

        backupManager.importDatabase(uri)

        assertTrue(!walFile.exists())
        assertTrue(!shmFile.exists())
    }

    @Test
    fun hasActiveWorkout_returnsTrueWhenWorkoutExists() = runTest {
        val workout = mockk<Workout>()
        coEvery { workoutDao.getActiveWorkout() } returns workout

        val result = backupManager.hasActiveWorkout()

        assertTrue(result)
    }

    @Test
    fun hasActiveWorkout_returnsFalseWhenNoWorkout() = runTest {
        coEvery { workoutDao.getActiveWorkout() } returns null

        val result = backupManager.hasActiveWorkout()

        assertTrue(!result)
    }
}
