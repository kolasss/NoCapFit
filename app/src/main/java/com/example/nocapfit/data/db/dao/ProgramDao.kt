package com.example.nocapfit.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.nocapfit.data.db.entity.Program
import com.example.nocapfit.data.db.entity.ProgramExercise
import com.example.nocapfit.data.db.entity.ProgramExerciseSet
import com.example.nocapfit.data.db.relation.ProgramWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {
    @Insert
    suspend fun insert(program: Program): Long

    @Update
    suspend fun update(program: Program)

    @Delete
    suspend fun delete(program: Program)

    @Query("SELECT * FROM programs WHERE id = :id")
    suspend fun getById(id: Long): Program?

    @Transaction
    @Query("SELECT * FROM programs WHERE profileId = :profileId ORDER BY name ASC")
    fun getAllWithExercises(profileId: Long): Flow<List<ProgramWithExercises>>

    @Transaction
    @Query("SELECT * FROM programs WHERE id = :id")
    suspend fun getProgramWithExercises(id: Long): ProgramWithExercises?

    @Insert
    suspend fun insertProgramExercise(programExercise: ProgramExercise): Long

    @Insert
    suspend fun insertProgramExerciseSet(set: ProgramExerciseSet): Long

    @Query("DELETE FROM program_exercises WHERE programId = :programId")
    suspend fun deleteExercisesForProgram(programId: Long)
}
