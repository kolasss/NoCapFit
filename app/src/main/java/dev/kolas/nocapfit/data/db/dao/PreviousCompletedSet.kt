package dev.kolas.nocapfit.data.db.dao

data class PreviousCompletedSet(
    val exerciseId: Long,
    val setIndex: Int,
    val weightThousandths: Int,
    val reps: Int
)
