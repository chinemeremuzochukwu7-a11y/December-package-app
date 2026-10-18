package com.example.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object RateAppManager {
    private const val PREFS_NAME = "rate_app_prefs"
    private const val KEY_HAS_RATED = "key_has_rated"
    private const val KEY_MAGIC_COUNT = "key_magic_count"

    // UI state flow to trigger the in-app magic rating dialog across Composables
    private val _showMagicRatingDialog = MutableStateFlow(false)
    val showMagicRatingDialog: StateFlow<Boolean> = _showMagicRatingDialog.asStateFlow()

    private val _magicEventTitle = MutableStateFlow("✨ Something Magical Happened!")
    val magicEventTitle: StateFlow<String> = _magicEventTitle.asStateFlow()

    /**
     * Call when something magical happens in the app
     * (e.g. AI wish created, card rendered, greeting shared)
     */
    fun triggerMagicEvent(context: Context, eventDescription: String = "✨ Something Magical Happened!") {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hasRated = prefs.getBoolean(KEY_HAS_RATED, false)
        if (hasRated) return

        val count = prefs.getInt(KEY_MAGIC_COUNT, 0) + 1
        prefs.edit().putInt(KEY_MAGIC_COUNT, count).apply()

        _magicEventTitle.value = eventDescription
        _showMagicRatingDialog.value = true
    }

    fun showRatingPromptExplicitly(eventDescription: String = "Rate Holiday Wishes") {
        _magicEventTitle.value = eventDescription
        _showMagicRatingDialog.value = true
    }

    fun dismissMagicRatingDialog(context: Context, neverShowAgain: Boolean = false) {
        _showMagicRatingDialog.value = false
        if (neverShowAgain) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_HAS_RATED, true).apply()
        }
    }

    fun launchGooglePlayReview(context: Context, onComplete: () -> Unit = {}) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_HAS_RATED, true).apply()
        _showMagicRatingDialog.value = false

        val activity = context as? Activity
        if (activity != null) {
            try {
                val reviewManager = ReviewManagerFactory.create(activity)
                val request = reviewManager.requestReviewFlow()
                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                        flow.addOnCompleteListener {
                            onComplete()
                        }
                    } else {
                        launchMarketOrWebIntent(context)
                        onComplete()
                    }
                }
                return
            } catch (e: Exception) {
                // Fallback to market intent below
            }
        }
        launchMarketOrWebIntent(context)
        onComplete()
    }

    private fun launchMarketOrWebIntent(context: Context) {
        val packageName = context.packageName
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }
}
