package com.example.nocapfit.data.repository

import com.example.nocapfit.data.db.dao.ProfileDao
import com.example.nocapfit.data.db.entity.Profile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao
) {
    suspend fun insert(profile: Profile): Long = profileDao.insert(profile)
    suspend fun getDefault(): Profile? = profileDao.getDefault()
}
