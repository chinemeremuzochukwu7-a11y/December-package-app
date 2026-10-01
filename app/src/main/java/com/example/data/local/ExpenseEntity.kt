package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String, // Gifts, Food, Decorations, Travel, Outfits, Other
    val dateEpoch: Long = System.currentTimeMillis(),
    val notes: String = "",
    val recipientOrStore: String = "",
    val isPaid: Boolean = true
)
