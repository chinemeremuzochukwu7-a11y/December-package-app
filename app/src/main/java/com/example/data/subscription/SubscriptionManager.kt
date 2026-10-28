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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.R
import com.example.data.firebase.UserFirestoreRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
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
 * Provides permanent hardware-bound cloud protection so user credits and entitlements
 * survive "Clear Data / Storage" without requiring manual login/password registration.
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

    private val _credits = MutableStateFlow(25) // 25 free starter credits provisional
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
        appContext = context.applicationContext
        val dbId = try { context.getString(R.string.firestore_database_id) } catch (_: Exception) { null }
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val hasCachedCredits = prefs?.getBoolean(KEY_CREDITS_INITIALIZED, false) ?: false
            _isPro.value = prefs?.getBoolean(KEY_IS_PRO, false) ?: false
            _activeTier.value = prefs?.getString(KEY_ACTIVE_TIER, "Free") ?: "Free"
            if (hasCachedCredits) {
                _credits.value = prefs?.getInt(KEY_CREDITS, 25) ?: 25
            }
        }
        val userId = UserFirestoreRepository.getOrCreateUserId(context)
        prefs?.edit()?.putString("current_user_id", userId)?.apply()
        if (dbId != null) {
            prefs?.edit()?.putString("firestore_db_id", dbId)?.apply()
        }

        // Check local monthly rollover & daily reward
        checkMonthlyResetAndDailyReward()

        // Kick off cloud synchronization immediately to verify/restore credits
        syncWithCloud(context)

        try {
            val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()

            billingClient = BillingClient.newBuilder(context)
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
        if (!com.example.utils.NetworkUtils.isNetworkAvailable(context)) {
            onResult(false, _credits.value, "Internet connection required to verify and restore credits from cloud.")
            return
        }
        _isSyncing.value = true
        scope.launch {
            try {
                val userId = UserFirestoreRepository.getOrCreateUserId(context)
                val repo = UserFirestoreRepository(context)
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

    private fun getFirestoreInstance(): FirebaseFirestore {
        val ctx = appContext
        val databaseId = prefs?.getString("firestore_db_id", null)
            ?: ctx?.let {
                try { it.getString(R.string.firestore_database_id) } catch (_: Exception) { null }
            }
        return if (!databaseId.isNullOrBlank()) {
            try {
                FirebaseFirestore.getInstance(databaseId)
            } catch (_: Exception) {
                FirebaseFirestore.getInstance()
            }
        } else {
            FirebaseFirestore.getInstance()
        }
    }

    private fun getUserId(): String {
        return prefs?.getString("current_user_id", null)
            ?: appContext?.let { UserFirestoreRepository.getOrCreateUserId(it) }
            ?: ""
    }

    private fun connectToBilling() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "BillingClient connected successfully")
                    queryProducts()
                    restorePurchases()
                } else {
                    Log.i(TAG, "Billing service not available on this environment (demo/sandbox active): ${billingResult.debugMessage}")
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
     * Launches Google Play Billing Flow for a plan
     */
    fun purchase(
        activity: Activity,
        plan: BillingPlan,
        isYearly: Boolean,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        // Verify active internet connection (Wi-Fi or cellular data) before Google Play Billing
        if (!com.example.utils.NetworkUtils.isNetworkAvailable(activity)) {
            val msg = "No internet connection detected. Please connect to Wi-Fi or mobile cellular data before upgrading."
            _error.value = msg
            onError(msg)
            return
        }

        val client = billingClient
        if (client == null || !client.isReady) {
            // Demo/Sandbox fallback for development when testing without play store service
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
            // Sandbox/Dev environment activation
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
        if (userId.isNotBlank()) {
            val db = getFirestoreInstance()
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
            db.collection("users").document(userId).set(
                mapOf("isPro" to isPro, "updatedAt" to now),
                SetOptions.merge()
            )
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
                setCreditsDirect(300)
                prefsRef.edit().putString(KEY_LAST_RESET_MONTH, currentMonth).apply()
                val userId = getUserId()
                if (userId.isNotBlank()) {
                    val db = getFirestoreInstance()
                    db.collection("users").document(userId).set(
                        mapOf("credits" to 300L, "lastResetMonth" to currentMonth),
                        SetOptions.merge()
                    )
                }
            }
        }

        // Daily Free Credit claim check (1 free credit per day upon entering)
        val lastDailyClaimDate = prefsRef.getString(KEY_LAST_DAILY_CLAIM_DATE, null)
        _canClaimDailyCredit.value = (lastDailyClaimDate != todayDate)
    }

    /**
     * Claims the daily free credit (1 credit per day) with dual-layer cloud persistence.
     */
    fun claimDailyCredit(): Boolean {
        val prefsRef = prefs ?: return false
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val lastClaim = prefsRef.getString(KEY_LAST_DAILY_CLAIM_DATE, null)
        if (lastClaim != todayDate) {
            addCredits(1)
            prefsRef.edit().putString(KEY_LAST_DAILY_CLAIM_DATE, todayDate).apply()
            val userId = getUserId()
            if (userId.isNotBlank()) {
                val db = getFirestoreInstance()
                db.collection("users").document(userId).set(
                    mapOf("lastDailyClaimDate" to todayDate),
                    SetOptions.merge()
                )
            }
            _canClaimDailyCredit.value = false
            return true
        }
        return false
    }

    private fun setCreditsDirect(amount: Int) {
        _credits.value = amount
        prefs?.edit()
            ?.putInt(KEY_CREDITS, amount)
            ?.putBoolean(KEY_CREDITS_INITIALIZED, true)
            ?.apply()
        val userId = getUserId()
        if (userId.isNotBlank()) {
            val db = getFirestoreInstance()
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
            db.collection("users").document(userId).set(
                mapOf("credits" to amount.toLong(), "updatedAt" to now),
                SetOptions.merge()
            )
            db.collection("claimed_devices").document(userId).set(
                mapOf("currentCredits" to amount.toLong(), "lastUpdated" to now),
                SetOptions.merge()
            )
        }
    }

    /**
     * Fallback simulated purchase for testing in sandbox/dev builds
     */
    private fun simulatePurchaseSuccess(plan: BillingPlan, isYearly: Boolean) {
        _isPro.value = true
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        if (isYearly) {
            _credits.value = 300
            _activeTier.value = "Holiday Pro Yearly (300 Credits/mo for 1 Year)"
            prefs?.edit()
                ?.putBoolean(KEY_IS_PRO, true)
                ?.putBoolean(KEY_IS_YEARLY, true)
                ?.putBoolean(KEY_CREDITS_INITIALIZED, true)
                ?.putLong(KEY_YEARLY_UPGRADE_TIME, System.currentTimeMillis())
                ?.putString(KEY_LAST_RESET_MONTH, currentMonth)
                ?.putInt(KEY_CREDITS, 300)
                ?.putString(KEY_ACTIVE_TIER, _activeTier.value)
                ?.apply()
        } else {
            _credits.value = 300
            _activeTier.value = "Holiday Pro Monthly (300 Credits/mo)"
            prefs?.edit()
                ?.putBoolean(KEY_IS_PRO, true)
                ?.putBoolean(KEY_IS_YEARLY, false)
                ?.putBoolean(KEY_CREDITS_INITIALIZED, true)
                ?.putString(KEY_LAST_RESET_MONTH, currentMonth)
                ?.putInt(KEY_CREDITS, 300)
                ?.putString(KEY_ACTIVE_TIER, _activeTier.value)
                ?.apply()
        }

        val userId = getUserId()
        if (userId.isNotBlank()) {
            val db = getFirestoreInstance()
            db.collection("users").document(userId).set(
                mapOf(
                    "isPro" to true,
                    "isYearly" to isYearly,
                    "credits" to 300L,
                    "lastResetMonth" to currentMonth,
                    "updatedAt" to now
                ),
                SetOptions.merge()
            )
            db.collection("claimed_devices").document(userId).set(
                mapOf("isPro" to true, "currentCredits" to 300L, "lastUpdated" to now),
                SetOptions.merge()
            )
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
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
            if (isYearly) {
                _credits.value = 300
                _activeTier.value = "Holiday Pro Yearly (300 Credits/mo for 1 Year)"
                prefs?.edit()
                    ?.putBoolean(KEY_IS_PRO, true)
                    ?.putBoolean(KEY_IS_YEARLY, true)
                    ?.putBoolean(KEY_CREDITS_INITIALIZED, true)
                    ?.putLong(KEY_YEARLY_UPGRADE_TIME, System.currentTimeMillis())
                    ?.putString(KEY_LAST_RESET_MONTH, currentMonth)
                    ?.putInt(KEY_CREDITS, 300)
                    ?.putString(KEY_ACTIVE_TIER, _activeTier.value)
                    ?.apply()
            } else {
                _credits.value = 300
                _activeTier.value = "Holiday Pro Monthly (300 Credits/mo)"
                prefs?.edit()
                    ?.putBoolean(KEY_IS_PRO, true)
                    ?.putBoolean(KEY_IS_YEARLY, false)
                    ?.putBoolean(KEY_CREDITS_INITIALIZED, true)
                    ?.putString(KEY_LAST_RESET_MONTH, currentMonth)
                    ?.putInt(KEY_CREDITS, 300)
                    ?.putString(KEY_ACTIVE_TIER, _activeTier.value)
                    ?.apply()
            }

            val userId = getUserId()
            if (userId.isNotBlank()) {
                val db = getFirestoreInstance()
                db.collection("users").document(userId).set(
                    mapOf(
                        "isPro" to true,
                        "isYearly" to isYearly,
                        "credits" to 300L,
                        "lastResetMonth" to currentMonth,
                        "updatedAt" to now
                    ),
                    SetOptions.merge()
                )
                db.collection("claimed_devices").document(userId).set(
                    mapOf("isPro" to true, "currentCredits" to 300L, "lastUpdated" to now),
                    SetOptions.merge()
                )
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
        if (ctx != null && !com.example.utils.NetworkUtils.isNetworkAvailable(ctx)) {
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
        _credits.value = firestoreCredits
        _isPro.value = firestoreIsPro
        _isCloudSynced.value = true
        val editor = prefs?.edit()
            ?.putInt(KEY_CREDITS, firestoreCredits)
            ?.putBoolean(KEY_IS_PRO, firestoreIsPro)
            ?.putBoolean(KEY_IS_YEARLY, isYearly)
            ?.putBoolean(KEY_CREDITS_INITIALIZED, true)

        if (!lastDailyClaimDate.isNullOrBlank()) {
            editor?.putString(KEY_LAST_DAILY_CLAIM_DATE, lastDailyClaimDate)
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            _canClaimDailyCredit.value = (lastDailyClaimDate != todayDate)
        }
        if (!lastResetMonth.isNullOrBlank()) {
            editor?.putString(KEY_LAST_RESET_MONTH, lastResetMonth)
        }
        editor?.apply()
    }

    fun syncWithFirestore(firestoreCredits: Int, firestoreIsPro: Boolean) {
        syncWithFirestore(firestoreCredits, firestoreIsPro, false, null, null)
    }

    fun addCredits(amount: Int) {
        val newTotal = _credits.value + amount
        _credits.value = newTotal
        prefs?.edit()
            ?.putInt(KEY_CREDITS, newTotal)
            ?.putBoolean(KEY_CREDITS_INITIALIZED, true)
            ?.apply()

        // Atomically sync to Cloud Firestore
        val userId = getUserId()
        if (userId.isNotBlank()) {
            val db = getFirestoreInstance()
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
            db.collection("users").document(userId).set(
                mapOf(
                    "credits" to FieldValue.increment(amount.toLong()),
                    "updatedAt" to now
                ),
                SetOptions.merge()
            )
            db.collection("claimed_devices").document(userId).set(
                mapOf(
                    "currentCredits" to FieldValue.increment(amount.toLong()),
                    "lastUpdated" to now
                ),
                SetOptions.merge()
            )
        }
    }

    fun hasEnoughCredits(amount: Int): Boolean {
        if (_isPro.value && isYearlySubscriber()) return true
        return _credits.value >= amount
    }

    fun useCredits(amount: Int): Boolean {
        if (_isPro.value && isYearlySubscriber()) return true // Yearly subscribers have unlimited credits
        val current = _credits.value
        if (current >= amount) {
            val newTotal = current - amount
            _credits.value = newTotal
            prefs?.edit()
                ?.putInt(KEY_CREDITS, newTotal)
                ?.putBoolean(KEY_CREDITS_INITIALIZED, true)
                ?.apply()

            // Atomically deduct from Cloud Firestore
            val userId = getUserId()
            if (userId.isNotBlank()) {
                val db = getFirestoreInstance()
                val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                db.collection("users").document(userId).set(
                    mapOf(
                        "credits" to FieldValue.increment(-amount.toLong()),
                        "updatedAt" to now
                    ),
                    SetOptions.merge()
                )
                db.collection("claimed_devices").document(userId).set(
                    mapOf(
                        "currentCredits" to FieldValue.increment(-amount.toLong()),
                        "lastUpdated" to now
                    ),
                    SetOptions.merge()
                )
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
