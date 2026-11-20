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
import com.example.BuildConfig
import com.example.R
import com.example.data.firebase.UserFirestoreRepository
import com.example.data.repository.MonetizationManager
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
 * Manages Google Play Billing Client, purchases, entitlements, and profitable AI credit allowances.
 * Only 3 subscription tiers exist ($2.90/mo, $5.90/mo, $35.00/yr) — each with capped monthly credits
 * to guarantee healthy positive margins above Gemini API token costs.
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
    private const val KEY_PLAN_CREDITS = "key_plan_credits"
    private const val KEY_CREDITS_INITIALIZED = "key_credits_initialized"
    private const val KEY_ACTIVE_TIER = "key_active_tier"
    private const val KEY_LAST_RESET_MONTH = "key_last_reset_month"
    private const val KEY_LAST_DAILY_CLAIM_DATE = "key_last_daily_claim_date"
    private const val KEY_IS_YEARLY = "key_is_yearly"
    private const val KEY_YEARLY_UPGRADE_TIME = "key_yearly_upgrade_time"

    // Google Play Console Exact Subscription Product IDs (ONLY 3 PLANS)
    const val PRODUCT_MONTHLY = "holiday_wishes_monthly"            // $2.90/mo -> 150 credits/mo
    const val PRODUCT_PLUS_MONTHLY = "holiday_wishes_plus_monthly"  // $5.90/mo -> 400 credits/mo
    const val PRODUCT_YEARLY = "holiday_wishes_yearly"              // $35.00/yr -> 350 credits/mo (4,200/yr)

    const val STARTER_MONTHLY_CREDITS = 150
    const val PLUS_MONTHLY_CREDITS = 400
    const val YEARLY_MONTHLY_CREDITS = 350

    // Backward-compatibility constants
    const val ENTITLEMENT_HOLIDAY_WISHES_PRO = "holiday_wishes_pro"

    val PLAN_MONTHLY = BillingPlan(
        id = PRODUCT_MONTHLY,
        yearlyId = PRODUCT_YEARLY,
        title = "Starter Monthly",
        monthlyPrice = "$2.90 / month",
        yearlyPrice = "$35.00 / year (~$2.92/mo)",
        monthlyCredits = STARTER_MONTHLY_CREDITS,
        yearlyCredits = YEARLY_MONTHLY_CREDITS * 12,
        features = listOf(
            "🪙 150 AI Credits refreshed every month",
            "Unlock all 1,000+ Pro Christmas, New Year & Season cards",
            "Unlock all 1,000+ Pro Wishes across all categories",
            "Remove all watermarks + Full HD Card Downloads",
            "Ad-Free Experience Across App"
        ),
        isRecommended = false,
        yearlyBonusText = "Starter Plan at $2.90/mo with 150 AI credits refreshed each month. Cancel anytime."
    )

    val PLAN_PLUS_MONTHLY = BillingPlan(
        id = PRODUCT_PLUS_MONTHLY,
        yearlyId = PRODUCT_YEARLY,
        title = "Plus Monthly",
        monthlyPrice = "$5.90 / month",
        yearlyPrice = "$35.00 / year (~$2.92/mo)",
        monthlyCredits = PLUS_MONTHLY_CREDITS,
        yearlyCredits = YEARLY_MONTHLY_CREDITS * 12,
        features = listOf(
            "🪙 400 AI Credits refreshed every month",
            "Unlock all 1,000+ Pro Cards & Pro Wishes",
            "Remove all watermarks + 4 HD Aspect Ratios",
            "100% Ad-Free Experience Across App",
            "Ideal for frequent holiday & family greetings"
        ),
        isRecommended = true,
        yearlyBonusText = "Most Popular! $5.90/mo with 400 AI credits refreshed every month. Cancel anytime."
    )

    val PLAN_YEARLY = BillingPlan(
        id = PRODUCT_YEARLY,
        yearlyId = PRODUCT_YEARLY,
        title = "Annual VIP Pass",
        monthlyPrice = "$35.00 / year",
        yearlyPrice = "$35.00 / year (~$2.92/mo)",
        monthlyCredits = YEARLY_MONTHLY_CREDITS,
        yearlyCredits = YEARLY_MONTHLY_CREDITS * 12,
        features = listOf(
            "✨ 350 AI Credits refreshed EVERY month (4,200 credits/yr)",
            "Save 51% vs Plus Monthly (just ~$2.92/mo billed yearly)",
            "Unlock all 1,000+ Pro Cards (Season, Christmas & New Year)",
            "Unlock all 1,000+ Pro Wishes & Remove Watermarks",
            "Ad-Free for the full year + VIP Gold Themes"
        ),
        isRecommended = true,
        yearlyBonusText = "BEST VALUE: Save 51%! $35.00/year (~$2.92/mo) • 350 fresh AI credits every month."
    )

    val AVAILABLE_PLANS = listOf(PLAN_MONTHLY, PLAN_PLUS_MONTHLY, PLAN_YEARLY)

    private val scope = CoroutineScope(Dispatchers.IO)
    private var billingClient: BillingClient? = null
    private var prefs: SharedPreferences? = null
    private var appContext: Context? = null

    private var pendingPurchaseOnSuccess: (() -> Unit)? = null
    private var pendingPurchaseOnError: ((String) -> Unit)? = null

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

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
            val cachedIsPro = prefsRef.getBoolean(KEY_IS_PRO, false)
            _isPro.value = cachedIsPro
            _activeTier.value = prefsRef.getString(KEY_ACTIVE_TIER, "Free") ?: "Free"
            _credits.value = prefsRef.getInt(KEY_CREDITS, 0)
            MonetizationManager.getInstance(ctx).syncProStatus(
                isPro = cachedIsPro,
                isVip = isYearlySubscriber() || _activeTier.value?.contains("Plus") == true || _activeTier.value?.contains("VIP") == true
            )
            checkMonthlyResetAndDailyReward()
        } else {
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
                MonetizationManager.getInstance(app).syncProStatus(profile.isPro, profile.isYearly)
                checkMonthlyResetAndDailyReward()
                Log.d(TAG, "Synchronized persistent credits from cloud: ${profile.credits}")
            } catch (e: Exception) {
                _isSyncing.value = false
                Log.w(TAG, "Notice syncing persistent credits from cloud: ${e.message}")
            }
        }
    }

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
                    MonetizationManager.getInstance(app).syncProStatus(profile.isPro, profile.isYearly)
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
                    Log.i(TAG, "Billing service not available: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected, will reconnect on demand.")
            }
        })
    }

    private fun queryProducts() {
        val client = billingClient ?: return
        if (!client.isReady) return

        val distinctIds = listOf(PRODUCT_MONTHLY, PRODUCT_PLUS_MONTHLY, PRODUCT_YEARLY)
        val productList = distinctIds.map { sku ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(sku)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
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

    fun planForProductId(productId: String): BillingPlan {
        return when (productId) {
            PRODUCT_YEARLY -> PLAN_YEARLY
            PRODUCT_PLUS_MONTHLY -> PLAN_PLUS_MONTHLY
            else -> PLAN_MONTHLY
        }
    }

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
        val productId = if (isYearly) plan.yearlyId else plan.id
        val effectivePlan = planForProductId(productId)

        if (client == null || !client.isReady) {
            if (BuildConfig.DEBUG) {
                simulatePurchaseSuccess(effectivePlan, isYearly)
                onSuccess()
            } else {
                connectToBilling()
                val msg = "Google Play Billing is initializing or unavailable on this device. Please ensure Google Play Store is active and retry."
                _error.value = msg
                onError(msg)
            }
            return
        }

        val productDetails = productDetailsMap[productId]
        if (productDetails != null) {
            val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken.orEmpty()
            if (offerToken.isBlank()) {
                val msg = "Subscription offer not available on Google Play for $productId."
                _error.value = msg
                onError(msg)
                return
            }

            pendingPurchaseOnSuccess = onSuccess
            pendingPurchaseOnError = onError

            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()
            )

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            com.example.data.ads.AppOpenAdManager.suppressNextForegroundAd(30_000L)
            val result = client.launchBillingFlow(activity, billingFlowParams)
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                pendingPurchaseOnSuccess = null
                pendingPurchaseOnError = null
                val msg = "Could not launch Google Play billing: ${result.debugMessage}"
                _error.value = msg
                onError(msg)
            }
        } else {
            if (BuildConfig.DEBUG) {
                simulatePurchaseSuccess(effectivePlan, isYearly)
                onSuccess()
            } else {
                queryProducts()
                val msg = "Subscription plan ($productId) is not yet available from Google Play. Please verify Play Console product setup and try again."
                _error.value = msg
                onError(msg)
            }
        }
    }

    fun purchaseByProductId(
        activity: Activity,
        productId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val isYearly = productId == PRODUCT_YEARLY
        val plan = planForProductId(productId)
        purchase(activity, plan, isYearly, onSuccess, onError)
    }

    fun setProDirect(isPro: Boolean) {
        if (!BuildConfig.DEBUG && isPro) {
            Log.w(TAG, "Ignored direct Pro elevation in release build; use Play Billing.")
            return
        }
        _isPro.value = isPro
        prefs?.edit()?.putBoolean(KEY_IS_PRO, isPro)?.apply()
        appContext?.let { MonetizationManager.getInstance(it).syncProStatus(isPro, isYearlySubscriber()) }
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

        // Reset monthly allowance strictly to the subscriber's tier quota (150, 400, or 350)
        if (lastResetMonth != null && lastResetMonth != currentMonth) {
            if (isYearlyActive || _isPro.value) {
                val monthlyAllowance = prefsRef.getInt(
                    KEY_PLAN_CREDITS,
                    if (isYearlyActive) YEARLY_MONTHLY_CREDITS else STARTER_MONTHLY_CREDITS
                ).coerceIn(STARTER_MONTHLY_CREDITS, PLUS_MONTHLY_CREDITS)

                _credits.value = monthlyAllowance
                prefsRef.edit()
                    .putInt(KEY_CREDITS, monthlyAllowance)
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
                            credits = monthlyAllowance.toLong(),
                            lastResetMonth = currentMonth
                        )
                    }
                }
            }
        }

        val lastDailyClaimDate = prefsRef.getString(KEY_LAST_DAILY_CLAIM_DATE, null)
        _canClaimDailyCredit.value = (lastDailyClaimDate != todayDate)
    }

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

    private fun activateEntitlement(plan: BillingPlan, isYearly: Boolean) {
        _isPro.value = true
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
        val allowance = plan.monthlyCredits
        _credits.value = allowance
        _activeTier.value = "${plan.title} (${plan.monthlyCredits} Credits/mo)"

        prefs?.edit()?.apply {
            putBoolean(KEY_IS_PRO, true)
            putBoolean(KEY_IS_YEARLY, isYearly)
            putBoolean(KEY_CREDITS_INITIALIZED, true)
            if (isYearly) putLong(KEY_YEARLY_UPGRADE_TIME, System.currentTimeMillis())
            putString(KEY_LAST_RESET_MONTH, currentMonth)
            putInt(KEY_CREDITS, allowance)
            putInt(KEY_PLAN_CREDITS, allowance)
            putString(KEY_ACTIVE_TIER, _activeTier.value)
        }?.apply()

        appContext?.let {
            MonetizationManager.getInstance(it).syncProStatus(
                isPro = true,
                isVip = isYearly || plan.id == PRODUCT_PLUS_MONTHLY
            )
        }

        val userId = getUserId()
        val repo = getRepo()
        if (userId.isNotBlank() && repo != null) {
            scope.launch {
                repo.updateProStatus(
                    userId = userId,
                    isPro = true,
                    isYearly = isYearly,
                    credits = allowance.toLong(),
                    lastResetMonth = currentMonth
                )
            }
        }
    }

    private fun simulatePurchaseSuccess(plan: BillingPlan, isYearly: Boolean) {
        if (!BuildConfig.DEBUG) return
        activateEntitlement(plan, isYearly)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        val successCb = pendingPurchaseOnSuccess
        val errorCb = pendingPurchaseOnError
        pendingPurchaseOnSuccess = null
        pendingPurchaseOnError = null

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            var activated = false
            for (purchase in purchases) {
                if (handlePurchase(purchase)) {
                    activated = true
                }
            }
            if (activated) {
                successCb?.invoke()
            } else {
                errorCb?.invoke("Purchase is pending confirmation from Google Play.")
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled billing flow")
            errorCb?.invoke("Purchase canceled.")
        } else {
            Log.e(TAG, "Purchase failed: ${billingResult.debugMessage}")
            _error.value = billingResult.debugMessage
            errorCb?.invoke("Google Play error: ${billingResult.debugMessage}")
        }
    }

    private fun handlePurchase(purchase: Purchase): Boolean {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return false

        val plan = when {
            purchase.products.contains(PRODUCT_YEARLY) -> PLAN_YEARLY
            purchase.products.contains(PRODUCT_PLUS_MONTHLY) -> PLAN_PLUS_MONTHLY
            else -> PLAN_MONTHLY
        }
        val isYearly = plan.id == PRODUCT_YEARLY

        activateEntitlement(plan, isYearly)

        val client = billingClient ?: return true
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
        return true
    }

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
                val activePurchases = purchasesList.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                val hasActive = activePurchases.isNotEmpty()

                if (hasActive) {
                    _isPro.value = true
                    val activePurchase = activePurchases.first()
                    val isYearly = activePurchase.products.contains(PRODUCT_YEARLY)
                    val restoredPlan = when {
                        isYearly -> PLAN_YEARLY
                        activePurchase.products.contains(PRODUCT_PLUS_MONTHLY) -> PLAN_PLUS_MONTHLY
                        else -> PLAN_MONTHLY
                    }
                    _activeTier.value = "${restoredPlan.title} (${restoredPlan.monthlyCredits} Credits/mo)"
                    prefs?.edit()?.apply {
                        putBoolean(KEY_IS_PRO, true)
                        putBoolean(KEY_IS_YEARLY, isYearly)
                        putInt(KEY_PLAN_CREDITS, restoredPlan.monthlyCredits)
                        putString(KEY_ACTIVE_TIER, _activeTier.value)
                    }?.apply()
                    appContext?.let {
                        MonetizationManager.getInstance(it).syncProStatus(
                            isPro = true,
                            isVip = isYearly || restoredPlan.id == PRODUCT_PLUS_MONTHLY
                        )
                    }
                    // Acknowledge any unacknowledged active purchase
                    activePurchases.filter { !it.isAcknowledged }.forEach { unack ->
                        client.acknowledgePurchase(
                            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(unack.purchaseToken).build()
                        ) {}
                    }
                } else if (!BuildConfig.DEBUG) {
                    val wasPro = _isPro.value
                    _isPro.value = false
                    prefs?.edit()?.putBoolean(KEY_IS_PRO, false)?.putBoolean(KEY_IS_YEARLY, false)?.apply()
                    appContext?.let { MonetizationManager.getInstance(it).syncProStatus(false, false) }
                    if (wasPro) {
                        val userId = getUserId()
                        val repo = getRepo()
                        if (userId.isNotBlank() && repo != null) {
                            scope.launch {
                                repo.updateProStatus(userId = userId, isPro = false, isYearly = false)
                            }
                        }
                    }
                }

                onComplete?.invoke(hasActive || (BuildConfig.DEBUG && _isPro.value))
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

        appContext?.let {
            MonetizationManager.getInstance(it).syncProStatus(
                isPro = firestoreIsPro,
                isVip = isYearly
            )
        }

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

    fun refundCredits(amount: Int) {
        releaseReservedCredits(amount)
    }

    /**
     * Checks credit balance strictly for ALL users (including Pro/Yearly subscribers)
     * so every subscriber uses their profitable monthly allowance (150, 400, or 350 credits/mo).
     */
    fun hasEnoughCredits(amount: Int): Boolean {
        return _credits.value >= amount
    }

    /**
     * Atomically reserves credits locally before an asynchronous Gemini call starts.
     * Call [commitReservedCredits] when Gemini succeeds, or [releaseReservedCredits] on failure.
     */
    @Synchronized
    fun reserveCredits(amount: Int): Boolean {
        if (amount <= 0) return true
        val current = _credits.value
        if (current < amount) return false
        val newTotal = current - amount
        _credits.value = newTotal
        prefs?.edit()
            ?.putInt(KEY_CREDITS, newTotal)
            ?.putBoolean(KEY_CREDITS_INITIALIZED, true)
            ?.apply()
        return true
    }

    /**
     * Commits previously reserved credits to the Firestore cloud ledger after a successful AI response.
     */
    fun commitReservedCredits(amount: Int) {
        if (amount <= 0) return
        val userId = getUserId()
        val repo = getRepo()
        if (userId.isNotBlank() && repo != null) {
            scope.launch {
                val ok = repo.deductCredits(userId, amount)
                if (!ok) {
                    try {
                        repo.ensureUserInitialized(userId)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    /**
     * Releases locally reserved credits if Gemini fails before completion (no Firestore inflation conflict).
     */
    @Synchronized
    fun releaseReservedCredits(amount: Int) {
        if (amount <= 0) return
        val newTotal = _credits.value + amount
        _credits.value = newTotal
        prefs?.edit()
            ?.putInt(KEY_CREDITS, newTotal)
            ?.putBoolean(KEY_CREDITS_INITIALIZED, true)
            ?.apply()
    }

    /**
     * Deducts exact credit cost immediately for ALL users and syncs to Firestore.
     */
    fun useCredits(amount: Int): Boolean {
        if (!reserveCredits(amount)) return false
        commitReservedCredits(amount)
        return true
    }

    fun useCredit(): Boolean = useCredits(1)

    fun clearError() {
        _error.value = null
    }
}
