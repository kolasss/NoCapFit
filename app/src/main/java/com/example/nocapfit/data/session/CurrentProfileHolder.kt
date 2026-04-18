package com.example.nocapfit.data.session

import com.example.nocapfit.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentProfileHolder @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    private val _profileId = MutableStateFlow<Long?>(null)
    val profileId: StateFlow<Long?> = _profileId.asStateFlow()

    private val loadMutex = Mutex()

    suspend fun ensureLoaded(): Long? {
        _profileId.value?.let { return it }
        loadMutex.withLock {
            if (_profileId.value == null) {
                _profileId.value = profileRepository.getDefault()?.id
            }
        }
        return _profileId.value
    }
}
