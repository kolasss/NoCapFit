package com.example.nocapfit.data.repository

import com.example.nocapfit.data.db.dao.ProgramDao
import com.example.nocapfit.data.db.entity.Program
import com.example.nocapfit.data.db.entity.ProgramExercise
import com.example.nocapfit.data.db.entity.ProgramExerciseSet
import com.example.nocapfit.data.db.relation.ProgramWithExercises
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgramRepository @Inject constructor(
    private val programDao: ProgramDao
) {
    suspend fun insert(program: Program): Long = programDao.insert(program)
    suspend fun update(program: Program) = programDao.update(program)
    suspend fun delete(program: Program) = programDao.delete(program)
    suspend fun getById(id: Long): Program? = programDao.getById(id)
    fun getAllWithExercises(profileId: Long): Flow<List<ProgramWithExercises>> = programDao.getAllWithExercises(profileId)
    suspend fun getProgramWithExercises(id: Long): ProgramWithExercises? = programDao.getProgramWithExercises(id)
    suspend fun insertProgramExercise(programExercise: ProgramExercise): Long = programDao.insertProgramExercise(programExercise)
    suspend fun insertProgramExerciseSet(set: ProgramExerciseSet): Long = programDao.insertProgramExerciseSet(set)
    suspend fun deleteExercisesForProgram(programId: Long) = programDao.deleteExercisesForProgram(programId)
}
