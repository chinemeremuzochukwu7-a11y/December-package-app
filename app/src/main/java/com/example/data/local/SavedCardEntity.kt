package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_cards")
data class SavedCardEntity(
    @PrimaryKey
    val id: String,
    val templateId: String,
    val title: String,
    val subtitle: String,
    val occasion: String,
    val recipientName: String,
    val message: String,
    val senderName: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val accentColorHex: Long,
    val decorationStyle: String,
    val textSizeSp: Float = 15f,
    val textAlign: String = "Center",
    val showRecipient: Boolean = true,
    val showSender: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
