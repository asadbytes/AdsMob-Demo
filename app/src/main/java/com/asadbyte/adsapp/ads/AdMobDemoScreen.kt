package com.asadbyte.adsapp.ads

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asadbyte.adsapp.MyApplication

@Composable
fun AdMobDemoScreen(
    onAdLoadComplete: () -> Unit
) {
    val context = LocalContext.current
    val adManager = remember {
        AdManager.getInstance(context.applicationContext as Application)
    }

    // Collect ad states
    val interstitialAd by adManager.interstitialAdState.collectAsStateWithLifecycle()
    val rewardedAd by adManager.rewardedAdState.collectAsStateWithLifecycle()
    val rewardedInterstitialAd by adManager.rewardedInterstitialAdState.collectAsStateWithLifecycle()
    val nativeAd by adManager.nativeAdState.collectAsStateWithLifecycle()
    val appOpenAd by adManager.appOpenAdState.collectAsStateWithLifecycle()

    // Loading states
    val isInterstitialLoading by adManager.isInterstitialLoading.collectAsStateWithLifecycle()
    val isRewardedLoading by adManager.isRewardedLoading.collectAsStateWithLifecycle()
    val isRewardedInterstitialLoading by adManager.isRewardedInterstitialLoading.collectAsStateWithLifecycle()
    val isNativeLoading by adManager.isNativeLoading.collectAsStateWithLifecycle()
    val isAppOpenLoading by adManager.isAppOpenLoading.collectAsStateWithLifecycle()

    var rewardMessage by remember { mutableStateOf("") }

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

        // Banner Ads Section
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Banner Ads",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Adaptive Banner",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    AdaptiveBannerAd()

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Collapsible Banner",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    CollapsibleBannerAd()

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Medium Rectangle Banner",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    MediumBannerAd()
                }
            }
        }

        // Interstitial Ad Section
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Interstitial Ad",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (interstitialAd != null) "Ready" else "Loading...",
                            color = if (interstitialAd != null)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline
                        )

                        Button(
                            onClick = {
                                if (interstitialAd != null) {
                                    adManager.showInterstitialAd(context as ComponentActivity)
                                } else {
                                    adManager.loadInterstitialAd(context, force = true)
                                }
                            },
                            enabled = !isInterstitialLoading
                        ) {
                            Text(
                                if (interstitialAd != null) "Show Ad" else "Load Ad"
                            )
                        }
                    }
                }
            }
        }

        // Rewarded Ad Section
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Rewarded Ad",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (rewardedAd != null) "Ready" else "Loading...",
                            color = if (rewardedAd != null)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline
                        )

                        Button(
                            onClick = {
                                if (rewardedAd != null) {
                                    adManager.showRewardedAd(context as ComponentActivity) { reward ->
                                        rewardMessage = "Earned ${reward.amount} ${reward.type}"
                                    }
                                } else {
                                    adManager.loadRewardedAd(context, force = true)
                                }
                            },
                            enabled = !isRewardedLoading
                        ) {
                            Text(
                                if (rewardedAd != null) "Show Ad" else "Load Ad"
                            )
                        }
                    }

                    if (rewardMessage.isNotEmpty()) {
                        Text(
                            text = "Reward: $rewardMessage",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // Rewarded Interstitial Ad Section
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Rewarded Interstitial Ad",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (rewardedInterstitialAd != null) "Ready" else "Loading...",
                            color = if (rewardedInterstitialAd != null)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline
                        )

                        Button(
                            onClick = {
                                if (rewardedInterstitialAd != null) {
                                    adManager.showRewardedInterstitialAd(context as ComponentActivity) { reward ->
                                        rewardMessage = "Earned ${reward.amount} ${reward.type} (Interstitial)"
                                    }
                                } else {
                                    adManager.loadRewardedInterstitialAd(context, force = true)
                                }
                            },
                            enabled = !isRewardedInterstitialLoading
                        ) {
                            Text(
                                if (rewardedInterstitialAd != null) "Show Ad" else "Load Ad"
                            )
                        }
                    }
                }
            }
        }

        // Native Ad Section
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Native Ad with Media View",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    NativeAdComposable(
                        nativeAd = nativeAd,
                        withMediaView = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            adManager.loadNativeAd(context, withMediaView = true, force = true)
                        },
                        enabled = !isNativeLoading,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Reload Native Ad")
                    }
                }
            }
        }

        // Native Ad without Media View Section
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Native Ad without Media View",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    NativeAdComposable(
                        nativeAd = nativeAd,
                        withMediaView = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            adManager.loadNativeAd(context, withMediaView = false, force = true)
                        },
                        enabled = !isNativeLoading,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Reload Native Ad")
                    }
                }
            }
        }

        // App Open Ad Section
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "App Open Ad",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (appOpenAd != null) "Ready" else "Loading...",
                            color = if (appOpenAd != null)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline
                        )

                        Button(
                            onClick = {
                                if (appOpenAd != null) {
                                    adManager.showAppOpenAd(context as ComponentActivity)
                                } else {
                                    adManager.loadAppOpenAd(context, onAdLoadComplete)
                                }
                            },
                            enabled = !isAppOpenLoading
                        ) {
                            Text(
                                if (appOpenAd != null) "Show Ad" else "Load Ad"
                            )
                        }
                    }
                }
            }
        }

        // Ad Management Section
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Ad Management",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                adManager.loadInterstitialAd(context, force = true)
                                adManager.loadRewardedAd(context, force = true)
                                adManager.loadRewardedInterstitialAd(context, force = true)
                                adManager.loadNativeAd(context, withMediaView = true, force = true)
                                adManager.loadAppOpenAd(context, onAdLoadComplete)
                            }
                        ) {
                            Text("Reload All Ads")
                        }

                        Button(
                            onClick = {
                                adManager.clearAds()
                                rewardMessage = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Clear All Ads")
                        }
                    }
                }
            }
        }
    }
}
