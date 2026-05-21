package dev.kolas.nocapfit.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.kolas.nocapfit.data.db.entity.Workout
import dev.kolas.nocapfit.data.db.entity.WorkoutExercise
import dev.kolas.nocapfit.data.db.entity.WorkoutSet
import dev.kolas.nocapfit.data.db.relation.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions")
@Dao
interface WorkoutDao {
    @Insert
    suspend fun insert(workout: Workout): Long

    @Update
    suspend fun update(workout: Workout)

    @Delete
    suspend fun delete(workout: Workout)

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getById(id: Long): Workout?

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWithExercises(id: Long): WorkoutWithExercises?

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :id")
    fun getWithExercisesFlow(id: Long): Flow<WorkoutWithExercises?>

    @Transaction
    @Query("SELECT * FROM workouts WHERE profileId = :profileId ORDER BY startTime DESC")
    fun getAllWithExercises(profileId: Long): Flow<List<WorkoutWithExercises>>

    @Transaction
    @Query(
        "SELECT DISTINCT w.* FROM workouts w " +
            "INNER JOIN workout_exercises we ON w.id = we.workoutId " +
            "WHERE we.exerciseId = :exerciseId AND w.endTime IS NOT NULL " +
            "ORDER BY w.startTime DESC"
    )
    fun getFinishedByExerciseId(exerciseId: Long): Flow<List<WorkoutWithExercises>>

    @Query(
        "SELECT programId, MAX(startTime) as lastTime FROM workouts " +
            "WHERE endTime IS NOT NULL AND programId IS NOT NULL " +
            "GROUP BY programId"
    )
    fun getLastWorkoutTimeByProgram(): Flow<List<ProgramLastWorkout>>

    @Transaction
    @Query(
        "SELECT * FROM workouts " +
            "WHERE programId = :programId AND endTime IS NOT NULL AND id != :excludeWorkoutId " +
            "ORDER BY startTime DESC LIMIT 1"
    )
    suspend fun getLastFinishedByProgramId(programId: Long, excludeWorkoutId: Long): WorkoutWithExercises?

    @Query(
        "SELECT we.exerciseId AS exerciseId, ws.setIndex AS setIndex, " +
            "ws.weightThousandths AS weightThousandths, ws.reps AS reps " +
            "FROM workouts w " +
            "INNER JOIN workout_exercises we ON we.workoutId = w.id " +
            "INNER JOIN workout_sets ws ON ws.workoutExerciseId = we.id " +
            "WHERE we.exerciseId IN (:exerciseIds) " +
            "AND w.endTime IS NOT NULL " +
            "AND ws.completed = 1 " +
            "AND w.startTime = (" +
            "  SELECT MAX(w2.startTime) FROM workouts w2 " +
            "  INNER JOIN workout_exercises we2 ON we2.workoutId = w2.id " +
            "  WHERE we2.exerciseId = we.exerciseId AND w2.endTime IS NOT NULL" +
            ")"
    )
    suspend fun getPreviousCompletedSets(exerciseIds: List<Long>): List<PreviousCompletedSet>

    @Query("SELECT * FROM workouts WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveWorkout(): Workout?

    @Query("SELECT * FROM workouts WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    fun getActiveWorkoutFlow(): Flow<Workout?>

    @Insert
    suspend fun insertWorkoutExercise(exercise: WorkoutExercise): Long

    @Insert
    suspend fun insertWorkoutSet(set: WorkoutSet): Long

    @Update
    suspend fun updateWorkoutExercise(exercise: WorkoutExercise)

    @Query("UPDATE workout_exercises SET note = :note WHERE id = :id")
    suspend fun updateWorkoutExerciseNote(id: Long, note: String?)

    @Transaction
    suspend fun swapExerciseOrder(first: WorkoutExercise, second: WorkoutExercise) {
        updateWorkoutExercise(first)
        updateWorkoutExercise(second)
    }

    @Update
    suspend fun updateWorkoutSet(set: WorkoutSet)

    @Update
    suspend fun updateWorkoutSets(sets: List<WorkoutSet>)

    @Query("DELETE FROM workout_sets WHERE id = :setId")
    suspend fun deleteWorkoutSet(setId: Long)

    @Query("DELETE FROM workout_exercises WHERE id = :exerciseId")
    suspend fun deleteWorkoutExercise(exerciseId: Long)

    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun deleteExercisesForWorkout(workoutId: Long)

    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun getMaxOrderIndex(workoutId: Long): Int

    @Query("SELECT * FROM workout_sets WHERE workoutExerciseId = :exerciseId ORDER BY setIndex ASC")
    suspend fun getSetsForExercise(exerciseId: Long): List<WorkoutSet>

    /**
     * Inserts [exercise] and its first set in one transaction. [defaultSet]'s `workoutExerciseId`
     * is ignored — it is assigned from the row id of the inserted [exercise] (mirrors
     * [insertWorkoutWithExercises]/[saveWorkoutEdits], where child FKs are set inside the transaction).
     */
    @Transaction
    suspend fun insertExerciseWithDefaultSet(exercise: WorkoutExercise, defaultSet: WorkoutSet) {
        val weId = insertWorkoutExercise(exercise)
        insertWorkoutSet(defaultSet.copy(workoutExerciseId = weId))
    }

    @Transaction
    suspend fun insertWorkoutWithExercises(
        workout: Workout,
        exercisesWithSets: List<Pair<WorkoutExercise, List<WorkoutSet>>>
    ): Long {
        val workoutId = insert(workout)
        for ((exercise, sets) in exercisesWithSets) {
            val weId = insertWorkoutExercise(exercise.copy(workoutId = workoutId))
            for (set in sets) {
                insertWorkoutSet(set.copy(workoutExerciseId = weId))
            }
        }
        return workoutId
    }

    @Transaction
    suspend fun saveWorkoutEdits(
        workout: Workout,
        exercisesWithSets: List<Pair<WorkoutExercise, List<WorkoutSet>>>
    ) {
        update(workout)
        deleteExercisesForWorkout(workout.id)
        for ((exercise, sets) in exercisesWithSets) {
            val weId = insertWorkoutExercise(exercise.copy(workoutId = workout.id))
            for (set in sets) {
                insertWorkoutSet(set.copy(workoutExerciseId = weId))
            }
        }
    }
}
