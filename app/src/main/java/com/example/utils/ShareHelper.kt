package com.example.utils

import android.content.Context
import android.content.Intent

object ShareHelper {
    fun shareWish(context: Context, wishText: String, subject: String = "Holiday Wish") {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, wishText)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Holiday Wish via")
        context.startActivity(shareIntent)
    }

    fun shareCard(
        context: Context,
        title: String,
        recipient: String,
        message: String,
        sender: String
    ) {
        val fullCardText = buildString {
            append("✨ $title ✨\n\n")
            if (recipient.isNotBlank()) append("To: $recipient\n\n")
            append("$message\n\n")
            if (sender.isNotBlank()) append("From: $sender\n\n")
            append("— Shared via Holiday Wishes 🎄")
        }
        shareWish(context, fullCardText, subject = title)
    }
}
