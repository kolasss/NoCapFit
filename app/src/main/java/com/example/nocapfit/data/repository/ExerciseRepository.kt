package com.example.nocapfit.data.repository

import com.example.nocapfit.data.db.dao.ExerciseDao
import com.example.nocapfit.data.db.entity.Exercise
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepository @Inject constructor(
    private val exerciseDao: ExerciseDao
) {
    suspend fun insert(exercise: Exercise): Long = exerciseDao.insert(exercise)
    suspend fun update(exercise: Exercise) = exerciseDao.update(exercise)
    suspend fun delete(exercise: Exercise) = exerciseDao.delete(exercise)
    suspend fun getById(id: Long): Exercise? = exerciseDao.getById(id)
    fun getByIdFlow(id: Long): Flow<Exercise?> = exerciseDao.getByIdFlow(id)
    fun getAllByProfile(profileId: Long): Flow<List<Exercise>> = exerciseDao.getAllByProfile(profileId)
    fun searchByName(profileId: Long, query: String): Flow<List<Exercise>> = exerciseDao.searchByName(profileId, query)
}
