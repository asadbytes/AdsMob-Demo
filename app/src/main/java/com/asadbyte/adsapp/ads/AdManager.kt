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
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdManager private constructor(application: Application):
DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private var currentActivity: Activity? = null

    companion object {
        @Volatile
        private var INSTANCE: AdManager? = null

        fun getInstance(application: Application): AdManager {
            return INSTANCE ?: synchronized(this) {
                // Pass application context to the constructor
                INSTANCE ?: AdManager(application).also { INSTANCE = it }
            }
        }

        // Test Ad Unit IDs
        const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
        const val REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/5354046379"
        const val NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
        const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    }

    // Add an init block to register the observers
    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    // This replaces @OnLifecycleEvent(Lifecycle.Event.ON_START)
    override fun onStart(owner: LifecycleOwner) {
        currentActivity?.let { showAppOpenAd(it) }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) { currentActivity = activity }
    override fun onActivityResumed(activity: Activity) { currentActivity = activity }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }

    // Ad States
    private val _interstitialAdState = MutableStateFlow<InterstitialAd?>(null)
    val interstitialAdState: StateFlow<InterstitialAd?> = _interstitialAdState.asStateFlow()

    private val _rewardedAdState = MutableStateFlow<RewardedAd?>(null)
    val rewardedAdState: StateFlow<RewardedAd?> = _rewardedAdState.asStateFlow()

    private val _rewardedInterstitialAdState = MutableStateFlow<RewardedInterstitialAd?>(null)
    val rewardedInterstitialAdState: StateFlow<RewardedInterstitialAd?> = _rewardedInterstitialAdState.asStateFlow()

    private val _nativeAdState = MutableStateFlow<NativeAd?>(null)
    val nativeAdState: StateFlow<NativeAd?> = _nativeAdState.asStateFlow()

    private val _appOpenAdState = MutableStateFlow<AppOpenAd?>(null)
    val appOpenAdState: StateFlow<AppOpenAd?> = _appOpenAdState.asStateFlow()

    // Loading States
    private val _isInterstitialLoading = MutableStateFlow(false)
    val isInterstitialLoading: StateFlow<Boolean> = _isInterstitialLoading.asStateFlow()

    private val _isRewardedLoading = MutableStateFlow(false)
    val isRewardedLoading: StateFlow<Boolean> = _isRewardedLoading.asStateFlow()

    private val _isRewardedInterstitialLoading = MutableStateFlow(false)
    val isRewardedInterstitialLoading: StateFlow<Boolean> = _isRewardedInterstitialLoading.asStateFlow()

    private val _isNativeLoading = MutableStateFlow(false)
    val isNativeLoading: StateFlow<Boolean> = _isNativeLoading.asStateFlow()

    private val _isAppOpenLoading = MutableStateFlow(false)
    val isAppOpenLoading: StateFlow<Boolean> = _isAppOpenLoading.asStateFlow()

    // Cache timestamps
    private var lastInterstitialLoadTime = 0L
    private var lastRewardedLoadTime = 0L
    private var lastRewardedInterstitialLoadTime = 0L
    private var lastNativeLoadTime = 0L
    private var lastAppOpenLoadTime = 0L

    private val adCacheTimeoutMs = 5 * 60 * 1000L // 5 minutes
    private val loadCooldownMs = 30 * 1000L // 30 seconds between loads

    fun initialize(context: Context) {
        MobileAds.initialize(context) { initializationStatus ->
            Log.d("AdManager", "AdMob initialized: $initializationStatus")
        }
    }

    // Interstitial Ad
    fun loadInterstitialAd(context: Context, force: Boolean = false) {
        val currentTime = System.currentTimeMillis()

        if (!force && (currentTime - lastInterstitialLoadTime) < loadCooldownMs) {
            Log.d("AdManager", "Interstitial ad load cooldown active")
            return
        }

        if (!force && _interstitialAdState.value != null &&
            (currentTime - lastInterstitialLoadTime) < adCacheTimeoutMs) {
            Log.d("AdManager", "Interstitial ad already cached and valid")
            return
        }

        if (_isInterstitialLoading.value) {
            Log.d("AdManager", "Interstitial ad already loading")
            return
        }

        _isInterstitialLoading.value = true
        lastInterstitialLoadTime = currentTime

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(context, INTERSTITIAL_AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("AdManager", "Interstitial ad failed to load: ${adError.message}")
                _interstitialAdState.value = null
                _isInterstitialLoading.value = false
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d("AdManager", "Interstitial ad loaded successfully")
                _interstitialAdState.value = interstitialAd
                _isInterstitialLoading.value = false

                interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d("AdManager", "Interstitial ad dismissed")
                        _interstitialAdState.value = null
                        loadInterstitialAd(context)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e("AdManager", "Interstitial ad failed to show: ${adError.message}")
                        _interstitialAdState.value = null
                    }
                }
            }
        })
    }

    fun showInterstitialAd(activity: Activity) {
        val ad = _interstitialAdState.value
        if (ad != null) {
            ad.show(activity)
        } else {
            Log.d("AdManager", "Interstitial ad not ready")
        }
    }

    // Rewarded Ad
    fun loadRewardedAd(context: Context, force: Boolean = false) {
        val currentTime = System.currentTimeMillis()

        if (!force && (currentTime - lastRewardedLoadTime) < loadCooldownMs) {
            Log.d("AdManager", "Rewarded ad load cooldown active")
            return
        }

        if (!force && _rewardedAdState.value != null &&
            (currentTime - lastRewardedLoadTime) < adCacheTimeoutMs) {
            Log.d("AdManager", "Rewarded ad already cached and valid")
            return
        }

        if (_isRewardedLoading.value) {
            Log.d("AdManager", "Rewarded ad already loading")
            return
        }

        _isRewardedLoading.value = true
        lastRewardedLoadTime = currentTime

        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(context, REWARDED_AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("AdManager", "Rewarded ad failed to load: ${adError.message}")
                _rewardedAdState.value = null
                _isRewardedLoading.value = false
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Log.d("AdManager", "Rewarded ad loaded successfully")
                _rewardedAdState.value = rewardedAd
                _isRewardedLoading.value = false

                rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d("AdManager", "Rewarded ad dismissed")
                        _rewardedAdState.value = null
                        // Preload next ad
                        loadRewardedAd(context)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e("AdManager", "Rewarded ad failed to show: ${adError.message}")
                        _rewardedAdState.value = null
                    }
                }
            }
        })
    }

    fun showRewardedAd(activity: Activity, onUserEarnedReward: (RewardItem) -> Unit) {
        val ad = _rewardedAdState.value
        if (ad != null) {
            ad.show(activity) { rewardItem ->
                onUserEarnedReward(rewardItem)
            }
        } else {
            Log.d("AdManager", "Rewarded ad not ready")
        }
    }

    // Rewarded Interstitial Ad
    fun loadRewardedInterstitialAd(context: Context, force: Boolean = false) {
        val currentTime = System.currentTimeMillis()

        if (!force && (currentTime - lastRewardedInterstitialLoadTime) < loadCooldownMs) {
            Log.d("AdManager", "Rewarded interstitial ad load cooldown active")
            return
        }

        if (!force && _rewardedInterstitialAdState.value != null &&
            (currentTime - lastRewardedInterstitialLoadTime) < adCacheTimeoutMs) {
            Log.d("AdManager", "Rewarded interstitial ad already cached and valid")
            return
        }

        if (_isRewardedInterstitialLoading.value) {
            Log.d("AdManager", "Rewarded interstitial ad already loading")
            return
        }

        _isRewardedInterstitialLoading.value = true
        lastRewardedInterstitialLoadTime = currentTime

        val adRequest = AdRequest.Builder().build()

        RewardedInterstitialAd.load(context, REWARDED_INTERSTITIAL_AD_UNIT_ID, adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("AdManager", "Rewarded interstitial ad failed to load: ${adError.message}")
                    _rewardedInterstitialAdState.value = null
                    _isRewardedInterstitialLoading.value = false
                }

                override fun onAdLoaded(rewardedInterstitialAd: RewardedInterstitialAd) {
                    Log.d("AdManager", "Rewarded interstitial ad loaded successfully")
                    _rewardedInterstitialAdState.value = rewardedInterstitialAd
                    _isRewardedInterstitialLoading.value = false

                    rewardedInterstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d("AdManager", "Rewarded interstitial ad dismissed")
                            _rewardedInterstitialAdState.value = null
                            // Preload next ad
                            loadRewardedInterstitialAd(context)
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e("AdManager", "Rewarded interstitial ad failed to show: ${adError.message}")
                            _rewardedInterstitialAdState.value = null
                        }
                    }
                }
            })
    }

    fun showRewardedInterstitialAd(activity: Activity, onUserEarnedReward: (RewardItem) -> Unit) {
        val ad = _rewardedInterstitialAdState.value
        if (ad != null) {
            ad.show(activity) { rewardItem ->
                onUserEarnedReward(rewardItem)
            }
        } else {
            Log.d("AdManager", "Rewarded interstitial ad not ready")
        }
    }

    // Native Ad
    fun loadNativeAd(context: Context, withMediaView: Boolean = true, force: Boolean = false) {
        val currentTime = System.currentTimeMillis()

        if (!force && (currentTime - lastNativeLoadTime) < loadCooldownMs) {
            Log.d("AdManager", "Native ad load cooldown active")
            return
        }

        if (!force && _nativeAdState.value != null &&
            (currentTime - lastNativeLoadTime) < adCacheTimeoutMs) {
            Log.d("AdManager", "Native ad already cached and valid")
            return
        }

        if (_isNativeLoading.value) {
            Log.d("AdManager", "Native ad already loading")
            return
        }

        _isNativeLoading.value = true
        lastNativeLoadTime = currentTime

        val adLoader = AdLoader.Builder(context, NATIVE_AD_UNIT_ID)
            .forNativeAd { nativeAd ->
                Log.d("AdManager", "Native ad loaded successfully")
                _nativeAdState.value = nativeAd
                _isNativeLoading.value = false
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("AdManager", "Native ad failed to load: ${adError.message}")
                    _nativeAdState.value = null
                    _isNativeLoading.value = false
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setRequestMultipleImages(true)
                    .setMediaAspectRatio(
                        if (withMediaView) NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE
                        else NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_UNKNOWN
                    )
                    .build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    // App Open Ad
    fun loadAppOpenAd(context: Context, onAdLoadComplete: () -> Unit) {
        val currentTime = System.currentTimeMillis()

        if ((currentTime - lastAppOpenLoadTime) < loadCooldownMs) {
            Log.d("AdManager", "App open ad load cooldown active")
            return
        }

        if (_appOpenAdState.value != null &&
            (currentTime - lastAppOpenLoadTime) < adCacheTimeoutMs) {
            Log.d("AdManager", "App open ad already cached and valid")
            return
        }

        if (_isAppOpenLoading.value) {
            Log.d("AdManager", "App open ad already loading")
            return
        }

        _isAppOpenLoading.value = true
        lastAppOpenLoadTime = currentTime

        val adRequest = AdRequest.Builder().build()

        AppOpenAd.load(context, APP_OPEN_AD_UNIT_ID, adRequest,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("AdManager", "App open ad failed to load: ${adError.message}")
                    _appOpenAdState.value = null
                    onAdLoadComplete()
                }

                override fun onAdLoaded(appOpenAd: AppOpenAd) {
                    Log.d("AdManager", "App open ad loaded successfully")
                    _appOpenAdState.value = appOpenAd

                    appOpenAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d("AdManager", "App open ad dismissed")
                            _appOpenAdState.value = null
                            loadAppOpenAd(context, onAdLoadComplete)
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e("AdManager", "App open ad failed to show: ${adError.message}")
                            _appOpenAdState.value = null
                        }
                    }
                    onAdLoadComplete()
                }
            })
    }

    fun showAppOpenAd(activity: Activity) {
        val ad = _appOpenAdState.value
        if (ad != null) {
            ad.show(activity)
        } else {
            Log.d("AdManager", "App open ad not ready")
        }
    }

    // Clear all ads
    fun clearAds() {
        _interstitialAdState.value = null
        _rewardedAdState.value = null
        _rewardedInterstitialAdState.value = null
        _nativeAdState.value?.destroy()
        _nativeAdState.value = null
        _appOpenAdState.value = null
    }
}
