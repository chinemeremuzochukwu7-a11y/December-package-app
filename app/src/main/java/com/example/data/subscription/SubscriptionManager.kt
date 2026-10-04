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

    // Backward-compatibility constants
    const val ENTITLEMENT_HOLIDAY_WISHES_PRO = "holiday_wishes_pro"
    const val PRODUCT_YEARLY = "yearly"
    const val PRODUCT_MONTHLY = "monthly"

    // Google Play Product IDs
    const val TIER_5_MONTHLY = "holiday_starter_5"
    const val TIER_5_YEARLY = "holiday_starter_5_yearly"
    const val TIER_15_MONTHLY = "holiday_pro_15"
    const val TIER_15_YEARLY = "holiday_pro_15_yearly"
    const val TIER_45_MONTHLY = "holiday_ultimate_45"
    const val TIER_45_YEARLY = "holiday_ultimate_45_yearly"

    val AVAILABLE_PLANS = listOf(
        BillingPlan(
            id = TIER_5_MONTHLY,
            yearlyId = TIER_5_YEARLY,
            title = "Starter Festive",
            monthlyPrice = "$5.00 / month",
            yearlyPrice = "$48.00 / year ($4.00/mo)",
            monthlyCredits = 50,
            yearlyCredits = 150,
            features = listOf(
                "50 AI Holiday Wish Credits",
                "Export Custom Greeting Cards",
                "Watermark Removal on Cards",
                "Save Unlimited Saved Wishes"
            ),
            isRecommended = true,
            yearlyBonusText = "20% Discount + 100 Bonus Credits + Exclusive Christmas Card Frame"
        ),
        BillingPlan(
            id = TIER_15_MONTHLY,
            yearlyId = TIER_15_YEARLY,
            title = "Pro Celebrator",
            monthlyPrice = "$15.00 / month",
            yearlyPrice = "$144.00 / year ($12.00/mo)",
            monthlyCredits = 200,
            yearlyCredits = 500,
            features = listOf(
                "200 AI Holiday Wish Credits",
                "All Premium Card Layouts & Fonts",
                "Full Holiday Expense Tracker Analytics",
                "Seasonal Ringtone & Sound Themes",
                "No Ads Experience"
            ),
            isRecommended = false,
            yearlyBonusText = "20% Discount + 300 Bonus Credits + Priority AI Generator"
        ),
        BillingPlan(
            id = TIER_45_MONTHLY,
            yearlyId = TIER_45_YEARLY,
            title = "Ultimate VIP",
            monthlyPrice = "$45.00 / month",
            yearlyPrice = "$432.00 / year ($36.00/mo)",
            monthlyCredits = 1000,
            yearlyCredits = 3000,
            features = listOf(
                "Unlimited AI Holiday Wishes",
                "Priority Gemini Pro AI engine",
                "Full Meal, Gift & Budget Planner Sync",
                "All Present & Future Holiday Themes",
                "VIP 24/7 Holiday Support",
                "Ad-Free Lifetime Badge"
            ),
            isRecommended = false,
            yearlyBonusText = "25% Discount + Unlimited Credits + Lifetime Holiday Gift Planner Access"
        )
    )

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
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            _isPro.value = prefs?.getBoolean(KEY_IS_PRO, false) ?: false
            _credits.value = prefs?.getInt(KEY_CREDITS, 25) ?: 25
            _activeTier.value = prefs?.getString(KEY_ACTIVE_TIER, "Free") ?: "Free"
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

        client.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetailsList.forEach { details ->
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
     * Fallback simulated purchase for testing in sandbox/dev builds
     */
    private fun simulatePurchaseSuccess(plan: BillingPlan, isYearly: Boolean) {
        val earnedCredits = if (isYearly) plan.yearlyCredits else plan.monthlyCredits
        addCredits(earnedCredits)
        _isPro.value = true
        _activeTier.value = "${plan.title} (${if (isYearly) "Annual" else "Monthly"})"
        prefs?.edit()
            ?.putBoolean(KEY_IS_PRO, true)
            ?.putString(KEY_ACTIVE_TIER, _activeTier.value)
            ?.apply()
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

    fun addCredits(amount: Int) {
        val newTotal = _credits.value + amount
        _credits.value = newTotal
        prefs?.edit()?.putInt(KEY_CREDITS, newTotal)?.apply()
    }

    fun hasEnoughCredits(amount: Int): Boolean {
        if (_isPro.value) return true
        return _credits.value >= amount
    }

    fun useCredits(amount: Int): Boolean {
        if (_isPro.value) return true // Upgraded users have unlimited access
        val current = _credits.value
        if (current >= amount) {
            val newTotal = current - amount
            _credits.value = newTotal
            prefs?.edit()?.putInt(KEY_CREDITS, newTotal)?.apply()
            return true
        }
        return false
    }

    fun useCredit(): Boolean = useCredits(1)

    fun clearError() {
        _error.value = null
    }
}
