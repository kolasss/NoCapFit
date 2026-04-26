package dev.kolas.nocapfit.ui.screens.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kolas.nocapfit.data.db.entity.Exercise
import dev.kolas.nocapfit.data.db.relation.WorkoutWithExercises
import dev.kolas.nocapfit.data.repository.ExerciseRepository
import dev.kolas.nocapfit.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    workoutRepository: WorkoutRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val exerciseId: Long = checkNotNull(savedStateHandle["exerciseId"])

    val exercise: StateFlow<Exercise?> = exerciseRepository
        .getByIdFlow(exerciseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val exerciseHistory: StateFlow<List<WorkoutWithExercises>> = workoutRepository
        .getFinishedByExerciseId(exerciseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun selectTab(index: Int) { _selectedTab.value = index }

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation: StateFlow<Boolean> = _showDeleteConfirmation.asStateFlow()

    fun showDeleteConfirmation() {
        _showDeleteConfirmation.value = true
    }

    fun dismissDeleteConfirmation() {
        _showDeleteConfirmation.value = false
    }

    fun deleteExercise(onDeleted: () -> Unit) {
        val exercise = exercise.value ?: return
        viewModelScope.launch {
            exerciseRepository.delete(exercise)
            onDeleted()
        }
    }
}
