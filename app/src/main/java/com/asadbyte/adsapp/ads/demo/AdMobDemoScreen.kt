package com.asadbyte.adsapp.ads.demo

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asadbyte.adsapp.ads.AdViewModel

@Composable
fun AdMobDemoScreen(
    viewModel: AdViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "AdMob Integration Demo",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item { BannerAdSection() }

        item {
            InterstitialAdSection(
                isReady = uiState.isInterstitialReady,
                isLoading = uiState.isInterstitialLoading,
                onLoadClick = { viewModel.loadInterstitialAd() },
                onShowClick = { viewModel.showInterstitialAd(activity) }
            )
        }

        item {
            RewardedAdSection(
                isReady = uiState.isRewardedReady,
                isLoading = uiState.isRewardedLoading,
                rewardMessage = uiState.rewardMessage,
                onLoadClick = { viewModel.loadRewardedAd() },
                onShowClick = { viewModel.showRewardedAd(activity) }
            )
        }

        item {
            NativeAdSection(
                nativeAd = uiState.nativeAd,
                isLoading = uiState.isNativeLoading,
                onReloadClick = { viewModel.loadNativeAd() }
            )
        }

        item {
            AdManagementSection(
                onReloadAll = {
                    viewModel.loadInterstitialAd()
                    viewModel.loadRewardedAd()
                    viewModel.loadNativeAd()
                },
                onClearAll = { viewModel.clearAllAds() }
            )
        }
    }
}