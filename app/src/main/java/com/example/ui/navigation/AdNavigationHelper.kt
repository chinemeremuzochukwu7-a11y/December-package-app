package com.example.ui.navigation

import android.app.Activity
import android.content.Context
import com.example.data.ads.AdManager
import com.example.data.subscription.SubscriptionManager

object AdNavigationHelper {

    private const val INTERSTITIAL_COOLDOWN_MS = 120_000L // 2 minutes minimum between interstitials
    private var lastInterstitialShownAtMs: Long = 0L
    private var gatedVisitCount: Int = 0

    // Sections eligible for frequency-capped interstitial transitions for free users
    private val GATED_SECTIONS = setOf(
        Screen.BirthdayTracker.route,
        Screen.ExpensesTracker.route,
        Screen.MealTracker.route,
        Screen.AlarmReminder.route,
        Screen.AiWishGenerator.route,
        Screen.CustomizeCard.route,
        Screen.CreateWish.route
    )

    fun navigateWithSectionAdCheck(
        context: Context,
        targetRoute: String,
        onNavigate: () -> Unit
    ) {
        val isGated = GATED_SECTIONS.contains(targetRoute)
        val isPro = SubscriptionManager.isPro.value

        if (isGated && !isPro) {
            gatedVisitCount += 1
            val now = System.currentTimeMillis()
            val cooldownElapsed = (now - lastInterstitialShownAtMs) >= INTERSTITIAL_COOLDOWN_MS
            val shouldShowInterstitial = gatedVisitCount >= 2 && cooldownElapsed

            val activity = context as? Activity
            if (shouldShowInterstitial && activity != null) {
                lastInterstitialShownAtMs = now
                AdManager.showInterstitialAd(activity) {
                    onNavigate()
                }
            } else {
                if (activity != null) {
                    AdManager.loadInterstitialAd(activity)
                }
                onNavigate()
            }
        } else {
            onNavigate()
        }
    }
}
