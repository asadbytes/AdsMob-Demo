package com.asadbyte.adsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.asadbyte.adsapp.ads.AdManager
import com.asadbyte.adsapp.ads.AdViewModel
import com.asadbyte.adsapp.ads.demo.AdMobDemoScreen
import com.asadbyte.adsapp.ui.theme.AdsAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: AdViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value.isAppLoading
        }

        viewModel.handleInitialAppLoad(this)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            if (!uiState.isAppLoading) {
                AdsAppTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AdMobDemoScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}