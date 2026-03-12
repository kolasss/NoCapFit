package com.example.nocapfit.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.nocapfit.data.db.entity.Profile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Insert
    suspend fun insert(profile: Profile): Long

    @Query("SELECT * FROM profiles ORDER BY id ASC LIMIT 1")
    suspend fun getDefault(): Profile?

    @Query("SELECT * FROM profiles ORDER BY id ASC")
    fun getAll(): Flow<List<Profile>>
}
