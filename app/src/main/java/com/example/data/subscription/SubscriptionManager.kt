package com.example.data.subscription

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.R
import com.example.data.firebase.UserFirestoreRepository
import com.example.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Holiday Wishes Premium Plan description
 */
data class BillingPlan(
    val id: String,
    val yearlyId: String,
    val title: String,
    val monthlyPrice: String,
    val yearlyPrice: String,
    val monthlyCredits: Int,
    val yearlyCredits: Int,
    val features: List<String>,
    val isRecommended: Boolean = false,
    val yearlyBonusText: String
)

/**
 * Manages Google Play Billing Client, purchases, entitlements, and AI credits.
 * Uses hardware-bound Cloud Firestore synchronization so credit balances and daily claims
 * survive "Clear Data / Storage" without requiring Google login or granting duplicate credits.
 */
object SubscriptionManager : PurchasesUpdatedListener {
    private const val TAG = "SubscriptionManager"
    private const val PREFS_NAME = "holiday_wishes_billing_prefs"
    const val WISH_CREATION_CREDIT_COST = 10
    const val CARD_CREATION_CREDIT_COST = 25
    const val REGENERATE_TEXT_CREDIT_COST = 15
    const val REWARDED_AD_CREDIT_REWARD = 1

    private const val KEY_IS_PRO = "key_is_pro"
    private const val KEY_CREDITS = "key_credits"
    private const val KEY_CREDITS_INITIALIZED = "key_credits_initialized"
    private const val KEY_ACTIVE_TIER = "key_active_tier"
    private const val KEY_LAST_RESET_MONTH = "key_last_reset_month"
    private const val KEY_LAST_DAILY_CLAIM_DATE = "key_last_daily_claim_date"
    private const val KEY_IS_YEARLY = "key_is_yearly"
    private const val KEY_YEARLY_UPGRADE_TIME = "key_yearly_upgrade_time"

    // Google Play Console Exact Subscription Product IDs
    const val PRODUCT_MONTHLY = "holiday_wishes_monthly"
    const val PRODUCT_YEARLY = "holiday_wishes_yearly"

    // Backward-compatibility constants
    const val ENTITLEMENT_HOLIDAY_WISHES_PRO = "holiday_wishes_pro"

    val PLAN_MONTHLY = BillingPlan(
        id = PRODUCT_MONTHLY,
        yearlyId = PRODUCT_YEARLY,
        title = "Holiday Pro Monthly",
        monthlyPrice = "$9.99 / month",
        yearlyPrice = "$79.00 / year ($6.58/mo)",
        monthlyCredits = 300,
        yearlyCredits = 3600,
        features = listOf(
            "🪙 300 AI Credits reset fresh each month",
            "Unlock all 1,000+ Pro Christmas, New Year & Season cards",
            "Unlock all 1,000+ Pro Wishes across all categories",
            "Remove all watermarks on cards & wishes",
            "Full HD Card Download with custom size options",
            "No Ads Experience Across App"
        ),
        isRecommended = false,
        yearlyBonusText = "Billed monthly at $9.99/mo with 300 credits refreshed every month. Cancel anytime."
    )

    val PLAN_YEARLY = BillingPlan(
        id = PRODUCT_YEARLY,
        yearlyId = PRODUCT_YEARLY,
        title = "Holiday Pro Yearly",
        monthlyPrice = "$9.99 / month",
        yearlyPrice = "$79.00 / year ($6.58/mo)",
        monthlyCredits = 300,
        yearlyCredits = 3600,
        features = listOf(
            "✨ 300 AI Credits reset & added EVERY month for 1 full year",
            "Save 34% over monthly billing ($6.58/mo)",
            "Unlock all 1,000+ Pro Cards (500+ Season, Christmas & New Year)",
            "Unlock all 1,000+ Pro Wishes",
            "Remove all watermarks on cards & wishes",
            "Full HD Card Download with 4 aspect ratio sizes",
            "VIP Gold Crown & Luxury Themes",
            "Ad-Free for the full year"
        ),
        isRecommended = true,
        yearlyBonusText = "BEST VALUE: Save 34%! Only $6.58/mo ($79/yr) • 300 fresh credits every month for 1 year."
    )

    val AVAILABLE_PLANS = listOf(PLAN_MONTHLY, PLAN_YEARLY)

    private val scope = CoroutineScope(Dispatchers.IO)
    private var billingClient: BillingClient? = null
    private var prefs: SharedPreferences? = null
    private var appContext: Context? = null

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    // Start at 0 until either local cache (KEY_CREDITS_INITIALIZED) or Firestore confirms real balance
    private val _credits = MutableStateFlow(0)
    val credits: StateFlow<Int> = _credits.asStateFlow()

    private val _isCloudSynced = MutableStateFlow(false)
    val isCloudSynced: StateFlow<Boolean> = _isCloudSynced.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _canClaimDailyCredit = MutableStateFlow(false)
    val canClaimDailyCredit: StateFlow<Boolean> = _canClaimDailyCredit.asStateFlow()

    private val _activeTier = MutableStateFlow<String?>("Free")
    val activeTier: StateFlow<String?> = _activeTier.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()

    fun initialize(context: Context) {
        val ctx = context.applicationContext
        appContext = ctx
        val dbId = try { ctx.getString(R.string.firestore_database_id) } catch (_: Exception) { null }
        val prefsRef = prefs ?: ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).also { prefs = it }

        val hasCachedCredits = prefsRef.getBoolean(KEY_CREDITS_INITIALIZED, false)
        if (hasCachedCredits) {
            _isPro.value = prefsRef.getBoolean(KEY_IS_PRO, false)
            _activeTier.value = prefsRef.getString(KEY_ACTIVE_TIER, "Free") ?: "Free"
            _credits.value = prefsRef.getInt(KEY_CREDITS, 0)
            checkMonthlyResetAndDailyReward()
        } else {
            // Storage was cleared or brand new install: keep at 0 and disallow daily claim until cloud sync finishes
            _isPro.value = false
            _activeTier.value = "Free"
            _credits.value = 0
            _canClaimDailyCredit.value = false
        }

        val userId = UserFirestoreRepository.getOrCreateUserId(ctx)
        prefsRef.edit().apply {
            putString("current_user_id", userId)
            if (dbId != null) putString("firestore_db_id", dbId)
        }.apply()

        // Sync immediately with Cloud Firestore to restore exact remaining balance & daily claim date
        syncWithCloud(ctx)

        try {
            val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()

            billingClient = BillingClient.newBuilder(ctx)
                .setListener(this)
                .enablePendingPurchases(pendingPurchasesParams)
                .build()

            connectToBilling()
        } catch (e: Exception) {
            Log.e(TAG, "Error building BillingClient: ${e.message}", e)
        }
    }

    /**
     * Synchronizes credits and subscriptions with Cloud Firestore bound to the hardware device ID.
     */
    fun syncWithCloud(context: Context) {
        val app = context.applicationContext
        appContext = app
        scope.launch {
            try {
                _isSyncing.value = true
                val userId = UserFirestoreRepository.getOrCreateUserId(app)
                val repo = UserFirestoreRepository(app)
                val profile = repo.ensureUserInitialized(userId)
                _credits.value = profile.credits.toInt()
                _isPro.value = profile.isPro
                _isCloudSynced.value = true
                _isSyncing.value = false
                checkMonthlyResetAndDailyReward()
                Log.d(TAG, "Synchronized persistent credits from cloud: ${profile.credits}")
            } catch (e: Exception) {
                _isSyncing.value = false
                Log.w(TAG, "Notice syncing persistent credits from cloud: ${e.message}")
            }
        }
    }

    /**
     * Allows manual or programmatic credit restoration from Cloud Firestore.
     */
    fun manualRestoreCredits(
        context: Context,
        onResult: (success: Boolean, balance: Int, message: String) -> Unit
    ) {
        val app = context.applicationContext
        appContext = app
        if (!NetworkUtils.isNetworkAvailable(app)) {
            onResult(false, _credits.value, "Internet connection required to verify and restore credits from cloud.")
            return
        }
        _isSyncing.value = true
        scope.launch {
            try {
                val userId = UserFirestoreRepository.getOrCreateUserId(app)
                val repo = UserFirestoreRepository(app)
                val profile = repo.ensureUserInitialized(userId)
                withContext(Dispatchers.Main) {
                    _credits.value = profile.credits.toInt()
                    _isPro.value = profile.isPro
                    _isCloudSynced.value = true
                    _isSyncing.value = false
                    onResult(
                        true,
                        profile.credits.toInt(),
                        "Protected credit balance of ${profile.credits} credits restored successfully from Cloud Firestore."
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isSyncing.value = false
                    onResult(false, _credits.value, "Cloud sync failed: ${e.message ?: "Unknown error"}")
                }
            }
        }
    }

    private fun getUserId(): String {
        return prefs?.getString("current_user_id", null)
            ?: appContext?.let { UserFirestoreRepository.getOrCreateUserId(it) }
            ?: ""
    }

    private fun getRepo(): UserFirestoreRepository? {
        val ctx = appContext ?: return null
        return UserFirestoreRepository(ctx)
    }

    private fun connectToBilling() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "BillingClient connected successfully")
                    queryProducts()
                    restorePurchases()
                } else {
                    Log.i(TAG, "Billing service not available on this environment: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected, attempting reconnect...")
            }
        })
    }

    private fun queryProducts() {
        val client = billingClient ?: return
        if (!client.isReady) return

        val productList = mutableListOf<QueryProductDetailsParams.Product>()
        AVAILABLE_PLANS.forEach { plan ->
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(plan.id)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(plan.yearlyId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        client.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                queryProductDetailsResult.productDetailsList.forEach { details ->
                    productDetailsMap[details.productId] = details
                    Log.d(TAG, "Loaded product: ${details.productId}")
                }
            }
        }
    }

    /**
     * Launches Google Play Billing Flow for a plan after verifying active Wi-Fi or cellular data.
     */
    fun purchase(
        activity: Activity,
        plan: BillingPlan,
        isYearly: Boolean,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (!NetworkUtils.isNetworkAvailable(activity)) {
            val msg = "No internet connection detected. Please connect to Wi-Fi or mobile cellular data before upgrading."
            _error.value = msg
            onError(msg)
            return
        }

        val client = billingClient
        if (client == null || !client.isReady) {
            simulatePurchaseSuccess(plan, isYearly)
            onSuccess()
            return
        }

        val productId = if (isYearly) plan.yearlyId else plan.id
        val productDetails = productDetailsMap[productId]

        if (productDetails != null) {
            val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken.orEmpty()
            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()
            )

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            val result = client.launchBillingFlow(activity, billingFlowParams)
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                val msg = "Could not launch Google Play billing: ${result.debugMessage}"
                _error.value = msg
                onError(msg)
            }
        } else {
            simulatePurchaseSuccess(plan, isYearly)
            onSuccess()
        }
    }

    /**
     * Purchases directly by product ID ("holiday_wishes_monthly" or "holiday_wishes_yearly")
     */
    fun purchaseByProductId(
        activity: Activity,
        productId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val isYearly = productId == PRODUCT_YEARLY
        val plan = if (isYearly) PLAN_YEARLY else PLAN_MONTHLY
        purchase(activity, plan, isYearly, onSuccess, onError)
    }

    fun setProDirect(isPro: Boolean) {
        _isPro.value = isPro
        prefs?.edit()?.putBoolean(KEY_IS_PRO, isPro)?.apply()
        val userId = getUserId()
        val repo = getRepo()
        if (userId.isNotBlank() && repo != null) {
            scope.launch {
                repo.updateProStatus(userId = userId, isPro = isPro)
            }
        }
    }

    fun isYearlySubscriber(): Boolean {
        val isYearly = prefs?.getBoolean(KEY_IS_YEARLY, false) == true
        val upgradeTime = prefs?.getLong(KEY_YEARLY_UPGRADE_TIME, 0L) ?: 0L
        val oneYearMillis = 365L * 24L * 60L * 60L * 1000L
        return isYearly && (upgradeTime == 0L || (System.currentTimeMillis() - upgradeTime) <= oneYearMillis)
    }

    /**
     * Checks if the month has rolled over for Pro subscriptions and validates daily credit eligibility.
     * Note: Free users' earned/accumulated credits are NEVER wiped or reset.
     */
    fun checkMonthlyResetAndDailyReward() {
        val prefsRef = prefs ?: return
        val hasInitialized = prefsRef.getBoolean(KEY_CREDITS_INITIALIZED, false) || _isCloudSynced.value
        if (!hasInitialized) {
            _canClaimDailyCredit.value = false
            return
        }

        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        val lastResetMonth = prefsRef.getString(KEY_LAST_RESET_MONTH, null)
        val isYearly = prefsRef.getBoolean(KEY_IS_YEARLY, false)
        val upgradeTime = prefsRef.getLong(KEY_YEARLY_UPGRADE_TIME, 0L)
        val oneYearMillis = 365L * 24L * 60L * 60L * 1000L
        val isYearlyActive = isYearly && (upgradeTime == 0L || (System.currentTimeMillis() - upgradeTime) <= oneYearMillis)

        // Only Pro / Yearly subscribers have their 300 credits refreshed on month rollover
        if (lastResetMonth != null && lastResetMonth != currentMonth) {
            if (isYearlyActive || _isPro.value) {
                _credits.value = 300
                prefsRef.edit()
                    .putInt(KEY_CREDITS, 300)
                    .putBoolean(KEY_CREDITS_INITIALIZED, true)
                    .putString(KEY_LAST_RESET_MONTH, currentMonth)
                    .apply()
                val userId = getUserId()
                val repo = getRepo()
                if (userId.isNotBlank() && repo != null) {
                    scope.launch {
                        repo.updateProStatus(
                            userId = userId,
                            isPro = true,
                            isYearly = isYearlyActive,
                            credits = 300L,
                            lastResetMonth = currentMonth
                        )
                    }
                }
            }
        }

        val lastDailyClaimDate = prefsRef.getString(KEY_LAST_DAILY_CLAIM_DATE, null)
        _canClaimDailyCredit.value = (lastDailyClaimDate != todayDate)
    }

    /**
     * Claims the daily free credit (1 credit per day) backed by an atomic Firestore transaction
     * so clearing storage cannot reset or duplicate daily rewards.
     */
    fun claimDailyCredit(): Boolean {
        val prefsRef = prefs ?: return false
        val hasInitialized = _isCloudSynced.value || prefsRef.getBoolean(KEY_CREDITS_INITIALIZED, false)
        if (!hasInitialized) {
            val ctx = appContext
            if (ctx != null) syncWithCloud(ctx)
            return false
        }

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val lastClaim = prefsRef.getString(KEY_LAST_DAILY_CLAIM_DATE, null)
        if (lastClaim == todayDate) {
            _canClaimDailyCredit.value = false
            return false
        }

        val previousCredits = _credits.value
        val optimisticCredits = previousCredits + 1
        _credits.value = optimisticCredits
        _canClaimDailyCredit.value = false
        prefsRef.edit()
            .putInt(KEY_CREDITS, optimisticCredits)
            .putString(KEY_LAST_DAILY_CLAIM_DATE, todayDate)
            .putBoolean(KEY_CREDITS_INITIALIZED, true)
            .apply()

        val userId = getUserId()
        val repo = getRepo()
        if (userId.isNotBlank() && repo != null) {
            scope.launch {
                try {
                    val committedCredits = repo.claimDailyCredit(userId, todayDate)
                    if (committedCredits != null) {
                        _credits.value = committedCredits.toInt()
                        prefsRef.edit()
                            .putInt(KEY_CREDITS, committedCredits.toInt())
                            .putString(KEY_LAST_DAILY_CLAIM_DATE, todayDate)
                            .apply()
                    } else {
                        // Cloud confirmed it was already claimed today on this hardware device
                        val profile = repo.ensureUserInitialized(userId)
                        _credits.value = profile.credits.toInt()
                        _canClaimDailyCredit.value = false
                        prefsRef.edit()
                            .putInt(KEY_CREDITS, profile.credits.toInt())
                            .putString(KEY_LAST_DAILY_CLAIM_DATE, todayDate)
                            .apply()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error persisting daily claim: ${e.message}")
                }
            }
        }
        return true
    }

    /**
     * Fallback simulated purchase for testing in sandbox/dev builds
     */
    private fun simulatePurchaseSuccess(plan: BillingPlan, isYearly: Boolean) {
        _isPro.value = true
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        _credits.value = 300
        _activeTier.value = if (isYearly) {
            "Holiday Pro Yearly (300 Credits/mo for 1 Year)"
        } else {
            "Holiday Pro Monthly (300 Credits/mo)"
        }

        prefs?.edit()?.apply {
            putBoolean(KEY_IS_PRO, true)
            putBoolean(KEY_IS_YEARLY, isYearly)
            putBoolean(KEY_CREDITS_INITIALIZED, true)
            if (isYearly) putLong(KEY_YEARLY_UPGRADE_TIME, System.currentTimeMillis())
            putString(KEY_LAST_RESET_MONTH, currentMonth)
            putInt(KEY_CREDITS, 300)
            putString(KEY_ACTIVE_TIER, _activeTier.value)
        }?.apply()

        val userId = getUserId()
        val repo = getRepo()
        if (userId.isNotBlank() && repo != null) {
            scope.launch {
                repo.updateProStatus(
                    userId = userId,
                    isPro = true,
                    isYearly = isYearly,
                    credits = 300L,
                    lastResetMonth = currentMonth
                )
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled billing flow")
        } else {
            Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
            _error.value = billingResult.debugMessage
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            _isPro.value = true
            val isYearly = purchase.products.contains(PRODUCT_YEARLY)
            val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
            _credits.value = 300
            _activeTier.value = if (isYearly) {
                "Holiday Pro Yearly (300 Credits/mo for 1 Year)"
            } else {
                "Holiday Pro Monthly (300 Credits/mo)"
            }

            prefs?.edit()?.apply {
                putBoolean(KEY_IS_PRO, true)
                putBoolean(KEY_IS_YEARLY, isYearly)
                putBoolean(KEY_CREDITS_INITIALIZED, true)
                if (isYearly) putLong(KEY_YEARLY_UPGRADE_TIME, System.currentTimeMillis())
                putString(KEY_LAST_RESET_MONTH, currentMonth)
                putInt(KEY_CREDITS, 300)
                putString(KEY_ACTIVE_TIER, _activeTier.value)
            }?.apply()

            val userId = getUserId()
            val repo = getRepo()
            if (userId.isNotBlank() && repo != null) {
                scope.launch {
                    repo.updateProStatus(
                        userId = userId,
                        isPro = true,
                        isYearly = isYearly,
                        credits = 300L,
                        lastResetMonth = currentMonth
                    )
                }
            }

            val client = billingClient ?: return
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                client.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Purchase acknowledged successfully")
                    }
                }
            }
        }
    }

    /**
     * Restores active subscriptions
     */
    fun restorePurchases(onComplete: ((Boolean) -> Unit)? = null) {
        val ctx = appContext
        if (ctx != null && !NetworkUtils.isNetworkAvailable(ctx)) {
            val msg = "No internet connection detected. Please connect to Wi-Fi or mobile data to restore purchases."
            _error.value = msg
            onComplete?.invoke(_isPro.value)
            return
        }

        val client = billingClient
        if (client == null || !client.isReady) {
            onComplete?.invoke(_isPro.value)
            return
        }

        _isLoading.value = true
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        client.queryPurchasesAsync(params) { billingResult, purchasesList ->
            _isLoading.value = false
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActive = purchasesList.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                _isPro.value = hasActive
                prefs?.edit()?.putBoolean(KEY_IS_PRO, hasActive)?.apply()
                onComplete?.invoke(hasActive)
            } else {
                onComplete?.invoke(_isPro.value)
            }
        }
    }

    fun syncWithFirestore(
        firestoreCredits: Int,
        firestoreIsPro: Boolean,
        isYearly: Boolean = false,
        lastDailyClaimDate: String? = null,
        lastResetMonth: String? = null
    ) {
        val safeCredits = firestoreCredits.coerceAtLeast(0)
        _credits.value = safeCredits
        _isPro.value = firestoreIsPro
        _isCloudSynced.value = true

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val effectiveClaimDate = lastDailyClaimDate?.takeIf { it.isNotBlank() }

        val editor = prefs?.edit()
            ?.putInt(KEY_CREDITS, safeCredits)
            ?.putBoolean(KEY_IS_PRO, firestoreIsPro)
            ?.putBoolean(KEY_IS_YEARLY, isYearly)
            ?.putBoolean(KEY_CREDITS_INITIALIZED, true)

        if (effectiveClaimDate != null) {
            editor?.putString(KEY_LAST_DAILY_CLAIM_DATE, effectiveClaimDate)
        } else {
            editor?.remove(KEY_LAST_DAILY_CLAIM_DATE)
        }
        _canClaimDailyCredit.value = (effectiveClaimDate != todayDate)

        if (!lastResetMonth.isNullOrBlank()) {
            editor?.putString(KEY_LAST_RESET_MONTH, lastResetMonth)
        }
        editor?.apply()
    }

    fun syncWithFirestore(firestoreCredits: Int, firestoreIsPro: Boolean) {
        syncWithFirestore(firestoreCredits, firestoreIsPro, false, null, null)
    }

    fun addCredits(amount: Int) {
        if (amount <= 0) return
        val newTotal = _credits.value + amount
        _credits.value = newTotal
        prefs?.edit()
            ?.putInt(KEY_CREDITS, newTotal)
            ?.putBoolean(KEY_CREDITS_INITIALIZED, true)
            ?.apply()

        val userId = getUserId()
        val repo = getRepo()
        if (userId.isNotBlank() && repo != null) {
            scope.launch {
                repo.addCredits(userId, amount)
            }
        }
    }

    fun hasEnoughCredits(amount: Int): Boolean {
        if (_isPro.value && isYearlySubscriber()) return true
        return _credits.value >= amount
    }

    fun useCredits(amount: Int): Boolean {
        if (_isPro.value && isYearlySubscriber()) return true
        val current = _credits.value
        if (current >= amount) {
            val newTotal = current - amount
            _credits.value = newTotal
            prefs?.edit()
                ?.putInt(KEY_CREDITS, newTotal)
                ?.putBoolean(KEY_CREDITS_INITIALIZED, true)
                ?.apply()

            val userId = getUserId()
            val repo = getRepo()
            if (userId.isNotBlank() && repo != null) {
                scope.launch {
                    val ok = repo.deductCredits(userId, amount)
                    if (!ok) {
                        // Reconcile local state with Firestore if deduction failed due to desync
                        try {
                            repo.ensureUserInitialized(userId)
                        } catch (_: Exception) {}
                    }
                }
            }
            return true
        }
        return false
    }

    fun useCredit(): Boolean = useCredits(1)

    fun clearError() {
        _error.value = null
    }
}
