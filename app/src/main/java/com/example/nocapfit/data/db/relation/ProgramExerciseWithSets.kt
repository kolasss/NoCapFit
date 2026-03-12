package com.example.nocapfit.data.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.nocapfit.data.db.entity.Exercise
import com.example.nocapfit.data.db.entity.ProgramExercise
import com.example.nocapfit.data.db.entity.ProgramExerciseSet

data class ProgramExerciseWithSets(
    @Embedded val programExercise: ProgramExercise,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: Exercise,
    @Relation(
        parentColumn = "id",
        entityColumn = "programExerciseId"
    )
    val sets: List<ProgramExerciseSet>
)
