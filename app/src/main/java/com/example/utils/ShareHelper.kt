package com.example.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.data.ads.AdManager
import com.example.data.subscription.SubscriptionManager

object ShareHelper {
    fun shareWish(context: Context, wishText: String, subject: String = "Holiday Wish") {
        val isPro = SubscriptionManager.isPro.value
        val finalWishText = if (isPro) {
            wishText
        } else {
            if (wishText.contains("Holiday Wishes")) {
                wishText
            } else {
                "$wishText\n\n✨ Made with Holiday Wishes 🎄"
            }
        }

        val doShare = {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, finalWishText)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share Holiday Wish via")
            context.startActivity(shareIntent)
        }

        if (isPro) {
            doShare()
        } else {
            val activity = context as? Activity
            if (activity != null) {
                // Free users must watch rewarded ad before sending to user (+1 credit earned)
                AdManager.showRewardedAd(
                    activity = activity,
                    onRewardEarned = { /* 1 credit added */ },
                    onDismiss = { doShare() }
                )
            } else {
                doShare()
            }
        }
    }

    fun shareCard(
        context: Context,
        title: String,
        recipient: String,
        message: String,
        sender: String
    ) {
        val isPro = SubscriptionManager.isPro.value
        val fullCardText = buildString {
            append("✨ $title ✨\n\n")
            if (recipient.isNotBlank()) append("To: $recipient\n\n")
            append("$message\n\n")
            if (sender.isNotBlank()) append("From: $sender\n\n")
            if (!isPro) {
                append("✨ Made with Holiday Wishes 🎄")
            }
        }.trimEnd()
        shareWish(context, fullCardText, subject = title)
    }
}
