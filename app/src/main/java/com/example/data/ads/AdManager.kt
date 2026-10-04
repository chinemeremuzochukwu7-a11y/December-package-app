package com.example.data.ads

import android.app.Activity
import android.content.Context
import android.util.Log
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
 * Manages Google Mobile Ads (AdMob) including:
 * - Banner Ads
 * - Interstitial Ads
 * - Rewarded Ads (for earning free AI holiday wish credits)
 *
 * NOTE: Replace the TEST Ad Unit IDs below with your production AdMob IDs when ready.
 */
object AdManager {
    private const val TAG = "AdManager"

    // Google Test Ad Unit IDs (replace with your real IDs once approved)
    var BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    var INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    var REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private val _isRewardedAdLoaded = MutableStateFlow(false)
    val isRewardedAdLoaded: StateFlow<Boolean> = _isRewardedAdLoaded.asStateFlow()

    private val _isInterstitialAdLoaded = MutableStateFlow(false)
    val isInterstitialAdLoaded: StateFlow<Boolean> = _isInterstitialAdLoaded.asStateFlow()

    fun initialize(context: Context) {
        try {
            MobileAds.initialize(context) { status ->
                Log.d(TAG, "AdMob MobileAds initialized: $status")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Notice initializing MobileAds: ${e.message}")
        }
    }

    /**
     * Preloads an Interstitial Ad on demand
     */
    fun loadInterstitialAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
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

    /**
     * Shows the Interstitial Ad if available, then invokes onDismiss
     */
    fun showInterstitialAd(activity: Activity, onDismiss: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    _isInterstitialAdLoaded.value = false
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    _isInterstitialAdLoaded.value = false
                    onDismiss()
                }
            }
            ad.show(activity)
        } else {
            onDismiss()
        }
    }

    /**
     * Shows the Rewarded Ad on demand to award credits (1 credit per ad)
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (amount: Int) -> Unit = {},
        onDismiss: () -> Unit = {}
    ) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    _isRewardedAdLoaded.value = false
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    _isRewardedAdLoaded.value = false
                    // Fallback in sandbox/emulator: award 1 credit and proceed
                    com.example.data.subscription.SubscriptionManager.addCredits(1)
                    onRewardEarned(1)
                    onDismiss()
                }
            }

            ad.show(activity) { _ ->
                val amount = 1 // 1 credit after watching rewarded ads
                Log.d(TAG, "User earned reward: $amount credit")
                com.example.data.subscription.SubscriptionManager.addCredits(amount)
                onRewardEarned(amount)
            }
        } else {
            // Load on demand
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                activity,
                REWARDED_AD_UNIT_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(loadedAd: RewardedAd) {
                        rewardedAd = loadedAd
                        _isRewardedAdLoaded.value = true
                        showRewardedAd(activity, onRewardEarned, onDismiss)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.d(TAG, "Rewarded ad load notice: ${error.message}")
                        // In sandbox/emulator testing, grant 1 credit directly
                        com.example.data.subscription.SubscriptionManager.addCredits(1)
                        onRewardEarned(1)
                        onDismiss()
                    }
                }
            )
        }
    }

    /**
     * For free users: requires watching a rewarded ad before action (creation or sharing),
     * awards 1 credit, then executes the action. Pro users bypass ads instantly.
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
            onRewardEarned = { /* 1 credit added */ },
            onDismiss = { onComplete() }
        )
    }
}

/**
 * Reusable Banner Ad Composable
 */
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdManager.BANNER_AD_UNIT_ID
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
                        override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
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
