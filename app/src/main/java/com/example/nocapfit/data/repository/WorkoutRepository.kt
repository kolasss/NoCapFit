package com.example.nocapfit.data.repository

import com.example.nocapfit.data.db.dao.WorkoutDao
import com.example.nocapfit.data.db.entity.Workout
import com.example.nocapfit.data.db.entity.WorkoutExercise
import com.example.nocapfit.data.db.entity.WorkoutSet
import com.example.nocapfit.data.db.relation.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao
) {
    suspend fun insert(workout: Workout): Long = workoutDao.insert(workout)
    suspend fun update(workout: Workout) = workoutDao.update(workout)
    suspend fun delete(workout: Workout) = workoutDao.delete(workout)
    suspend fun getById(id: Long): Workout? = workoutDao.getById(id)
    suspend fun getWithExercises(id: Long): WorkoutWithExercises? = workoutDao.getWithExercises(id)
    fun getWithExercisesFlow(id: Long): Flow<WorkoutWithExercises?> = workoutDao.getWithExercisesFlow(id)
    fun getLastWorkoutTimeByProgram() = workoutDao.getLastWorkoutTimeByProgram()
    suspend fun getLastFinishedByProgramId(programId: Long, excludeWorkoutId: Long) =
        workoutDao.getLastFinishedByProgramId(programId, excludeWorkoutId)
    fun getFinishedByExerciseId(exerciseId: Long): Flow<List<WorkoutWithExercises>> =
        workoutDao.getFinishedByExerciseId(exerciseId)
    fun getAllWithExercises(profileId: Long): Flow<List<WorkoutWithExercises>> = workoutDao.getAllWithExercises(
        profileId
    )
    suspend fun getLastFinishedByExerciseId(exerciseId: Long): WorkoutWithExercises? =
        workoutDao.getLastFinishedByExerciseId(exerciseId)
    suspend fun getActiveWorkout(): Workout? = workoutDao.getActiveWorkout()
    fun getActiveWorkoutFlow(): Flow<Workout?> = workoutDao.getActiveWorkoutFlow()
    suspend fun insertWorkoutExercise(exercise: WorkoutExercise): Long = workoutDao.insertWorkoutExercise(exercise)
    suspend fun swapExerciseOrder(first: WorkoutExercise, second: WorkoutExercise) =
        workoutDao.swapExerciseOrder(first, second)
    suspend fun insertWorkoutSet(set: WorkoutSet): Long = workoutDao.insertWorkoutSet(set)
    suspend fun updateWorkoutSet(set: WorkoutSet) = workoutDao.updateWorkoutSet(set)
    suspend fun getMaxOrderIndex(workoutId: Long): Int = workoutDao.getMaxOrderIndex(workoutId)
    suspend fun deleteWorkoutExercise(exerciseId: Long) = workoutDao.deleteWorkoutExercise(exerciseId)
    suspend fun getSetsForExercise(exerciseId: Long): List<WorkoutSet> = workoutDao.getSetsForExercise(exerciseId)
    suspend fun insertWorkoutWithExercises(
        workout: Workout,
        exercisesWithSets: List<Pair<WorkoutExercise, List<WorkoutSet>>>
    ): Long = workoutDao.insertWorkoutWithExercises(workout, exercisesWithSets)
    suspend fun saveWorkoutEdits(
        workout: Workout,
        exercisesWithSets: List<Pair<WorkoutExercise, List<WorkoutSet>>>
    ) = workoutDao.saveWorkoutEdits(workout, exercisesWithSets)
}
