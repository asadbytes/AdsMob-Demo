package com.asadbyte.adsapp.ads

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Data class to hold all UI-related state
data class AdUiState(
    val isAppLoading: Boolean = true,
    val isInterstitialLoading: Boolean = false,
    val isRewardedLoading: Boolean = false,
    val isRewardedInterstitialLoading: Boolean = false,
    val isNativeLoading: Boolean = false,
    val isInterstitialReady: Boolean = false,
    val isRewardedReady: Boolean = false,
    val isRewardedInterstitialReady: Boolean = false,
    val nativeAd: NativeAd? = null,
    val rewardMessage: String = ""
)

class AdViewModel(application: Application) : AndroidViewModel(application) {

    private val adManager = AdManager.getInstance(application)

    private val _uiState = MutableStateFlow(AdUiState())
    val uiState = _uiState.asStateFlow()

    // Private properties to hold the loaded ads
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null

    init {
        // Pre-load some ads on init
        loadInterstitialAd()
        loadRewardedAd()
    }

    fun handleInitialAppLoad(activity: Activity) {
        adManager.loadAndShowInitialAppOpenAd(activity) {
            // This callback is triggered when the ad is closed or fails to load.
            // Now we can safely dismiss the splash screen.
            _uiState.update { it.copy(isAppLoading = false) }
        }
    }

    // region Event Handlers
    fun loadInterstitialAd() {
        if (uiState.value.isInterstitialLoading) return
        _uiState.update { it.copy(isInterstitialLoading = true) }
        adManager.loadInterstitialAd(
            getApplication(),
            onSuccess = { ad ->
                interstitialAd = ad
                _uiState.update { it.copy(isInterstitialLoading = false, isInterstitialReady = true) }
            },
            onFailure = {
                _uiState.update { it.copy(isInterstitialLoading = false, isInterstitialReady = false) }
            }
        )
    }

    fun showInterstitialAd(activity: Activity) {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                _uiState.update { it.copy(isInterstitialReady = false) }
                loadInterstitialAd() // Pre-load next
            }
            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                interstitialAd = null
                _uiState.update { it.copy(isInterstitialReady = false) }
            }
        }
        interstitialAd?.show(activity)
    }

    fun loadRewardedAd() {
        if (uiState.value.isRewardedLoading) return
        _uiState.update { it.copy(isRewardedLoading = true) }
        adManager.loadRewardedAd(
            getApplication(),
            onSuccess = { ad ->
                rewardedAd = ad
                _uiState.update { it.copy(isRewardedLoading = false, isRewardedReady = true) }
            },
            onFailure = {
                _uiState.update { it.copy(isRewardedLoading = false, isRewardedReady = false) }
            }
        )
    }

    fun showRewardedAd(activity: Activity) {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                _uiState.update { it.copy(isRewardedReady = false) }
                loadRewardedAd() // Pre-load next
            }
            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                rewardedAd = null
                _uiState.update { it.copy(isRewardedReady = false) }
            }
        }
        rewardedAd?.show(activity) { reward ->
            val newRewardMessage = "Earned ${reward.amount} ${reward.type}"
            _uiState.update { it.copy(rewardMessage = newRewardMessage) }
        }
    }

    fun loadNativeAd() {
        if (uiState.value.isNativeLoading) return
        _uiState.update { it.copy(isNativeLoading = true) }

        // Destroy the old native ad before loading a new one
        _uiState.value.nativeAd?.destroy()

        adManager.loadNativeAd(
            getApplication(),
            onSuccess = { ad ->
                _uiState.update { it.copy(isNativeLoading = false, nativeAd = ad) }
            },
            onFailure = {
                _uiState.update { it.copy(isNativeLoading = false, nativeAd = null) }
            }
        )
    }

    fun clearAllAds() {
        interstitialAd = null
        rewardedAd = null
        rewardedInterstitialAd = null
        _uiState.value.nativeAd?.destroy()

        // Reset the entire state
        _uiState.value = AdUiState()
    }
    //endregion
}