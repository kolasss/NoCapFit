package com.example.nocapfit.ui.screens.workouthistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nocapfit.data.db.relation.WorkoutWithExercises
import com.example.nocapfit.data.repository.WorkoutRepository
import com.example.nocapfit.data.session.CurrentProfileHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkoutHistoryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val currentProfileHolder: CurrentProfileHolder
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val completedWorkouts: StateFlow<List<WorkoutWithExercises>> = currentProfileHolder.profileId
        .flatMapLatest { profileId ->
            if (profileId == null) {
                flowOf(emptyList())
            } else {
                workoutRepository.getAllWithExercises(profileId)
            }
        }
        .onEach { _isLoading.value = false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasActiveWorkout: StateFlow<Boolean> = workoutRepository.getActiveWorkoutFlow()
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        viewModelScope.launch { currentProfileHolder.ensureLoaded() }
    }
}
