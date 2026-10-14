package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_greetings")
data class ScheduledGreetingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,                    // e.g. "Christmas Greeting to Mom"
    val recipientName: String,            // e.g. "Mom"
    val recipientPhone: String = "",      // Phone number for WhatsApp (e.g. "+1234567890")
    val messageText: String,              // Card message or Wish text
    val cardTitle: String = "",           // Card template title if card
    val cardImagePath: String? = null,    // Local cached file path for card image
    val scheduledTimestamp: Long,         // Epoch time in milliseconds when to send
    val targetPlatform: String = "WhatsApp", // "WhatsApp", "Other Media"
    val isDelivered: Boolean = false,     // Whether notification was fired
    val createdAt: Long = System.currentTimeMillis()
)
