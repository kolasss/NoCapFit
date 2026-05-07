package dev.kolas.nocapfit.data.repository

import dev.kolas.nocapfit.data.db.dao.ProgramDao
import dev.kolas.nocapfit.data.db.entity.Program
import dev.kolas.nocapfit.data.db.entity.ProgramExercise
import dev.kolas.nocapfit.data.db.entity.ProgramExerciseSet
import dev.kolas.nocapfit.data.db.entity.WorkoutSet
import dev.kolas.nocapfit.data.db.relation.ProgramWithExercises
import dev.kolas.nocapfit.data.db.relation.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgramRepository @Inject constructor(
    private val programDao: ProgramDao
) {
    suspend fun insert(program: Program): Long = programDao.insert(program)
    suspend fun update(program: Program) = programDao.update(program)
    suspend fun delete(program: Program) = programDao.delete(program)
    suspend fun getById(id: Long): Program? = programDao.getById(id)
    fun getAllWithExercises(profileId: Long): Flow<List<ProgramWithExercises>> = programDao.getAllWithExercises(
        profileId
    )
    suspend fun getProgramWithExercises(id: Long): ProgramWithExercises? = programDao.getProgramWithExercises(id)
    suspend fun saveProgramWithExercises(
        program: Program,
        isUpdate: Boolean,
        exercises: List<Pair<ProgramExercise, List<ProgramExerciseSet>>>
    ): Long = programDao.saveProgramWithExercises(program, isUpdate, exercises)

    suspend fun copyProgram(programId: Long): Long {
        val source = programDao.getProgramWithExercises(programId)
            ?: throw IllegalArgumentException("Program not found: $programId")
        val newProgram = Program(
            profileId = source.program.profileId,
            name = "${source.program.name} (Copy)"
        )
        val exercises = source.exercises.map { peWithSets ->
            newProgramExercise(
                exerciseId = peWithSets.programExercise.exerciseId,
                orderIndex = peWithSets.programExercise.orderIndex,
                note = peWithSets.programExercise.note
            ) to peWithSets.sets.map { it.copy(id = 0L, programExerciseId = 0L) }
        }
        return programDao.saveProgramWithExercises(newProgram, isUpdate = false, exercises)
    }

    suspend fun createProgramFromWorkout(workout: WorkoutWithExercises, programName: String): Long {
        val program = Program(profileId = workout.workout.profileId, name = programName)
        val exercises = workout.exercises
            .filter { it.workoutExercise.exerciseId != null }
            .mapNotNull { weWithSets ->
                val completedSets = weWithSets.sets.filter { it.completed }
                if (completedSets.isEmpty()) return@mapNotNull null
                newProgramExercise(
                    exerciseId = weWithSets.workoutExercise.exerciseId!!,
                    orderIndex = weWithSets.workoutExercise.orderIndex,
                    note = weWithSets.workoutExercise.note
                ) to completedSets.map { it.toProgramSet() }
            }
        return programDao.saveProgramWithExercises(program, isUpdate = false, exercises)
    }

    private fun newProgramExercise(exerciseId: Long, orderIndex: Int, note: String?) =
        ProgramExercise(programId = 0L, exerciseId = exerciseId, orderIndex = orderIndex, note = note)

    private fun WorkoutSet.toProgramSet() = ProgramExerciseSet(
        programExerciseId = 0L,
        setIndex = setIndex,
        weightThousandths = weightThousandths,
        reps = reps,
        restTimeSeconds = restTimeSeconds
    )
}
