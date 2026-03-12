package com.example.nocapfit.data.db

import androidx.room.TypeConverter
import com.example.nocapfit.data.db.entity.TimerStatus

class Converters {
    @TypeConverter
    fun fromTimerStatus(status: TimerStatus): String = status.name

    @TypeConverter
    fun toTimerStatus(value: String): TimerStatus = TimerStatus.valueOf(value)
}
