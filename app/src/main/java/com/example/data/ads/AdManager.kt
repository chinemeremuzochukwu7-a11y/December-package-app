package com.example.data.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Google Mobile Ads (AdMob):
 * - Footer Banner Ads
 * - Section Interstitial Ads
 * - Rewarded Ads (grants +1 credit or single Pro item unlock ONLY when ad completes in release)
 *
 * Uses official Google AdMob test IDs in DEBUG builds to prevent invalid-traffic account bans,
 * and the publisher's production Ad Unit IDs automatically in RELEASE builds.
 */
object AdManager {
    private const val TAG = "AdManager"

    // Production Ad Unit IDs
    var BANNER_AD_UNIT_ID = "ca-app-pub-6322953088287505/4181550406"
    var INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-6322953088287505/5212480917"
    var REWARDED_AD_UNIT_ID = "ca-app-pub-6322953088287505/5404052605"

    // Official Google AdMob Sample Test IDs (used automatically in DEBUG builds)
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    fun activeBannerAdUnitId(): String =
        if (BuildConfig.DEBUG) TEST_BANNER_AD_UNIT_ID else BANNER_AD_UNIT_ID

    private fun activeInterstitialAdUnitId(): String =
        if (BuildConfig.DEBUG) TEST_INTERSTITIAL_AD_UNIT_ID else INTERSTITIAL_AD_UNIT_ID

    private fun activeRewardedAdUnitId(): String =
        if (BuildConfig.DEBUG) TEST_REWARDED_AD_UNIT_ID else REWARDED_AD_UNIT_ID

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private val _isRewardedAdLoaded = MutableStateFlow(false)
    val isRewardedAdLoaded: StateFlow<Boolean> = _isRewardedAdLoaded.asStateFlow()

    private val _isInterstitialAdLoaded = MutableStateFlow(false)
    val isInterstitialAdLoaded: StateFlow<Boolean> = _isInterstitialAdLoaded.asStateFlow()

    @Volatile
    var isFullScreenAdShowing = false
        private set

    fun onFullScreenAdOpened() {
        isFullScreenAdShowing = true
    }

    fun onFullScreenAdClosed() {
        isFullScreenAdShowing = false
    }

    @Volatile
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context.applicationContext) { status ->
                Log.d(TAG, "AdMob MobileAds initialized: $status")
            }
            isInitialized = true
        } catch (e: Throwable) {
            Log.w(TAG, "Notice initializing MobileAds: ${e.message}")
        }
    }

    fun loadInterstitialAd(context: Context) {
        initialize(context)
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            activeInterstitialAdUnitId(),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    _isInterstitialAdLoaded.value = true
                    Log.d(TAG, "Interstitial ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    _isInterstitialAdLoaded.value = false
                    Log.d(TAG, "Interstitial ad failed to load: ${error.message}")
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity, onDismiss: () -> Unit = {}) {
        initialize(activity)
        val ad = interstitialAd
        if (ad != null && !isFullScreenAdShowing) {
            isFullScreenAdShowing = true
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    _isInterstitialAdLoaded.value = false
                    isFullScreenAdShowing = false
                    loadInterstitialAd(activity)
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    _isInterstitialAdLoaded.value = false
                    isFullScreenAdShowing = false
                    onDismiss()
                }
            }
            ad.show(activity)
        } else {
            loadInterstitialAd(activity)
            onDismiss()
        }
    }

    /**
     * Shows a Rewarded Ad to award +1 credit (or trigger custom reward callbacks).
     * Never awards free credits on failed/blocked ads in release builds.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (amount: Int) -> Unit = {},
        onDismiss: () -> Unit = {},
        onFailed: ((String) -> Unit)? = null
    ) {
        initialize(activity)
        val ad = rewardedAd
        if (ad != null && !isFullScreenAdShowing) {
            var rewardedThisShow = false
            isFullScreenAdShowing = true
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    _isRewardedAdLoaded.value = false
                    isFullScreenAdShowing = false
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    _isRewardedAdLoaded.value = false
                    isFullScreenAdShowing = false
                    if (BuildConfig.DEBUG) {
                        com.example.data.subscription.SubscriptionManager.addCredits(1)
                        onRewardEarned(1)
                        onDismiss()
                    } else {
                        val msg = "Rewarded video failed to play. Please check connection and retry."
                        if (onFailed != null) onFailed(msg)
                        else Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                }
            }

            ad.show(activity) { _ ->
                if (!rewardedThisShow) {
                    rewardedThisShow = true
                    val amount = 1
                    Log.d(TAG, "User earned reward: $amount credit")
                    com.example.data.subscription.SubscriptionManager.addCredits(amount)
                    onRewardEarned(amount)
                }
            }
        } else if (ad == null) {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                activity,
                activeRewardedAdUnitId(),
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(loadedAd: RewardedAd) {
                        rewardedAd = loadedAd
                        _isRewardedAdLoaded.value = true
                        showRewardedAd(activity, onRewardEarned, onDismiss, onFailed)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.d(TAG, "Rewarded ad load notice: ${error.message}")
                        if (BuildConfig.DEBUG) {
                            com.example.data.subscription.SubscriptionManager.addCredits(1)
                            onRewardEarned(1)
                            onDismiss()
                        } else {
                            val msg = "Video ad unavailable right now. Please connect to internet or disable ad-blockers."
                            if (onFailed != null) onFailed(msg)
                            else Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    }
                }
            )
        } else {
            onDismiss()
        }
    }

    /**
     * Requires a completed rewarded ad before unlocking/executing an action in release.
     */
    fun requireRewardedAdBeforeAction(
        activity: Activity,
        onComplete: () -> Unit
    ) {
        if (com.example.data.subscription.SubscriptionManager.isPro.value) {
            onComplete()
            return
        }

        showRewardedAd(
            activity = activity,
            onRewardEarned = { onComplete() }
        )
    }
}

/**
 * Reusable Footer Banner Ad Composable
 */
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdManager.activeBannerAdUnitId()
) {
    var isFailed by remember { mutableStateOf(false) }

    if (isFailed) return

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            try {
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    adListener = object : com.google.android.gms.ads.AdListener() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            Log.d("AdMobBanner", "Banner ad load notice: ${loadAdError.message}")
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            } catch (e: Throwable) {
                Log.w("AdMobBanner", "Failed to create AdView: ${e.message}")
                isFailed = true
                android.view.View(context)
            }
        }
    )
}
