package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PricingTier(
    val id: String,
    val title: String,
    val priceDisplay: String,
    val priceNumeric: Double,
    val badge: String?,
    val description: String,
    val features: List<String>
)

object PricingTiers {
    val PRO_LIFETIME = PricingTier(
        id = "holiday_wishes_pro_5",
        title = "Holiday Wishes Pro",
        priceDisplay = "$4.99",
        priceNumeric = 4.99,
        badge = "Popular",
        description = "One-time purchase for full access to all exclusive holiday card designs.",
        features = listOf(
            "Unlock all 10+ Pro Christmas & New Year templates",
            "Ad-Free experience across the entire app",
            "High-resolution card export for print & stories",
            "Lifetime access with zero recurring subscriptions"
        )
    )

    val ULTIMATE_VIP = PricingTier(
        id = "holiday_wishes_vip_10",
        title = "Ultimate VIP Pass",
        priceDisplay = "$9.99",
        priceNumeric = 9.99,
        badge = "Best Value",
        description = "The complete holiday package for families, creators & holiday cheer.",
        features = listOf(
            "Everything in Holiday Wishes Pro ($5 tier)",
            "Unlimited AI Holiday Wish Generations (No daily limits)",
            "Exclusive VIP Gold Foil & Champagne card effects",
            "Access to all 10 festive sticker stamps & custom badges",
            "VIP Gold Crown badge on your saved cards",
            "Priority support & all future 2027 holiday updates"
        )
    )
}

class MonetizationManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isProUser = MutableStateFlow(prefs.getBoolean(KEY_IS_PRO, false))
    val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()

    private val _isVipUser = MutableStateFlow(prefs.getBoolean(KEY_IS_VIP, false))
    val isVipUser: StateFlow<Boolean> = _isVipUser.asStateFlow()

    private val _unlockedCardIds = MutableStateFlow(
        prefs.getStringSet(KEY_UNLOCKED_CARDS, emptySet())?.toSet() ?: emptySet()
    )
    val unlockedCardIds: StateFlow<Set<String>> = _unlockedCardIds.asStateFlow()

    private val _rewardedAdWatches = MutableStateFlow(prefs.getInt(KEY_REWARDED_WATCHES, 0))
    val rewardedAdWatches: StateFlow<Int> = _rewardedAdWatches.asStateFlow()

    fun isCardUnlocked(cardId: String, isTemplatePro: Boolean): Boolean {
        if (!isTemplatePro) return true
        if (_isProUser.value || _isVipUser.value) return true
        return _unlockedCardIds.value.contains(cardId)
    }

    /**
     * Simulates or executes Google Play Billing purchase.
     * When integrated with the Play Billing Client, this is called upon BillingResult.OK.
     */
    fun completePurchase(tierId: String): Boolean {
        val isVip = tierId == PricingTiers.ULTIMATE_VIP.id
        prefs.edit().apply {
            putBoolean(KEY_IS_PRO, true)
            if (isVip) {
                putBoolean(KEY_IS_VIP, true)
            }
            apply()
        }
        _isProUser.value = true
        if (isVip) {
            _isVipUser.value = true
        }
        return true
    }

    /**
     * Unlocks a single Pro template after the user watches a 15-30s rewarded video ad.
     */
    fun unlockCardViaRewardedAd(cardId: String) {
        val currentSet = _unlockedCardIds.value.toMutableSet()
        currentSet.add(cardId)
        val newWatches = _rewardedAdWatches.value + 1

        prefs.edit().apply {
            putStringSet(KEY_UNLOCKED_CARDS, currentSet)
            putInt(KEY_REWARDED_WATCHES, newWatches)
            apply()
        }

        _unlockedCardIds.value = currentSet
        _rewardedAdWatches.value = newWatches
    }

    /**
     * Restores existing purchases for user.
     */
    fun restorePurchases(): Boolean {
        // Reads from Google Play Billing or persistent preferences
        val hasPro = prefs.getBoolean(KEY_IS_PRO, false)
        val hasVip = prefs.getBoolean(KEY_IS_VIP, false)
        _isProUser.value = hasPro
        _isVipUser.value = hasVip
        return hasPro || hasVip
    }

    /**
     * Reset / Debug helper for testing monetization flows in emulator.
     */
    fun resetPurchasesForTesting() {
        prefs.edit().clear().apply()
        _isProUser.value = false
        _isVipUser.value = false
        _unlockedCardIds.value = emptySet()
        _rewardedAdWatches.value = 0
    }

    companion object {
        private const val PREFS_NAME = "holiday_wishes_monetization_prefs"
        private const val KEY_IS_PRO = "key_is_pro_user"
        private const val KEY_IS_VIP = "key_is_vip_user"
        private const val KEY_UNLOCKED_CARDS = "key_unlocked_cards"
        private const val KEY_REWARDED_WATCHES = "key_rewarded_watches"

        @Volatile
        private var instance: MonetizationManager? = null

        fun getInstance(context: Context): MonetizationManager {
            return instance ?: synchronized(this) {
                instance ?: MonetizationManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
