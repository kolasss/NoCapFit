package dev.kolas.nocapfit.data.db

import androidx.room.TypeConverter
import dev.kolas.nocapfit.data.db.entity.TimerStatus

class Converters {
    @TypeConverter
    fun fromTimerStatus(status: TimerStatus): String = status.name

    @TypeConverter
    fun toTimerStatus(value: String): TimerStatus = TimerStatus.valueOf(value)
}
