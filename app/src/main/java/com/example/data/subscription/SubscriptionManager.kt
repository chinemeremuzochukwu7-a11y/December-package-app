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
import com.android.billingclient.api.ConsumeParams
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

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

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _credits = MutableStateFlow(25) // 25 free starter credits
    val credits: StateFlow<Int> = _credits.asStateFlow()

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
        val dbId = try { context.getString(R.string.firestore_database_id) } catch (_: Exception) { null }
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            _isPro.value = prefs?.getBoolean(KEY_IS_PRO, false) ?: false
            _credits.value = prefs?.getInt(KEY_CREDITS, 25) ?: 25
            _activeTier.value = prefs?.getString(KEY_ACTIVE_TIER, "Free") ?: "Free"
        }
        val userId = com.example.data.firebase.UserFirestoreRepository.getOrCreateUserId(context)
        prefs?.edit()?.putString("current_user_id", userId)?.apply()
        if (dbId != null) {
            prefs?.edit()?.putString("firestore_db_id", dbId)?.apply()
        }

        // Check and execute monthly credit reset and daily free reward eligibility
        checkMonthlyResetAndDailyReward()

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
    }

    fun isYearlySubscriber(): Boolean {
        val isYearly = prefs?.getBoolean(KEY_IS_YEARLY, false) == true
        val upgradeTime = prefs?.getLong(KEY_YEARLY_UPGRADE_TIME, 0L) ?: 0L
        val oneYearMillis = 365L * 24L * 60L * 60L * 1000L
        return isYearly && (upgradeTime == 0L || (System.currentTimeMillis() - upgradeTime) <= oneYearMillis)
    }

    /**
     * Checks if the month has rolled over. If so, resets user credits:
     * - Yearly subscribers get 300 credits every month for 1 full year.
     * - Monthly Pro subscribers get 300 credits every month.
     * - Free users reset to 25 starter credits.
     * Also checks if user is eligible to claim their 1 daily free credit.
     */
    fun checkMonthlyResetAndDailyReward() {
        val prefsRef = prefs ?: return
        val currentMonth = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US).format(java.util.Date())
        val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

        val lastResetMonth = prefsRef.getString(KEY_LAST_RESET_MONTH, null)
        val isYearly = prefsRef.getBoolean(KEY_IS_YEARLY, false)
        val upgradeTime = prefsRef.getLong(KEY_YEARLY_UPGRADE_TIME, 0L)
        val oneYearMillis = 365L * 24L * 60L * 60L * 1000L
        val isYearlyActive = isYearly && (upgradeTime == 0L || (System.currentTimeMillis() - upgradeTime) <= oneYearMillis)

        if (lastResetMonth == null || lastResetMonth != currentMonth) {
            if (isYearlyActive) {
                // For yearly upgrades: always reset and add 300 fresh credits every month for 1 year
                setCreditsDirect(300)
            } else if (_isPro.value) {
                setCreditsDirect(300)
            } else {
                setCreditsDirect(25)
            }
            prefsRef.edit().putString(KEY_LAST_RESET_MONTH, currentMonth).apply()
        }

        // Daily Free Credit claim check (1 free credit per day upon entering)
        val lastDailyClaimDate = prefsRef.getString(KEY_LAST_DAILY_CLAIM_DATE, null)
        _canClaimDailyCredit.value = (lastDailyClaimDate != todayDate)
    }

    /**
     * Claims the daily free credit (1 credit per day)
     */
    fun claimDailyCredit(): Boolean {
        val prefsRef = prefs ?: return false
        val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val lastClaim = prefsRef.getString(KEY_LAST_DAILY_CLAIM_DATE, null)
        if (lastClaim != todayDate) {
            addCredits(1)
            prefsRef.edit().putString(KEY_LAST_DAILY_CLAIM_DATE, todayDate).apply()
            _canClaimDailyCredit.value = false
            return true
        }
        return false
    }

    private fun setCreditsDirect(amount: Int) {
        _credits.value = amount
        prefs?.edit()?.putInt(KEY_CREDITS, amount)?.apply()
        val userId = prefs?.getString("current_user_id", null)
            ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseId = prefs?.getString("firestore_db_id", null)
            val db = if (databaseId != null) com.google.firebase.firestore.FirebaseFirestore.getInstance(databaseId)
            else com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(userId).update("credits", amount)
        }
    }

    /**
     * Fallback simulated purchase for testing in sandbox/dev builds
     */
    private fun simulatePurchaseSuccess(plan: BillingPlan, isYearly: Boolean) {
        _isPro.value = true
        val currentMonth = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US).format(java.util.Date())
        if (isYearly) {
            _credits.value = 300
            _activeTier.value = "Holiday Pro Yearly (300 Credits/mo for 1 Year)"
            prefs?.edit()
                ?.putBoolean(KEY_IS_PRO, true)
                ?.putBoolean(KEY_IS_YEARLY, true)
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
                ?.putString(KEY_LAST_RESET_MONTH, currentMonth)
                ?.putInt(KEY_CREDITS, 300)
                ?.putString(KEY_ACTIVE_TIER, _activeTier.value)
                ?.apply()
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
            val currentMonth = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US).format(java.util.Date())
            if (isYearly) {
                _credits.value = 300
                _activeTier.value = "Holiday Pro Yearly (300 Credits/mo for 1 Year)"
                prefs?.edit()
                    ?.putBoolean(KEY_IS_PRO, true)
                    ?.putBoolean(KEY_IS_YEARLY, true)
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
                    ?.putString(KEY_LAST_RESET_MONTH, currentMonth)
                    ?.putInt(KEY_CREDITS, 300)
                    ?.putString(KEY_ACTIVE_TIER, _activeTier.value)
                    ?.apply()
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

    fun syncWithFirestore(firestoreCredits: Int, firestoreIsPro: Boolean) {
        _credits.value = firestoreCredits
        _isPro.value = firestoreIsPro
        prefs?.edit()
            ?.putInt(KEY_CREDITS, firestoreCredits)
            ?.putBoolean(KEY_IS_PRO, firestoreIsPro)
            ?.apply()
    }

    fun addCredits(amount: Int) {
        val newTotal = _credits.value + amount
        _credits.value = newTotal
        prefs?.edit()?.putInt(KEY_CREDITS, newTotal)?.apply()
        // Also sync to Firestore
        val userId = prefs?.getString("current_user_id", null)
            ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseId = prefs?.getString("firestore_db_id", null)
            val db = if (databaseId != null) com.google.firebase.firestore.FirebaseFirestore.getInstance(databaseId)
            else com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(userId)
                .update("credits", com.google.firebase.firestore.FieldValue.increment(amount.toLong()))
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
            prefs?.edit()?.putInt(KEY_CREDITS, newTotal)?.apply()
            // Also deduct from Firestore
            val userId = prefs?.getString("current_user_id", null)
                ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val databaseId = prefs?.getString("firestore_db_id", null)
                val db = if (databaseId != null) com.google.firebase.firestore.FirebaseFirestore.getInstance(databaseId)
                else com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("users").document(userId)
                    .update("credits", com.google.firebase.firestore.FieldValue.increment(-amount.toLong()))
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
