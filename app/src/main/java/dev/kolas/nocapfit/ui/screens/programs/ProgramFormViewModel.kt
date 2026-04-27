package dev.kolas.nocapfit.ui.screens.programs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kolas.nocapfit.data.db.entity.Exercise
import dev.kolas.nocapfit.data.db.entity.Program
import dev.kolas.nocapfit.data.db.entity.ProgramExercise
import dev.kolas.nocapfit.data.db.entity.ProgramExerciseSet
import dev.kolas.nocapfit.data.repository.ExerciseRepository
import dev.kolas.nocapfit.data.repository.ProgramRepository
import dev.kolas.nocapfit.data.repository.WorkoutRepository
import dev.kolas.nocapfit.data.session.CurrentProfileHolder
import dev.kolas.nocapfit.ui.components.parseMmSsToSeconds
import dev.kolas.nocapfit.ui.components.secondsToMmSsDigits
import dev.kolas.nocapfit.ui.model.PreviousSetData
import dev.kolas.nocapfit.ui.model.PreviousSetLookup
import dev.kolas.nocapfit.ui.util.formatWeightInput
import dev.kolas.nocapfit.ui.util.parseWeight
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

@androidx.compose.runtime.Immutable
data class SetEntry(
    val weight: String = "0.0",
    val reps: String = "0",
    val restTimeSeconds: String = "100"
)

@androidx.compose.runtime.Immutable
data class ExerciseEntry(
    val exercise: Exercise,
    val sets: List<SetEntry> = listOf(SetEntry()),
    val note: String? = null
)

@androidx.compose.runtime.Immutable
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
    private val currentProfileHolder: CurrentProfileHolder,
    private val workoutRepository: WorkoutRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val programId: Long = savedStateHandle.get<Long>("programId") ?: -1L
    val isEditing: Boolean = programId > 0

    private val _uiState = MutableStateFlow(ProgramFormUiState())
    val uiState: StateFlow<ProgramFormUiState> = _uiState.asStateFlow()

    val availableExercises: StateFlow<List<Exercise>> = currentProfileHolder.profileId
        .flatMapLatest { profileId ->
            if (profileId == null) flowOf(emptyList()) else exerciseRepository.getAllByProfile(profileId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _previousSets = MutableStateFlow(PreviousSetLookup(emptyMap()))
    val previousSets: StateFlow<PreviousSetLookup> = _previousSets.asStateFlow()

    init {
        viewModelScope.launch {
            currentProfileHolder.ensureLoaded()

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
                                            weight = formatWeightInput(set.weightThousandths),
                                            reps = set.reps.toString(),
                                            restTimeSeconds = secondsToMmSsDigits(set.restTimeSeconds)
                                        )
                                    }
                                    .ifEmpty { listOf(SetEntry()) },
                                note = peWithSets.programExercise.note
                            )
                        }
                    _uiState.value = ProgramFormUiState(
                        name = programWithExercises.program.name,
                        exercises = exerciseEntries,
                        isLoading = false
                    )
                    loadPreviousData(exerciseEntries)
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
        viewModelScope.launch {
            loadPreviousDataForExercise(exercise.id)
        }
    }

    fun removeExercise(index: Int) {
        val current = _uiState.value
        _uiState.value = current.copy(
            exercises = current.exercises.toMutableList().apply { removeAt(index) }
        )
    }

    fun moveExercise(fromIndex: Int, toIndex: Int) {
        val current = _uiState.value
        if (fromIndex < 0 || toIndex < 0 || fromIndex >= current.exercises.size ||
            toIndex >= current.exercises.size
        ) {
            return
        }
        val exercises = current.exercises.toMutableList()
        val item = exercises.removeAt(fromIndex)
        exercises.add(toIndex, item)
        _uiState.value = current.copy(exercises = exercises)
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

    fun updateExerciseNote(exerciseIndex: Int, note: String?) {
        val current = _uiState.value
        val exercises = current.exercises.toMutableList()
        val entry = exercises[exerciseIndex]
        exercises[exerciseIndex] = entry.copy(note = note)
        _uiState.value = current.copy(exercises = exercises)
    }

    fun setRestTimeForAll(exerciseIndex: Int, restTimeDigits: String) {
        val current = _uiState.value
        val exercises = current.exercises.toMutableList()
        val entry = exercises[exerciseIndex]
        exercises[exerciseIndex] = entry.copy(
            sets = entry.sets.map { it.copy(restTimeSeconds = restTimeDigits) }
        )
        _uiState.value = current.copy(exercises = exercises)
    }

    private suspend fun loadPreviousData(exercises: List<ExerciseEntry>) {
        val map = mutableMapOf<Pair<Long, Int>, PreviousSetData>()
        for (entry in exercises) {
            loadPreviousForExercise(entry.exercise.id, map)
        }
        _previousSets.value = PreviousSetLookup(map)
    }

    private suspend fun loadPreviousForExercise(
        exId: Long,
        map: MutableMap<Pair<Long, Int>, PreviousSetData>
    ) {
        val previous = workoutRepository.getLastFinishedByExerciseId(exId) ?: return
        val prevExercise = previous.exercises.find {
            it.workoutExercise.exerciseId == exId
        } ?: return
        for (set in prevExercise.sets.filter { it.completed }) {
            map[exId to set.setIndex] = PreviousSetData(set.weightThousandths, set.reps)
        }
    }

    private suspend fun loadPreviousDataForExercise(exerciseId: Long) {
        val map = _previousSets.value.toMutableMap()
        loadPreviousForExercise(exerciseId, map)
        _previousSets.value = PreviousSetLookup(map)
    }

    suspend fun save(): Boolean {
        val state = _uiState.value
        val profileId = currentProfileHolder.profileId.value ?: return false

        val validationError = when {
            state.name.isBlank() -> "Program name is required"
            state.exercises.isEmpty() -> "Add at least one exercise"
            else -> null
        }
        if (validationError != null) {
            _uiState.value = state.copy(nameError = validationError)
            return false
        }

        _uiState.value = state.copy(isSaving = true)

        return try {
            val program = Program(
                id = if (isEditing) programId else 0L,
                profileId = profileId,
                name = state.name.trim()
            )
            val exercises = state.exercises.mapIndexed { exerciseIndex, exerciseEntry ->
                val pe = ProgramExercise(
                    programId = 0L,
                    exerciseId = exerciseEntry.exercise.id,
                    orderIndex = exerciseIndex,
                    note = exerciseEntry.note
                )
                val sets = exerciseEntry.sets.mapIndexed { setIndex, setEntry ->
                    ProgramExerciseSet(
                        programExerciseId = 0L,
                        setIndex = setIndex,
                        weightThousandths = parseWeight(setEntry.weight),
                        reps = setEntry.reps.toIntOrNull() ?: 0,
                        restTimeSeconds = parseMmSsToSeconds(setEntry.restTimeSeconds)
                    )
                }
                pe to sets
            }
            programRepository.saveProgramWithExercises(program, isEditing, exercises)
            true
        } catch (_: Exception) {
            _uiState.value = _uiState.value.copy(isSaving = false)
            false
        }
    }
}
