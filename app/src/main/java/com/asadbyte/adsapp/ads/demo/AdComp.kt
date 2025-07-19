package com.asadbyte.adsapp.ads.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asadbyte.adsapp.ads.AdaptiveBannerAd
import com.asadbyte.adsapp.ads.NativeAdComposable
import com.google.android.gms.ads.nativead.NativeAd

@Composable
fun AdSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
fun AdActionButton(
    isReady: Boolean,
    isLoading: Boolean,
    onLoadClick: () -> Unit,
    onShowClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isReady) "Ready" else if (isLoading) "Loading..." else "Not Loaded",
            color = if (isReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
        Button(
            onClick = { if (isReady) onShowClick() else onLoadClick() },
            enabled = !isLoading
        ) {
            Text(if (isReady) "Show Ad" else "Load Ad")
        }
    }
}

@Composable
fun BannerAdSection() {
    AdSectionCard(title = "Banner Ads") {
        Text("Adaptive Banner", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        AdaptiveBannerAd()
    }
}

@Composable
fun InterstitialAdSection(
    isReady: Boolean,
    isLoading: Boolean,
    onLoadClick: () -> Unit,
    onShowClick: () -> Unit
) {
    AdSectionCard(title = "Interstitial Ad") {
        AdActionButton(isReady, isLoading, onLoadClick, onShowClick)
    }
}

@Composable
fun RewardedAdSection(
    isReady: Boolean,
    isLoading: Boolean,
    rewardMessage: String,
    onLoadClick: () -> Unit,
    onShowClick: () -> Unit
) {
    AdSectionCard(title = "Rewarded Ad") {
        AdActionButton(isReady, isLoading, onLoadClick, onShowClick)
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

@Composable
fun NativeAdSection(
    nativeAd: NativeAd?,
    isLoading: Boolean,
    onReloadClick: () -> Unit
) {
    AdSectionCard(title = "Native Ad") {
        // Assuming NativeAdComposable takes a nullable NativeAd
        NativeAdComposable(nativeAd = nativeAd, withMediaView = true)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onReloadClick,
            enabled = !isLoading,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Reload Native Ad")
        }
    }
}

@Composable
fun AdManagementSection(onReloadAll: () -> Unit, onClearAll: () -> Unit) {
    AdSectionCard(title = "Ad Management") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onReloadAll) {
                Text("Reload All Ads")
            }
            Button(
                onClick = onClearAll,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Clear All Ads")
            }
        }
    }
}