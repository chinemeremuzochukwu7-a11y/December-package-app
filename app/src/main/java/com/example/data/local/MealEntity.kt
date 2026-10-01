package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // e.g. "2026-10-01"
    val mealType: String,   // Breakfast, Lunch, Dinner, Festive Snack, Holiday Feast
    val foodName: String,
    val calories: Int? = null,
    val notes: String = "",
    val isFestiveSpecial: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
