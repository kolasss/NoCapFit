package com.example.nocapfit.data.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.nocapfit.data.db.entity.Program
import com.example.nocapfit.data.db.entity.ProgramExercise

data class ProgramWithExercises(
    @Embedded val program: Program,
    @Relation(
        entity = ProgramExercise::class,
        parentColumn = "id",
        entityColumn = "programId"
    )
    val exercises: List<ProgramExerciseWithSets>
)
