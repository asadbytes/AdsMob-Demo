package com.asadbyte.adsapp

import android.app.Application
import com.asadbyte.adsapp.ads.AdManager
import com.asadbyte.adsapp.ads.AppOpenAdManager

class MyApplication : Application() {

    private lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate() {
        super.onCreate()

        // Initialize AdMob
        AdManager.getInstance().initialize(this)

        // Initialize App Open Ad Manager
        appOpenAdManager = AppOpenAdManager(this)
        appOpenAdManager.loadAd(this)
    }
}
