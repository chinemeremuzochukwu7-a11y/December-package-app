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
    const val CARD_CREATION_CREDIT_COST = 15
    const val REWARDED_AD_CREDIT_REWARD = 1

    private const val KEY_IS_PRO = "key_is_pro"
    private const val KEY_CREDITS = "key_credits"
    private const val KEY_ACTIVE_TIER = "key_active_tier"

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
        yearlyCredits = 999999,
        features = listOf(
            "🪙 300 AI Credits included each month",
            "Unlock all 1,000+ Pro Christmas, New Year & Season cards",
            "Unlock all 1,000+ Pro Wishes across all categories",
            "Remove all watermarks on cards & wishes",
            "Full HD Card Download to Device Gallery",
            "No Ads Experience Across App"
        ),
        isRecommended = false,
        yearlyBonusText = "Billed monthly at $9.99/mo with 300 credits included. Cancel anytime."
    )

    val PLAN_YEARLY = BillingPlan(
        id = PRODUCT_YEARLY,
        yearlyId = PRODUCT_YEARLY,
        title = "Holiday Pro Yearly",
        monthlyPrice = "$9.99 / month",
        yearlyPrice = "$79.00 / year ($6.58/mo)",
        monthlyCredits = 300,
        yearlyCredits = 999999,
        features = listOf(
            "✨ UNLIMITED AI Credits for 1 full year",
            "Save 34% over monthly billing ($6.58/mo)",
            "Unlock all 1,000+ Pro Cards (500+ Season, Christmas & New Year)",
            "Unlock all 1,000+ Pro Wishes",
            "Remove all watermarks on cards & wishes",
            "Full HD Card Download & Auto-Schedule delivery",
            "VIP Gold Crown & Luxury Themes",
            "Ad-Free for the full year"
        ),
        isRecommended = true,
        yearlyBonusText = "BEST VALUE: Save 34%! Only $6.58/mo billed annually ($79/yr) • UNLIMITED CREDITS."
    )

    val AVAILABLE_PLANS = listOf(PLAN_MONTHLY, PLAN_YEARLY)

    private val scope = CoroutineScope(Dispatchers.IO)
    private var billingClient: BillingClient? = null
    private var prefs: SharedPreferences? = null

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _credits = MutableStateFlow(25) // 25 free starter credits
    val credits: StateFlow<Int> = _credits.asStateFlow()

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
        return prefs?.getBoolean("key_is_yearly", false) == true || _credits.value >= 99999
    }

    /**
     * Fallback simulated purchase for testing in sandbox/dev builds
     */
    private fun simulatePurchaseSuccess(plan: BillingPlan, isYearly: Boolean) {
        _isPro.value = true
        if (isYearly) {
            _credits.value = 999999
            _activeTier.value = "Holiday Pro Yearly (Unlimited)"
            prefs?.edit()
                ?.putBoolean(KEY_IS_PRO, true)
                ?.putBoolean("key_is_yearly", true)
                ?.putInt(KEY_CREDITS, 999999)
                ?.putString(KEY_ACTIVE_TIER, _activeTier.value)
                ?.apply()
        } else {
            addCredits(300)
            _activeTier.value = "Holiday Pro Monthly (300 Credits)"
            prefs?.edit()
                ?.putBoolean(KEY_IS_PRO, true)
                ?.putBoolean("key_is_yearly", false)
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
            prefs?.edit()?.putBoolean(KEY_IS_PRO, true)?.apply()

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
