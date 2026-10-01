package com.example.data.subscription

import android.app.Activity
import android.util.Log
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages RevenueCat subscription state, entitlement checking,
 * offerings, and in-app purchase transactions for Holiday Wishes.
 */
object SubscriptionManager {
    private const val TAG = "SubscriptionManager"

    // Primary Pro entitlement identifier
    const val ENTITLEMENT_HOLIDAY_WISHES_PRO = "holiday_wishes_pro"

    // Default product identifiers
    const val PRODUCT_YEARLY = "yearly"
    const val PRODUCT_MONTHLY = "monthly"

    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _customerInfo = MutableStateFlow<CustomerInfo?>(null)
    val customerInfo: StateFlow<CustomerInfo?> = _customerInfo.asStateFlow()

    private val _offerings = MutableStateFlow<Offerings?>(null)
    val offerings: StateFlow<Offerings?> = _offerings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        setupCustomerInfoListener()
        refreshCustomerInfo()
        fetchOfferings()
    }

    /**
     * Listens for real-time entitlement and purchase changes
     */
    private fun setupCustomerInfoListener() {
        try {
            if (Purchases.isConfigured) {
                Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { info ->
                    Log.d(TAG, "CustomerInfo updated in real time")
                    updateCustomerInfoState(info)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register UpdatedCustomerInfoListener", e)
        }
    }

    /**
     * Refreshes customer info and validates 'holiday_wishes_pro' entitlement
     */
    fun refreshCustomerInfo(onComplete: ((Boolean) -> Unit)? = null) {
        if (!Purchases.isConfigured) {
            onComplete?.invoke(false)
            return
        }

        scope.launch {
            _isLoading.value = true
            try {
                val info = Purchases.sharedInstance.awaitCustomerInfo()
                updateCustomerInfoState(info)
                onComplete?.invoke(_isPro.value)
            } catch (e: PurchasesException) {
                val error = e.error
                Log.e(TAG, "Error fetching customer info: ${error.message}")
                _error.value = error.message
                onComplete?.invoke(_isPro.value)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error fetching customer info", e)
                onComplete?.invoke(_isPro.value)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Fetches current offerings from RevenueCat
     */
    fun fetchOfferings() {
        if (!Purchases.isConfigured) return

        scope.launch {
            try {
                val currentOfferings = Purchases.sharedInstance.awaitOfferings()
                _offerings.value = currentOfferings
                Log.d(TAG, "Fetched offerings: ${currentOfferings.current?.identifier}")
            } catch (e: PurchasesException) {
                val error = e.error
                Log.e(TAG, "Error fetching offerings: ${error.message}")
                _error.value = error.message
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error fetching offerings", e)
            }
        }
    }

    /**
     * Purchases a package (monthly or yearly) using coroutines
     */
    fun purchase(
        activity: Activity,
        packageToPurchase: Package,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!Purchases.isConfigured) {
            onError("Purchases SDK is not configured")
            return
        }

        scope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val params = PurchaseParams.Builder(activity, packageToPurchase).build()
                val purchaseResult = Purchases.sharedInstance.awaitPurchase(params)
                updateCustomerInfoState(purchaseResult.customerInfo)

                if (_isPro.value) {
                    onSuccess()
                } else {
                    onError("Purchase completed but Pro entitlement is inactive")
                }
            } catch (e: PurchasesException) {
                val error = e.error
                Log.e(TAG, "Purchase failed: ${error.message}, code=${error.code}")
                val message = error.message
                _error.value = message
                onError(message)
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Unknown purchase error"
                Log.e(TAG, "Purchase exception: $msg", e)
                _error.value = msg
                onError(msg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Restores previous purchases across Google Play accounts
     */
    fun restore(
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!Purchases.isConfigured) {
            onError("Purchases SDK is not configured")
            return
        }

        scope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val restoredInfo = Purchases.sharedInstance.awaitRestore()
                updateCustomerInfoState(restoredInfo)
                val proActive = _isPro.value
                onSuccess(proActive)
            } catch (e: PurchasesException) {
                val error = e.error
                Log.e(TAG, "Restore error: ${error.message}")
                val msg = error.message
                _error.value = msg
                onError(msg)
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Unknown restore error"
                _error.value = msg
                onError(msg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates reactive customer info and computes entitlement state
     */
    private fun updateCustomerInfoState(info: CustomerInfo) {
        _customerInfo.value = info
        val hasPro = info.entitlements[ENTITLEMENT_HOLIDAY_WISHES_PRO]?.isActive == true
        _isPro.value = hasPro
        Log.d(TAG, "holiday_wishes_pro entitlement isActive: $hasPro")
    }

    fun clearError() {
        _error.value = null
    }

    /**
     * Helper to get Monthly package from current offering
     */
    fun getMonthlyPackage(): Package? {
        val current = _offerings.value?.current ?: return null
        return current.monthly ?: current.availablePackages.firstOrNull {
            it.identifier.contains("monthly", ignoreCase = true) ||
            it.product.id.contains("monthly", ignoreCase = true)
        }
    }

    /**
     * Helper to get Yearly package from current offering
     */
    fun getYearlyPackage(): Package? {
        val current = _offerings.value?.current ?: return null
        return current.annual ?: current.availablePackages.firstOrNull {
            it.identifier.contains("yearly", ignoreCase = true) ||
            it.identifier.contains("annual", ignoreCase = true) ||
            it.product.id.contains("yearly", ignoreCase = true)
        }
    }
}
