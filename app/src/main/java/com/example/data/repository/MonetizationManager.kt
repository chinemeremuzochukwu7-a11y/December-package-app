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
    val PRO_MONTHLY = PricingTier(
        id = "holiday_wishes_monthly",
        title = "Holiday Pro Monthly",
        priceDisplay = "$9.99 / mo",
        priceNumeric = 9.99,
        badge = "300 Credits Included",
        description = "300 AI Credits included, full access to 1,000+ Pro cards & wishes, watermarks removed & ad-free.",
        features = listOf(
            "🪙 300 AI Credits included each month",
            "Unlock 1,000+ Pro Christmas, New Year & Season cards",
            "Unlock 1,000+ Pro Wishes across all categories",
            "Remove all watermarks on cards & wishes",
            "Full HD Card Download to Device Gallery",
            "Ad-Free experience across the entire app",
            "Cancel anytime in Google Play Store"
        )
    )

    val PRO_YEARLY = PricingTier(
        id = "holiday_wishes_yearly",
        title = "Holiday Pro Yearly",
        priceDisplay = "$79.00 / yr",
        priceNumeric = 79.00,
        badge = "⭐ SAVE 34% • UNLIMITED",
        description = "Best value! Just $6.58/mo billed annually. UNLIMITED AI Credits for the whole year!",
        features = listOf(
            "✨ UNLIMITED AI Credits for 1 full year",
            "Save 34% compared to monthly payments ($6.58/mo)",
            "Unlock all 1,000+ Pro Cards (500+ Season, Christmas & New Year)",
            "Unlock all 1,000+ Pro Wishes",
            "Remove all watermarks on cards & wishes",
            "Full HD Card Download & Auto-Schedule delivery",
            "Exclusive VIP Gold Foil effects & custom badges",
            "Ad-Free for the full year"
        )
    )

    // Compatibility aliases
    val PRO_LIFETIME = PRO_MONTHLY
    val ULTIMATE_VIP = PRO_YEARLY
}

class MonetizationManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isProUser = MutableStateFlow(prefs.getBoolean(KEY_IS_PRO, false))
    val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()

    private val _isVipUser = MutableStateFlow(prefs.getBoolean(KEY_IS_VIP, false))
    val isVipUser: StateFlow<Boolean> = _isVipUser.asStateFlow()

    private fun getTodayDateString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return sdf.format(java.util.Date())
    }

    private fun loadTodayUnlockedCards(): Set<String> {
        val today = getTodayDateString()
        val rawSet = prefs.getStringSet(KEY_DAILY_UNLOCKED_CARDS, emptySet()) ?: emptySet()
        return rawSet.mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size >= 2 && parts[1] == today) parts[0] else null
        }.toSet()
    }

    private fun loadTodayUnlockedWishes(): Set<String> {
        val today = getTodayDateString()
        val rawSet = prefs.getStringSet(KEY_DAILY_UNLOCKED_WISHES, emptySet()) ?: emptySet()
        return rawSet.mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size >= 2 && parts[1] == today) parts[0] else null
        }.toSet()
    }

    private val _unlockedCardIds = MutableStateFlow(loadTodayUnlockedCards())
    val unlockedCardIds: StateFlow<Set<String>> = _unlockedCardIds.asStateFlow()

    private val _unlockedWishIds = MutableStateFlow(loadTodayUnlockedWishes())
    val unlockedWishIds: StateFlow<Set<String>> = _unlockedWishIds.asStateFlow()

    private val _rewardedAdWatches = MutableStateFlow(prefs.getInt(KEY_REWARDED_WATCHES, 0))
    val rewardedAdWatches: StateFlow<Int> = _rewardedAdWatches.asStateFlow()

    /**
     * Checks if a card is unlocked.
     * If unlocked via rewarded ad, it remains unlocked only for the current day.
     * When the day ends, it automatically locks again!
     */
    fun isCardUnlocked(cardId: String, isTemplatePro: Boolean): Boolean {
        if (!isTemplatePro) return true
        if (_isProUser.value || _isVipUser.value || com.example.data.subscription.SubscriptionManager.isPro.value) return true
        val todayUnlocked = loadTodayUnlockedCards()
        if (_unlockedCardIds.value != todayUnlocked) {
            _unlockedCardIds.value = todayUnlocked
        }
        return todayUnlocked.contains(cardId)
    }

    /**
     * Checks if a wish is unlocked.
     * If unlocked via rewarded ad, it remains unlocked only for the current day.
     * When the day ends, it automatically locks again!
     */
    fun isWishUnlocked(wishId: String, isProWish: Boolean): Boolean {
        if (!isProWish) return true
        if (_isProUser.value || _isVipUser.value || com.example.data.subscription.SubscriptionManager.isPro.value) return true
        val todayUnlocked = loadTodayUnlockedWishes()
        if (_unlockedWishIds.value != todayUnlocked) {
            _unlockedWishIds.value = todayUnlocked
        }
        return todayUnlocked.contains(wishId)
    }

    /**
     * Unlocks a single Pro wish after the user watches a rewarded ad.
     * The unlock is tied to today's date so it automatically locks again when the day ends!
     */
    fun unlockWishViaRewardedAd(wishId: String) {
        val today = getTodayDateString()
        val currentSet = loadTodayUnlockedWishes().toMutableSet()
        currentSet.add(wishId)
        val newWatches = _rewardedAdWatches.value + 1

        val serializedSet = currentSet.map { "$it:$today" }.toSet()

        prefs.edit().apply {
            putStringSet(KEY_DAILY_UNLOCKED_WISHES, serializedSet)
            putInt(KEY_REWARDED_WATCHES, newWatches)
            apply()
        }

        _unlockedWishIds.value = currentSet
        _rewardedAdWatches.value = newWatches
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
        com.example.data.subscription.SubscriptionManager.setProDirect(true)
        return true
    }

    /**
     * Unlocks a single Pro template after the user watches a rewarded video ad.
     * The unlock is tied to today's date so it automatically locks again when the day ends!
     */
    fun unlockCardViaRewardedAd(cardId: String) {
        val today = getTodayDateString()
        val currentSet = loadTodayUnlockedCards().toMutableSet()
        currentSet.add(cardId)
        val newWatches = _rewardedAdWatches.value + 1

        val serializedSet = currentSet.map { "$it:$today" }.toSet()

        prefs.edit().apply {
            putStringSet(KEY_DAILY_UNLOCKED_CARDS, serializedSet)
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
        _unlockedWishIds.value = emptySet()
        _rewardedAdWatches.value = 0
    }

    companion object {
        private const val PREFS_NAME = "holiday_wishes_monetization_prefs"
        private const val KEY_IS_PRO = "key_is_pro_user"
        private const val KEY_IS_VIP = "key_is_vip_user"
        private const val KEY_DAILY_UNLOCKED_CARDS = "key_daily_unlocked_cards"
        private const val KEY_DAILY_UNLOCKED_WISHES = "key_daily_unlocked_wishes"
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
