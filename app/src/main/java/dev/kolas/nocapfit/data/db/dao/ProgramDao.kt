package dev.kolas.nocapfit.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.kolas.nocapfit.data.db.entity.Program
import dev.kolas.nocapfit.data.db.entity.ProgramExercise
import dev.kolas.nocapfit.data.db.entity.ProgramExerciseSet
import dev.kolas.nocapfit.data.db.relation.ProgramWithExercises
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
    @Query("SELECT * FROM programs WHERE profileId = :profileId ORDER BY id DESC")
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

    @Transaction
    suspend fun saveProgramWithExercises(
        program: Program,
        isUpdate: Boolean,
        exercises: List<Pair<ProgramExercise, List<ProgramExerciseSet>>>
    ): Long {
        val programId: Long
        if (isUpdate) {
            update(program)
            deleteExercisesForProgram(program.id)
            programId = program.id
        } else {
            programId = insert(program)
        }
        for ((exercise, sets) in exercises) {
            val peId = insertProgramExercise(exercise.copy(programId = programId))
            for (set in sets) {
                insertProgramExerciseSet(set.copy(programExerciseId = peId))
            }
        }
        return programId
    }
}
