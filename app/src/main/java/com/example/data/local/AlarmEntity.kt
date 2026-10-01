package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val type: String = "ALARM", // "ALARM" or "REMINDER"
    val hour: Int,              // 0 to 23
    val minute: Int,            // 0 to 59
    val isEnabled: Boolean = true,
    val repeatMode: String = "Daily", // "Once", "Daily", "Weekdays", "Weekends"
    val isLoudAlarm: Boolean = true,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
