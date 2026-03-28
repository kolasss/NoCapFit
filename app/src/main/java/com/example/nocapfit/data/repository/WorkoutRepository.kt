package com.example.nocapfit.data.repository

import com.example.nocapfit.data.db.dao.WorkoutDao
import com.example.nocapfit.data.db.entity.Workout
import com.example.nocapfit.data.db.entity.WorkoutExercise
import com.example.nocapfit.data.db.entity.WorkoutSet
import com.example.nocapfit.data.db.relation.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

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
    fun getAllWithExercises(profileId: Long): Flow<List<WorkoutWithExercises>> = workoutDao.getAllWithExercises(
        profileId
    )
    suspend fun getActiveWorkout(): Workout? = workoutDao.getActiveWorkout()
    suspend fun insertWorkoutExercise(exercise: WorkoutExercise): Long = workoutDao.insertWorkoutExercise(exercise)
    suspend fun insertWorkoutSet(set: WorkoutSet): Long = workoutDao.insertWorkoutSet(set)
    suspend fun updateWorkoutSet(set: WorkoutSet) = workoutDao.updateWorkoutSet(set)
    suspend fun deleteWorkoutExercise(exerciseId: Long) = workoutDao.deleteWorkoutExercise(exerciseId)
    suspend fun getSetsForExercise(exerciseId: Long): List<WorkoutSet> = workoutDao.getSetsForExercise(exerciseId)
    suspend fun insertWorkoutWithExercises(
        workout: Workout,
        exercisesWithSets: List<Pair<WorkoutExercise, List<WorkoutSet>>>
    ): Long = workoutDao.insertWorkoutWithExercises(workout, exercisesWithSets)
}
