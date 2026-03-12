package com.example.nocapfit.ui.screens.workoutdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nocapfit.data.db.relation.WorkoutWithExercises
import com.example.nocapfit.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutDetailViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workoutId: Long = checkNotNull(savedStateHandle["workoutId"])

    private val _workoutWithExercises = MutableStateFlow<WorkoutWithExercises?>(null)
    val workoutWithExercises: StateFlow<WorkoutWithExercises?> = _workoutWithExercises.asStateFlow()

    init {
        viewModelScope.launch {
            _workoutWithExercises.value = workoutRepository.getWithExercises(workoutId)
        }
    }
}
