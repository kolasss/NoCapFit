package com.example.nocapfit.ui.screens.workoutdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nocapfit.data.db.relation.WorkoutWithExercises
import com.example.nocapfit.data.repository.ProgramRepository
import com.example.nocapfit.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutDetailViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val programRepository: ProgramRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workoutId: Long = checkNotNull(savedStateHandle["workoutId"])

    val workoutWithExercises: StateFlow<WorkoutWithExercises?> = workoutRepository
        .getWithExercisesFlow(workoutId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation: StateFlow<Boolean> = _showDeleteConfirmation.asStateFlow()

    private val _showSaveAsProgram = MutableStateFlow(false)
    val showSaveAsProgram: StateFlow<Boolean> = _showSaveAsProgram.asStateFlow()

    fun showDeleteConfirmation() {
        _showDeleteConfirmation.value = true
    }

    fun dismissDeleteConfirmation() {
        _showDeleteConfirmation.value = false
    }

    fun showSaveAsProgram() {
        _showSaveAsProgram.value = true
    }

    fun dismissSaveAsProgram() {
        _showSaveAsProgram.value = false
    }

    fun saveAsProgram(name: String) {
        val data = workoutWithExercises.value ?: return
        viewModelScope.launch {
            programRepository.createProgramFromWorkout(data, name)
            _showSaveAsProgram.value = false
        }
    }

    fun deleteWorkout(onDeleted: () -> Unit) {
        val workout = workoutWithExercises.value?.workout ?: return
        viewModelScope.launch {
            workoutRepository.delete(workout)
            onDeleted()
        }
    }
}
