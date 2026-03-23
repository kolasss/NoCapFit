package com.example.nocapfit.ui.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nocapfit.data.db.entity.Exercise
import com.example.nocapfit.data.repository.ExerciseRepository
import com.example.nocapfit.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExerciseListViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _profileId = MutableStateFlow<Long?>(null)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    val exercises: StateFlow<List<Exercise>> = combine(_profileId, _searchQuery) { profileId, query ->
        profileId to query
    }.flatMapLatest { (profileId, query) ->
        if (profileId == null) {
            flowOf(emptyList())
        } else if (query.isBlank()) {
            exerciseRepository.getAllByProfile(profileId)
        } else {
            exerciseRepository.searchByName(profileId, query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val profile = profileRepository.getDefault()
            _profileId.value = profile?.id
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun showAddDialog() {
        _showAddDialog.value = true
    }

    fun dismissAddDialog() {
        _showAddDialog.value = false
    }

    fun addExercise(name: String, description: String, tags: String) {
        val profileId = _profileId.value ?: return
        viewModelScope.launch {
            exerciseRepository.insert(
                Exercise(
                    profileId = profileId,
                    name = name.trim(),
                    description = description.trim(),
                    tags = tags.trim()
                )
            )
            _showAddDialog.value = false
        }
    }
}
