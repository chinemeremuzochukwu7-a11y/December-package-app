package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_wishes")
data class FavoriteWishEntity(
    @PrimaryKey
    val id: String,
    val text: String,
    val category: String,
    val occasion: String = "Holiday",
    val savedAt: Long = System.currentTimeMillis()
)
