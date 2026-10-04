package com.example.ui.navigation

import android.app.Activity
import android.content.Context
import com.example.data.ads.AdManager
import com.example.data.subscription.SubscriptionManager

object AdNavigationHelper {

    // Sections requiring ads for free users before entering
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
            val activity = context as? Activity
            if (activity != null) {
                // Show ad before entering section for free users
                AdManager.showInterstitialAd(activity) {
                    onNavigate()
                }
            } else {
                onNavigate()
            }
        } else {
            onNavigate()
        }
    }
}
