package com.asadbyte.adsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.asadbyte.adsapp.ads.AdManager
import com.asadbyte.adsapp.ads.AdMobDemoScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val adManager by lazy { AdManager.getInstance(application) }
    private var isAdLoading = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition { isAdLoading }

        adManager.loadAppOpenAd(this) {
            adManager.showAppOpenAd(this)
            lifecycleScope.launch {
                delay(1000)
                isAdLoading = false
            }
        }
        // Initialize AdMob
        adManager.initialize(this)

        // Load ads
        adManager.loadInterstitialAd(this)
        adManager.loadRewardedAd(this)
        adManager.loadRewardedInterstitialAd(this)
        adManager.loadNativeAd(this, withMediaView = true)

        setContent {
            AdMobDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AdMobDemoScreen(
                        onAdLoadComplete = {
                            isAdLoading = false
                            adManager.showAppOpenAd(this)
                        }
                    )
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