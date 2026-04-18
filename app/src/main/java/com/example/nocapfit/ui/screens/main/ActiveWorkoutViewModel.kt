package com.example.nocapfit.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nocapfit.data.db.entity.Workout
import com.example.nocapfit.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    workoutRepository: WorkoutRepository
) : ViewModel() {
    val activeWorkout: StateFlow<Workout?> = workoutRepository.getActiveWorkoutFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
