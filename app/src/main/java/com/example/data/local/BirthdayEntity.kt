package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "birthdays")
data class BirthdayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val birthMonth: Int, // 1 to 12
    val birthDay: Int,   // 1 to 31
    val birthYear: Int? = null, // Optional birth year for age calculation
    val relationship: String = "Friend", // Friend, Family, Partner, Colleague, etc.
    val giftIdeas: String = "",
    val notes: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
