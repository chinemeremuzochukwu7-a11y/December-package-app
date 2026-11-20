package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PricingTier(
    val id: String,
    val title: String,
    val priceDisplay: String,
    val priceNumeric: Double,
    val creditsAllowance: Int,
    val badge: String?,
    val description: String,
    val features: List<String>
)

object PricingTiers {
    val STARTER_MONTHLY = PricingTier(
        id = "holiday_wishes_monthly",
        title = "Starter Monthly",
        priceDisplay = "$2.90 / mo",
        priceNumeric = 2.90,
        creditsAllowance = 150,
        badge = "150 Credits / Month",
        description = "150 Gemini AI Credits/month, all 1,000+ Pro cards & wishes unlocked, zero watermarks & ad-free.",
        features = listOf(
            "🪙 150 AI Credits refreshed every month",
            "Unlock 1,000+ Pro Christmas, New Year & Season cards",
            "Unlock 1,000+ Pro Wishes across all categories",
            "Remove watermarks + Full HD Card Downloads",
            "100% Ad-Free Experience Across App",
            "Cancel anytime on Google Play"
        )
    )

    val PLUS_MONTHLY = PricingTier(
        id = "holiday_wishes_plus_monthly",
        title = "Plus Monthly",
        priceDisplay = "$5.90 / mo",
        priceNumeric = 5.90,
        creditsAllowance = 400,
        badge = "⭐ RECOMMENDED • 400 Credits",
        description = "Best monthly value! 400 AI Credits/month for high-volume holiday greetings & AI cards.",
        features = listOf(
            "🪙 400 AI Credits refreshed every month",
            "Unlock all 1,000+ Pro Cards & Pro Wishes",
            "Remove watermarks + 4 HD Export Sizes",
            "Ad-Free Experience + Priority AI Creation",
            "Cancel anytime on Google Play"
        )
    )

    val ANNUAL_VIP = PricingTier(
        id = "holiday_wishes_yearly",
        title = "Annual VIP Pass",
        priceDisplay = "$35.00 / yr",
        priceNumeric = 35.00,
        creditsAllowance = 350,
        badge = "👑 SAVE 51% • 4,200 Credits/Yr",
        description = "Just ~$2.92/mo billed annually. 350 fresh AI Credits every month for 12 months (4,200/yr)!",
        features = listOf(
            "✨ 350 AI Credits/mo every month (4,200/year)",
            "Save 51% vs Plus Monthly (~$2.92/mo)",
            "Unlock all 1,000+ Pro Cards (Season, Xmas, New Year)",
            "Unlock all 1,000+ Pro Wishes & Zero Watermarks",
            "Ad-Free for 1 full year + VIP Gold Themes"
        )
    )

    val ALL_TIERS = listOf(STARTER_MONTHLY, PLUS_MONTHLY, ANNUAL_VIP)

    // Compatibility aliases mapped strictly to the 3 approved plans
    val PRO_MONTHLY = STARTER_MONTHLY
    val PRO_PLUS = PLUS_MONTHLY
    val PRO_YEARLY = ANNUAL_VIP
    val PRO_LIFETIME = STARTER_MONTHLY
    val ULTIMATE_VIP = ANNUAL_VIP
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

    fun syncProStatus(isPro: Boolean, isVip: Boolean) {
        _isProUser.value = isPro
        _isVipUser.value = isVip
        prefs.edit().apply {
            putBoolean(KEY_IS_PRO, isPro)
            putBoolean(KEY_IS_VIP, isVip)
            apply()
        }
    }

    fun isCardUnlocked(cardId: String, isTemplatePro: Boolean): Boolean {
        if (!isTemplatePro) return true
        if (_isProUser.value || _isVipUser.value || com.example.data.subscription.SubscriptionManager.isPro.value) return true
        val todayUnlocked = loadTodayUnlockedCards()
        if (_unlockedCardIds.value != todayUnlocked) {
            _unlockedCardIds.value = todayUnlocked
        }
        return todayUnlocked.contains(cardId)
    }

    fun isWishUnlocked(wishId: String, isProWish: Boolean): Boolean {
        if (!isProWish) return true
        if (_isProUser.value || _isVipUser.value || com.example.data.subscription.SubscriptionManager.isPro.value) return true
        val todayUnlocked = loadTodayUnlockedWishes()
        if (_unlockedWishIds.value != todayUnlocked) {
            _unlockedWishIds.value = todayUnlocked
        }
        return todayUnlocked.contains(wishId)
    }

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
     * Only invoked in debug builds when testing without Play Billing, or synced from SubscriptionManager.
     * Release builds must purchase exclusively through SubscriptionManager (Google Play BillingClient).
     */
    fun completePurchase(tierId: String): Boolean {
        if (!BuildConfig.DEBUG) {
            return false
        }
        val isVip = tierId == PricingTiers.ANNUAL_VIP.id || tierId == PricingTiers.PLUS_MONTHLY.id
        syncProStatus(isPro = true, isVip = isVip)
        com.example.data.subscription.SubscriptionManager.setProDirect(true)
        return true
    }

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

    fun restorePurchases(): Boolean {
        val hasPro = prefs.getBoolean(KEY_IS_PRO, false) || com.example.data.subscription.SubscriptionManager.isPro.value
        val hasVip = prefs.getBoolean(KEY_IS_VIP, false)
        _isProUser.value = hasPro
        _isVipUser.value = hasVip
        return hasPro || hasVip
    }

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
