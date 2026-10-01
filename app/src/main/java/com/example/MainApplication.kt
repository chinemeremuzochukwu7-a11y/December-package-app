package com.example

import android.app.Application
import android.util.Log
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            Purchases.logLevel = LogLevel.DEBUG
            Purchases.configure(
                PurchasesConfiguration.Builder(this, "test_azGWOTLBdjumFxzatKlHGgzBgUK")
                    .build()
            )
        } catch (e: Throwable) {
            Log.e("MainApplication", "RevenueCat Purchases initialization error: ${e.message}", e)
        }
    }
}
