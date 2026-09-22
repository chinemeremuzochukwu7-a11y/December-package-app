package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_generated_wishes")
data class AiGeneratedWishEntity(
    @PrimaryKey
    val id: String,
    val text: String,
    val occasion: String,
    val recipient: String,
    val recipientName: String,
    val tone: String,
    val language: String,
    val createdAt: Long = System.currentTimeMillis()
)
