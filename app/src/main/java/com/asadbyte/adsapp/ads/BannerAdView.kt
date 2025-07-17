package com.asadbyte.adsapp.ads

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAdView(
    adSize: AdSize,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(adSize)
                adUnitId = AdManager.BANNER_AD_UNIT_ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

@Composable
fun AdaptiveBannerAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, 320)

    BannerAdView(
        adSize = adSize,
        modifier = modifier
    )
}

@Composable
fun MediumBannerAd(modifier: Modifier = Modifier) {
    BannerAdView(
        adSize = AdSize.MEDIUM_RECTANGLE,
        modifier = modifier
    )
}

@Composable
fun CollapsibleBannerAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdManager.BANNER_AD_UNIT_ID

                val extras = Bundle().apply {
                    putString("collapsible", "bottom")
                }

                val adRequest = AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                    .build()

                loadAd(adRequest)
            }
        }
    )
}
