package com.asadbyte.adsapp.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

class AdManager private constructor(application: Application) :
    DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private var currentActivity: Activity? = null
    private var appOpenAd: AppOpenAd? = null
    private var isShowingAppOpenAd = false

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        currentActivity?.let {
            if (!isShowingAppOpenAd) {
                showAppOpenAd(it)
            }
        }
    }

    fun loadInterstitialAd(
        context: Context,
        onSuccess: (InterstitialAd) -> Unit,
        onFailure: () -> Unit
    ) {
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d("AdManager", "Interstitial ad loaded successfully")
                    onSuccess(ad)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("AdManager", "Interstitial ad failed to load: ${error.message}")
                    onFailure()
                }
            })
    }

    fun loadRewardedAd(
        context: Context,
        onSuccess: (RewardedAd) -> Unit,
        onFailure: () -> Unit
    ) {
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("AdManager", "Rewarded ad loaded successfully")
                    onSuccess(ad)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("AdManager", "Rewarded ad failed to load: ${error.message}")
                    onFailure()
                }
            })
    }

    fun loadRewardedInterstitialAd(
        context: Context,
        onSuccess: (RewardedInterstitialAd) -> Unit,
        onFailure: () -> Unit
    ) {
        RewardedInterstitialAd.load(
            context,
            REWARDED_INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    Log.d("AdManager", "Rewarded interstitial ad loaded successfully")
                    onSuccess(ad)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("AdManager", "Rewarded interstitial ad failed to load: ${error.message}")
                    onFailure()
                }
            })
    }

    fun loadNativeAd(
        context: Context,
        onSuccess: (NativeAd) -> Unit,
        onFailure: () -> Unit
    ) {
        val adLoader = AdLoader.Builder(context, NATIVE_AD_UNIT_ID)
            .forNativeAd { nativeAd ->
                Log.d("AdManager", "Native ad loaded successfully")
                onSuccess(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("AdManager", "Native ad failed to load: ${adError.message}")
                    onFailure()
                }
            })
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun loadAppOpenAd(context: Context, onComplete: () -> Unit) {
        AppOpenAd.load(
            context,
            APP_OPEN_AD_UNIT_ID,
            AdRequest.Builder().build(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d("AdManager", "App open ad loaded successfully")
                    appOpenAd = ad
                    onComplete()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("AdManager", "App open ad failed to load: ${error.message}")
                    appOpenAd = null
                    onComplete()
                }
            })
    }

    fun loadAndShowInitialAppOpenAd(
        activity: Activity,
        onAdClosedOrFailed: () -> Unit
    ) {
        AppOpenAd.load(
            activity,
            APP_OPEN_AD_UNIT_ID,
            AdRequest.Builder().build(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        // This is the key: signal completion when the ad is dismissed.
                        override fun onAdDismissedFullScreenContent() {
                            onAdClosedOrFailed()
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            onAdClosedOrFailed()
                        }
                    }
                    ad.show(activity)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    // If the ad fails to load, we must still signal completion
                    // to unblock the splash screen.
                    onAdClosedOrFailed()
                }
            }
        )
    }

    private fun showAppOpenAd(activity: Activity) {
        appOpenAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAppOpenAd = false
                    loadAppOpenAd(activity) {}
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    appOpenAd = null
                    isShowingAppOpenAd = false
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAppOpenAd = true
                }
            }
            ad.show(activity)
        }
    }

    // Companion Object and Lifecycle Callbacks...
    companion object {
        @Volatile
        private var INSTANCE: AdManager? = null

        fun getInstance(application: Application): AdManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdManager(application).also { INSTANCE = it }
            }

        // Ad Unit IDs
        const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
        const val REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/5354046379"
        const val NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
        const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) { currentActivity = activity }
    override fun onActivityResumed(activity: Activity) { currentActivity = activity }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) currentActivity = null
    }
}