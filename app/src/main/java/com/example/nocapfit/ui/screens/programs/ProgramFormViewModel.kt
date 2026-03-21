package com.example.nocapfit.ui.screens.programs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nocapfit.data.db.entity.Exercise
import com.example.nocapfit.data.db.entity.Program
import com.example.nocapfit.data.db.entity.ProgramExercise
import com.example.nocapfit.data.db.entity.ProgramExerciseSet
import com.example.nocapfit.data.repository.ExerciseRepository
import com.example.nocapfit.data.repository.ProfileRepository
import com.example.nocapfit.data.repository.ProgramRepository
import com.example.nocapfit.ui.components.parseMmSsToSeconds
import com.example.nocapfit.ui.components.secondsToMmSsDigits
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

data class SetEntry(
    val weight: String = "0.0",
    val reps: String = "0",
    val restTimeSeconds: String = "100"
)

data class ExerciseEntry(
    val exercise: Exercise,
    val sets: List<SetEntry> = listOf(SetEntry())
)

data class ProgramFormUiState(
    val name: String = "",
    val exercises: List<ExerciseEntry> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val nameError: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProgramFormViewModel @Inject constructor(
    private val programRepository: ProgramRepository,
    private val exerciseRepository: ExerciseRepository,
    private val profileRepository: ProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val programId: Long = savedStateHandle.get<Long>("programId") ?: -1L
    val isEditing: Boolean = programId > 0

    private val _profileId = MutableStateFlow<Long?>(null)

    private val _uiState = MutableStateFlow(ProgramFormUiState())
    val uiState: StateFlow<ProgramFormUiState> = _uiState.asStateFlow()

    val availableExercises: StateFlow<List<Exercise>> = _profileId.flatMapLatest { profileId ->
        if (profileId == null) flowOf(emptyList()) else exerciseRepository.getAllByProfile(profileId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val profile = profileRepository.getDefault()
            _profileId.value = profile?.id

            if (isEditing) {
                val programWithExercises = programRepository.getProgramWithExercises(programId)
                if (programWithExercises != null) {
                    val exerciseEntries = programWithExercises.exercises
                        .sortedBy { it.programExercise.orderIndex }
                        .map { peWithSets ->
                            ExerciseEntry(
                                exercise = peWithSets.exercise,
                                sets = peWithSets.sets
                                    .sortedBy { it.setIndex }
                                    .map { set ->
                                        SetEntry(
                                            weight = formatWeight(set.weightThousandths),
                                            reps = set.reps.toString(),
                                            restTimeSeconds = secondsToMmSsDigits(set.restTimeSeconds)
                                        )
                                    }
                                    .ifEmpty { listOf(SetEntry()) }
                            )
                        }
                    _uiState.value = ProgramFormUiState(
                        name = programWithExercises.program.name,
                        exercises = exerciseEntries,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, nameError = null)
    }

    fun addExercise(exercise: Exercise) {
        val current = _uiState.value
        _uiState.value = current.copy(
            exercises = current.exercises + ExerciseEntry(exercise = exercise)
        )
    }

    fun removeExercise(index: Int) {
        val current = _uiState.value
        _uiState.value = current.copy(
            exercises = current.exercises.toMutableList().apply { removeAt(index) }
        )
    }

    fun addSet(exerciseIndex: Int) {
        val current = _uiState.value
        val exercises = current.exercises.toMutableList()
        val entry = exercises[exerciseIndex]
        exercises[exerciseIndex] = entry.copy(sets = entry.sets + SetEntry())
        _uiState.value = current.copy(exercises = exercises)
    }

    fun removeSet(exerciseIndex: Int, setIndex: Int) {
        val current = _uiState.value
        val exercises = current.exercises.toMutableList()
        val entry = exercises[exerciseIndex]
        if (entry.sets.size > 1) {
            exercises[exerciseIndex] = entry.copy(
                sets = entry.sets.toMutableList().apply { removeAt(setIndex) }
            )
            _uiState.value = current.copy(exercises = exercises)
        }
    }

    fun updateSet(exerciseIndex: Int, setIndex: Int, setEntry: SetEntry) {
        val current = _uiState.value
        val exercises = current.exercises.toMutableList()
        val entry = exercises[exerciseIndex]
        val sets = entry.sets.toMutableList()
        sets[setIndex] = setEntry
        exercises[exerciseIndex] = entry.copy(sets = sets)
        _uiState.value = current.copy(exercises = exercises)
    }

    suspend fun save(): Boolean {
        val state = _uiState.value
        val profileId = _profileId.value ?: return false

        if (state.name.isBlank()) {
            _uiState.value = state.copy(nameError = "Program name is required")
            return false
        }
        if (state.exercises.isEmpty()) {
            _uiState.value = state.copy(nameError = "Add at least one exercise")
            return false
        }

        _uiState.value = state.copy(isSaving = true)

        return try {
            val savedProgramId: Long
            if (isEditing) {
                programRepository.update(
                    Program(id = programId, profileId = profileId, name = state.name.trim())
                )
                programRepository.deleteExercisesForProgram(programId)
                savedProgramId = programId
            } else {
                savedProgramId = programRepository.insert(
                    Program(profileId = profileId, name = state.name.trim())
                )
            }

            state.exercises.forEachIndexed { exerciseIndex, exerciseEntry ->
                val programExerciseId = programRepository.insertProgramExercise(
                    ProgramExercise(
                        programId = savedProgramId,
                        exerciseId = exerciseEntry.exercise.id,
                        orderIndex = exerciseIndex
                    )
                )
                exerciseEntry.sets.forEachIndexed { setIndex, setEntry ->
                    programRepository.insertProgramExerciseSet(
                        ProgramExerciseSet(
                            programExerciseId = programExerciseId,
                            setIndex = setIndex,
                            weightThousandths = parseWeight(setEntry.weight),
                            reps = setEntry.reps.toIntOrNull() ?: 0,
                            restTimeSeconds = parseMmSsToSeconds(setEntry.restTimeSeconds)
                        )
                    )
                }
            }
            true
        } catch (_: Exception) {
            _uiState.value = _uiState.value.copy(isSaving = false)
            false
        }
    }

    companion object {
        fun formatWeight(thousandths: Int): String {
            val value = thousandths / 1000.0
            val formatted = if (value == value.toLong().toDouble()) {
                "${value.toLong()}.0"
            } else {
                value.toBigDecimal().stripTrailingZeros().toPlainString().let { s ->
                    if ('.' !in s) "$s.0" else s
                }
            }
            return formatted
        }

        fun parseWeight(input: String): Int {
            val value = input.toDoubleOrNull() ?: 0.0
            return (value * 1000).roundToInt()
        }
    }
}
