package com.example

import android.app.Application
import android.util.Log
import com.example.data.ads.AppOpenAdManager
import com.example.data.subscription.SubscriptionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainApplication : Application() {
    private val appScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        try {
            AppOpenAdManager.initialize(this)
        } catch (e: Throwable) {
            Log.w("MainApplication", "App Open Ad setup notice: ${e.message}")
        }
        // Initialize Play Billing in background to prevent any main thread hitch or preview emulator hang
        appScope.launch {
            try {
                SubscriptionManager.initialize(this@MainApplication)
            } catch (e: Throwable) {
                Log.w("MainApplication", "SDK background initialization notice: ${e.message}")
            }
        }
    }
}
