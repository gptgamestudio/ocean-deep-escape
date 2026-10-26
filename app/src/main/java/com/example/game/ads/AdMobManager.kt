package com.example.game.ads

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
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
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

/**
 * AdMobManager handles initialization, banner ads, interstitial ads, and rewarded ads lifecycle.
 * Prevents MESA rendernode errors and AdServices ServiceUnavailableException
 * in emulators and environments lacking Google Play Services / DRM nodes.
 */
object AdMobManager {
    private const val TAG = "AdMobManager"

    // User's Verified Live AdMob IDs from AdMob Console
    const val LIVE_BANNER_AD_UNIT_ID = "ca-app-pub-9786472984542330/7618248449"
    const val LIVE_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-9786472984542330/5354302327"
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    // Monetization app-ads.txt verification
    const val PUBLISHER_ID = "pub-9786472984542330"
    const val CERTIFICATION_AUTHORITY_ID = "f08c47fec0942fa0"
    const val APP_ADS_TXT_ENTRY = "google.com, pub-9786472984542330, DIRECT, f08c47fec0942fa0"

    private var isInitialized = false
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var lastInterstitialShownTime = 0L
    private const val INTERSTITIAL_COOLDOWN_MS = 25_000L

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    /**
     * Checks whether Google Play Services is available and operational.
     */
    fun isPlayServicesAvailable(context: Context): Boolean {
        return try {
            val availability = GoogleApiAvailability.getInstance()
            val resultCode = availability.isGooglePlayServicesAvailable(context)
            resultCode == ConnectionResult.SUCCESS
        } catch (t: Throwable) {
            Log.w(TAG, "GooglePlayServices check exception: ${t.message}")
            false
        }
    }

    /**
     * Checks if running inside an emulator or container environment
     */
    fun isRunningInEmulator(): Boolean {
        val fingerPrint = Build.FINGERPRINT.lowercase()
        val model = Build.MODEL.lowercase()
        val hardware = Build.HARDWARE.lowercase()
        val product = Build.PRODUCT.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()
        val board = Build.BOARD.lowercase()

        return fingerPrint.startsWith("generic")
                || fingerPrint.startsWith("unknown")
                || fingerPrint.contains("sdk_gphone")
                || fingerPrint.contains("emulator")
                || model.contains("google_sdk")
                || model.contains("emulator")
                || model.contains("android sdk")
                || manufacturer.contains("genymotion")
                || hardware.contains("goldfish")
                || hardware.contains("ranchu")
                || hardware.contains("cutf")
                || product.contains("sdk")
                || product.contains("google_sdk")
                || product.contains("emulator")
                || product.contains("simulator")
                || board.contains("goldfish")
                || board.contains("ranchu")
                || board.contains("cutf")
    }

    /**
     * Initialize Google Mobile Ads SDK once per app lifecycle safely.
     * Bypasses emulator environments to eliminate adservices/MESA rendernode errors.
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        if (isRunningInEmulator()) {
            Log.i(TAG, "Running in Android emulator/container: skipping native AdMob init to prevent adservices/MESA errors.")
            return
        }
        if (!isPlayServicesAvailable(context)) {
            Log.i(TAG, "Google Play Services unavailable. Skipping MobileAds init.")
            return
        }

        try {
            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "AdMob MobileAds initialized: ${initializationStatus.adapterStatusMap}")
                isInitialized = true
                preloadInterstitial(context)
                preloadRewarded(context)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "AdMob initialization caught exception", e)
        }
    }

    /**
     * Preload Interstitial Ad so it's ready when player dies or wins
     */
    fun preloadInterstitial(
        context: Context,
        adUnitId: String = LIVE_INTERSTITIAL_AD_UNIT_ID
    ) {
        if (isRunningInEmulator() || !isPlayServicesAvailable(context)) return
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isInterstitialLoading = false
                        Log.d(TAG, "Interstitial ad successfully loaded")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        interstitialAd = null
                        isInterstitialLoading = false
                        Log.w(TAG, "Interstitial ad failed to load (${loadAdError.code}): ${loadAdError.message}")
                    }
                }
            )
        } catch (t: Throwable) {
            isInterstitialLoading = false
            Log.w(TAG, "Exception during interstitial preload: ${t.message}")
        }
    }

    /**
     * Display Interstitial Ad if loaded and cooldown has passed
     */
    fun showInterstitial(
        activity: Activity,
        adUnitId: String = LIVE_INTERSTITIAL_AD_UNIT_ID,
        forceShow: Boolean = false,
        onAdDismissed: () -> Unit = {}
    ) {
        if (isRunningInEmulator() || !isPlayServicesAvailable(activity)) {
            onAdDismissed()
            return
        }

        val now = System.currentTimeMillis()
        if (!forceShow && (now - lastInterstitialShownTime < INTERSTITIAL_COOLDOWN_MS)) {
            Log.d(TAG, "Interstitial skipped due to cooldown")
            onAdDismissed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed by user")
                    interstitialAd = null
                    lastInterstitialShownTime = System.currentTimeMillis()
                    preloadInterstitial(activity, adUnitId)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Interstitial failed to show: ${adError.message}")
                    interstitialAd = null
                    preloadInterstitial(activity, adUnitId)
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad showed fullscreen")
                    lastInterstitialShownTime = System.currentTimeMillis()
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial not ready, preloading now")
            preloadInterstitial(activity, adUnitId)
            onAdDismissed()
        }
    }

    /**
     * Preload Rewarded Video Ad for earning in-game lives (+10 lives per ad)
     */
    fun preloadRewarded(
        context: Context,
        adUnitId: String = TEST_REWARDED_AD_UNIT_ID
    ) {
        if (isRunningInEmulator() || !isPlayServicesAvailable(context)) return
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                adUnitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isRewardedLoading = false
                        Log.d(TAG, "Rewarded ad successfully loaded")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        rewardedAd = null
                        isRewardedLoading = false
                        Log.w(TAG, "Rewarded ad failed to load (${loadAdError.code}): ${loadAdError.message}")
                    }
                }
            )
        } catch (t: Throwable) {
            isRewardedLoading = false
            Log.w(TAG, "Exception during rewarded preload: ${t.message}")
        }
    }

    /**
     * Show Rewarded Ad: User watches video ad and earns +10 lives.
     * Fallbacks to simulated reward if in emulator or network error so gameplay never gets blocked.
     */
    fun showRewardedAd(
        activity: Activity,
        adUnitId: String = TEST_REWARDED_AD_UNIT_ID,
        onRewardEarned: (Int) -> Unit,
        onDismissed: () -> Unit = {}
    ) {
        if (isRunningInEmulator() || !isPlayServicesAvailable(activity)) {
            Log.d(TAG, "Running in container/emulator or Play Services unavailable: granting +10 lives reward instantly")
            onRewardEarned(10)
            onDismissed()
            return
        }

        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed by user")
                    rewardedAd = null
                    preloadRewarded(activity, adUnitId)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Rewarded ad failed to show: ${adError.message}")
                    rewardedAd = null
                    preloadRewarded(activity, adUnitId)
                    // Ensure player still gets their lives even on ad show failure
                    onRewardEarned(10)
                    onDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad showed fullscreen")
                }
            }
            ad.show(activity, OnUserEarnedRewardListener { rewardItem ->
                Log.d(TAG, "User earned reward from ad: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned(10)
            })
        } else {
            Log.d(TAG, "Rewarded ad was not ready, preloading now and granting +10 lives reward")
            preloadRewarded(activity, adUnitId)
            onRewardEarned(10)
            onDismissed()
        }
    }
}

/**
 * Jetpack Compose wrapper for Google Mobile Ads Banner.
 * Uses software layer type to prevent MESA rendernode errors.
 * Gracefully switches to sponsored in-game banner if Play Services or DRM rendernodes are unavailable.
 */
@Composable
fun AdMobBannerAd(
    bannerId: String = AdMobManager.LIVE_BANNER_AD_UNIT_ID,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val canLoadNativeAds = remember { !AdMobManager.isRunningInEmulator() && AdMobManager.isPlayServicesAvailable(context) }
    var isLiveAdLoaded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF2001424)),
        border = BorderStroke(1.dp, Color(0xFF0077B6).copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = modifier.testTag("admob_banner_container")
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (canLoadNativeAds) {
                // Real Google Mobile Ads AdView with Software Layer to prevent MESA rendernode failure
                AndroidView(
                    modifier = Modifier.testTag("admob_adview"),
                    factory = { ctx ->
                        AdView(ctx).apply {
                            // Force software rendering layer on AdView to prevent MESA from querying /dev/dri/renderD128
                            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                            setAdSize(AdSize.BANNER)
                            adUnitId = bannerId
                            adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    isLiveAdLoaded = true
                                    Log.d("AdMobBanner", "Banner ad loaded successfully")
                                }

                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    isLiveAdLoaded = false
                                    Log.w("AdMobBanner", "Banner load failed (${error.code}): ${error.message}, falling back to styled card")
                                }
                            }
                            try {
                                loadAd(AdRequest.Builder().build())
                            } catch (t: Throwable) {
                                Log.w("AdMobBanner", "Exception loading ad: ${t.message}")
                            }
                        }
                    }
                )
            }

            // High-fidelity fallback / sponsored banner display when live banner is filling or offline
            if (!isLiveAdLoaded || !canLoadNativeAds) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFFFFB703),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "AD",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Ad",
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.size(14.dp)
                    )

                    Text(
                        text = "ADMOB SPONSORED: DEEP-SEA EXPEDITION RADAR",
                        color = Color(0xFFE0FAFF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
