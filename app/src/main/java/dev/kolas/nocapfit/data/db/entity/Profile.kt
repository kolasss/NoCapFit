package dev.kolas.nocapfit.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "profiles",
    indices = [Index(value = ["name"], unique = true)]
)
data class Profile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)
