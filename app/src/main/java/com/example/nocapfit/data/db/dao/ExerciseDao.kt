package com.example.nocapfit.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.nocapfit.data.db.entity.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert
    suspend fun insert(exercise: Exercise): Long

    @Update
    suspend fun update(exercise: Exercise)

    @Delete
    suspend fun delete(exercise: Exercise)

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): Exercise?

    @Query("SELECT * FROM exercises WHERE profileId = :profileId ORDER BY name ASC")
    fun getAllByProfile(profileId: Long): Flow<List<Exercise>>

    @Query(
        "SELECT * FROM exercises WHERE profileId = :profileId AND " +
            "(name LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') ORDER BY name ASC"
    )
    fun searchByName(profileId: Long, query: String): Flow<List<Exercise>>
}
