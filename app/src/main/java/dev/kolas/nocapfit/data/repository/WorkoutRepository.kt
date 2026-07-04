package dev.kolas.nocapfit.data.repository

import dev.kolas.nocapfit.data.db.dao.ExerciseHistorySetRow
import dev.kolas.nocapfit.data.db.dao.PreviousCompletedSet
import dev.kolas.nocapfit.data.db.dao.WorkoutDao
import dev.kolas.nocapfit.data.db.entity.Workout
import dev.kolas.nocapfit.data.db.entity.WorkoutExercise
import dev.kolas.nocapfit.data.db.entity.WorkoutSet
import dev.kolas.nocapfit.data.db.relation.WorkoutWithExerciseNames
import dev.kolas.nocapfit.data.db.relation.WorkoutWithExercises
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
    fun getExerciseHistory(exerciseId: Long): Flow<List<ExerciseHistorySetRow>> =
        workoutDao.getExerciseHistory(exerciseId)
    fun getFinishedWithExerciseNames(profileId: Long): Flow<List<WorkoutWithExerciseNames>> =
        workoutDao.getFinishedWithExerciseNames(profileId)
    suspend fun getPreviousCompletedSets(exerciseIds: List<Long>): List<PreviousCompletedSet> =
        if (exerciseIds.isEmpty()) emptyList() else workoutDao.getPreviousCompletedSets(exerciseIds)
    suspend fun getActiveWorkout(): Workout? = workoutDao.getActiveWorkout()
    fun getActiveWorkoutFlow(): Flow<Workout?> = workoutDao.getActiveWorkoutFlow()
    suspend fun insertWorkoutExercise(exercise: WorkoutExercise): Long = workoutDao.insertWorkoutExercise(exercise)
    suspend fun updateWorkoutExerciseNote(id: Long, note: String?) =
        workoutDao.updateWorkoutExerciseNote(id, note)
    suspend fun swapExerciseOrder(first: WorkoutExercise, second: WorkoutExercise) =
        workoutDao.swapExerciseOrder(first, second)
    suspend fun insertWorkoutSet(set: WorkoutSet): Long = workoutDao.insertWorkoutSet(set)
    suspend fun insertExerciseWithDefaultSet(exercise: WorkoutExercise, defaultSet: WorkoutSet) =
        workoutDao.insertExerciseWithDefaultSet(exercise, defaultSet)
    suspend fun updateWorkoutSet(set: WorkoutSet) = workoutDao.updateWorkoutSet(set)
    suspend fun updateWorkoutSets(sets: List<WorkoutSet>) = workoutDao.updateWorkoutSets(sets)
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
