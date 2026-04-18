package com.example.nocapfit.ui.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nocapfit.data.db.entity.Exercise
import com.example.nocapfit.data.repository.ExerciseRepository
import com.example.nocapfit.data.session.CurrentProfileHolder
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
    private val currentProfileHolder: CurrentProfileHolder
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val exercises: StateFlow<List<Exercise>> =
        combine(currentProfileHolder.profileId, _searchQuery) { profileId, query ->
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
        viewModelScope.launch { currentProfileHolder.ensureLoaded() }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
