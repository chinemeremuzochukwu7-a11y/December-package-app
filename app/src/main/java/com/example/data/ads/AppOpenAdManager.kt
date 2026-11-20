package com.example.data.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.example.BuildConfig
import com.example.MainActivity
import com.example.data.subscription.SubscriptionManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.lang.ref.WeakReference

/**
 * Manages Google AdMob App Open Ads cleanly across cold starts and foreground resumes.
 *
 * Production App Open Ad Unit ID: ca-app-pub-6322953088287505/6075368347
 * Uses Google's official test App Open ID in DEBUG builds to avoid invalid-traffic strikes.
 *
 * Safeguards:
 * - Skipped automatically for Pro subscribers.
 * - Discards expired ads older than 4 hours per AdMob policy.
 * - Never overlaps with Rewarded, Interstitial, external Share sheets, Photo Picker, or Billing flows.
 */
object AppOpenAdManager : Application.ActivityLifecycleCallbacks {

    private const val TAG = "AppOpenAdManager"

    // Production App Open Ad Unit ID
    const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-6322953088287505/6075368347"

    // Official Google AdMob Debug Test App Open Ad Unit ID
    private const val TEST_APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"

    private const val FOUR_HOURS_MS = 4L * 60L * 60L * 1000L
    private const val COLD_START_WINDOW_MS = 5_000L
    private const val MIN_SHOW_INTERVAL_MS = 30_000L

    fun activeAppOpenAdUnitId(): String =
        if (BuildConfig.DEBUG) TEST_APP_OPEN_AD_UNIT_ID else APP_OPEN_AD_UNIT_ID

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    @Volatile
    var isShowingAd = false
        private set

    private var loadTimeMs: Long = 0L
    private var appCreatedAtMs: Long = 0L
    private var lastShownAtMs: Long = 0L
    private var hasShownColdStartAd = false

    private var appContext: Context? = null
    private var isInitialized = false
    private var startedActivityCount = 0
    private var currentActivityRef: WeakReference<Activity>? = null

    @Volatile
    private var suppressNextForegroundUntilMs: Long = 0L

    fun initialize(application: Application) {
        if (isInitialized) return
        isInitialized = true
        appContext = application.applicationContext
        appCreatedAtMs = System.currentTimeMillis()
        application.registerActivityLifecycleCallbacks(this)

        AdManager.initialize(application)
        if (!SubscriptionManager.isPro.value) {
            loadAd(application)
        }
    }

    /**
     * Suppresses App Open ad on the next foreground transition (e.g., returning from
     * Android Share Sheet, Photo Picker, or Google Play Billing).
     */
    fun suppressNextForegroundAd(durationMs: Long = 10_000L) {
        suppressNextForegroundUntilMs = System.currentTimeMillis() + durationMs
    }

    fun loadAd(context: Context) {
        if (SubscriptionManager.isPro.value) return
        if (isLoadingAd || isAdAvailable()) return

        val safeContext = context.applicationContext ?: context
        isLoadingAd = true

        try {
            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                safeContext,
                activeAppOpenAdUnitId(),
                request,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTimeMs = System.currentTimeMillis()
                        Log.d(TAG, "App Open ad loaded")

                        // If loaded cleanly during initial app launch window and app is foregrounded, show it
                        val activeActivity = currentActivityRef?.get()
                        val withinLaunchWindow = (System.currentTimeMillis() - appCreatedAtMs) <= COLD_START_WINDOW_MS
                        if (!hasShownColdStartAd &&
                            withinLaunchWindow &&
                            activeActivity != null &&
                            startedActivityCount > 0 &&
                            !activeActivity.isFinishing &&
                            !activeActivity.isDestroyed
                        ) {
                            hasShownColdStartAd = true
                            showAdIfAvailable(activeActivity)
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isLoadingAd = false
                        Log.d(TAG, "App Open ad failed to load: ${loadAdError.message}")
                    }
                }
            )
        } catch (t: Throwable) {
            isLoadingAd = false
            Log.w(TAG, "App Open ad load notice: ${t.message}")
        }
    }

    private fun isAdAvailable(): Boolean {
        val ad = appOpenAd ?: return false
        val notExpired = (System.currentTimeMillis() - loadTimeMs) < FOUR_HOURS_MS
        if (!notExpired) {
            appOpenAd = null
            return false
        }
        return true
    }

    fun showAdIfAvailable(activity: Activity, onComplete: () -> Unit = {}) {
        if (SubscriptionManager.isPro.value) {
            onComplete()
            return
        }
        if (isShowingAd || AdManager.isFullScreenAdShowing) {
            onComplete()
            return
        }
        if (System.currentTimeMillis() < suppressNextForegroundUntilMs) {
            suppressNextForegroundUntilMs = 0L
            onComplete()
            return
        }
        if (lastShownAtMs > 0L && (System.currentTimeMillis() - lastShownAtMs) < MIN_SHOW_INTERVAL_MS) {
            onComplete()
            return
        }
        if (activity.isFinishing || activity.isDestroyed) {
            onComplete()
            return
        }

        val ad = appOpenAd
        if (ad == null || !isAdAvailable()) {
            loadAd(activity)
            onComplete()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                AdManager.onFullScreenAdClosed()
                loadAd(activity)
                onComplete()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false
                AdManager.onFullScreenAdClosed()
                loadAd(activity)
                onComplete()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
                lastShownAtMs = System.currentTimeMillis()
                AdManager.onFullScreenAdOpened()
            }
        }

        isShowingAd = true
        hasShownColdStartAd = true
        ad.show(activity)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is MainActivity) {
            currentActivityRef = WeakReference(activity)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        val wasInBackground = startedActivityCount == 0
        startedActivityCount++

        if (activity is MainActivity && !isShowingAd) {
            currentActivityRef = WeakReference(activity)
            if (wasInBackground) {
                showAdIfAvailable(activity)
            }
        }
    }

    override fun onActivityResumed(activity: Activity) {
        if (activity is MainActivity && !isShowingAd) {
            currentActivityRef = WeakReference(activity)
        }
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        if (startedActivityCount > 0) {
            startedActivityCount--
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivityRef?.get() === activity) {
            currentActivityRef = null
        }
    }
}
