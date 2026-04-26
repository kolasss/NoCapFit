package dev.kolas.nocapfit.ui.screens.programs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kolas.nocapfit.data.db.relation.ProgramWithExercises
import dev.kolas.nocapfit.data.repository.ProgramRepository
import dev.kolas.nocapfit.data.session.CurrentProfileHolder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProgramListViewModel @Inject constructor(
    private val programRepository: ProgramRepository,
    private val currentProfileHolder: CurrentProfileHolder
) : ViewModel() {

    val programs: StateFlow<List<ProgramWithExercises>> = currentProfileHolder.profileId
        .flatMapLatest { profileId ->
            if (profileId == null) {
                flowOf(emptyList())
            } else {
                programRepository.getAllWithExercises(profileId)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch { currentProfileHolder.ensureLoaded() }
    }

    fun deleteProgram(programWithExercises: ProgramWithExercises) {
        viewModelScope.launch {
            programRepository.delete(programWithExercises.program)
        }
    }

    fun copyProgram(programId: Long) {
        viewModelScope.launch {
            programRepository.copyProgram(programId)
        }
    }
}
