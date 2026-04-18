package com.example.nocapfit.ui.screens.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nocapfit.data.db.entity.Exercise
import com.example.nocapfit.data.repository.ExerciseRepository
import com.example.nocapfit.data.session.CurrentProfileHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseFormViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val currentProfileHolder: CurrentProfileHolder,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val exerciseId: Long = checkNotNull(savedStateHandle["exerciseId"])
    val isEditMode: Boolean = exerciseId != -1L

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _tags = MutableStateFlow("")
    val tags: StateFlow<String> = _tags.asStateFlow()

    init {
        viewModelScope.launch {
            currentProfileHolder.ensureLoaded()
            if (isEditMode) {
                val exercise = exerciseRepository.getById(exerciseId) ?: return@launch
                _name.value = exercise.name
                _description.value = exercise.description
                _tags.value = exercise.tags
            }
        }
    }

    fun updateName(value: String) { _name.value = value }
    fun updateDescription(value: String) { _description.value = value }
    fun updateTags(value: String) { _tags.value = value }

    fun save(onSaved: () -> Unit) {
        val name = _name.value.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            if (isEditMode) {
                val existing = exerciseRepository.getById(exerciseId) ?: return@launch
                exerciseRepository.update(
                    existing.copy(
                        name = name,
                        description = _description.value.trim(),
                        tags = _tags.value.trim()
                    )
                )
            } else {
                val profileId = currentProfileHolder.ensureLoaded() ?: return@launch
                exerciseRepository.insert(
                    Exercise(
                        profileId = profileId,
                        name = name,
                        description = _description.value.trim(),
                        tags = _tags.value.trim()
                    )
                )
            }
            onSaved()
        }
    }
}
