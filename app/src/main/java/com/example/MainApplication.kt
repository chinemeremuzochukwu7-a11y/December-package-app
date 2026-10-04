package com.example

import android.app.Application
import android.util.Log
import com.example.data.ads.AdManager
import com.example.data.subscription.SubscriptionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainApplication : Application() {
    private val appScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        try {
            // Ensure WebView HTTP cache directories exist to prevent Chromium directory enumeration warnings
            val jsCache = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
            if (!jsCache.exists()) jsCache.mkdirs()
            val wasmCache = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm")
            if (!wasmCache.exists()) wasmCache.mkdirs()
        } catch (_: Throwable) {}

        // Initialize Play Billing and AdMob in background to prevent any main thread hitch or preview emulator hang
        appScope.launch {
            try {
                SubscriptionManager.initialize(this@MainApplication)
                AdManager.initialize(this@MainApplication)
            } catch (e: Throwable) {
                Log.w("MainApplication", "SDK background initialization notice: ${e.message}")
            }
        }
    }
}
