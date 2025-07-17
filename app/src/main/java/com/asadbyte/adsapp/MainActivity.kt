package com.asadbyte.adsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.asadbyte.adsapp.ads.AdManager
import com.asadbyte.adsapp.ads.AdMobDemoScreen

class MainActivity : ComponentActivity() {

    private val adManager = AdManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize AdMob
        adManager.initialize(this)

        // Load ads
        adManager.loadInterstitialAd(this)
        adManager.loadRewardedAd(this)
        adManager.loadRewardedInterstitialAd(this)
        adManager.loadNativeAd(this, withMediaView = true)
        adManager.loadAppOpenAd(this)

        setContent {
            AdMobDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AdMobDemoScreen()
                }
            }
        }
    }
}

@Composable
fun AdMobDemoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        content = content
    )
}