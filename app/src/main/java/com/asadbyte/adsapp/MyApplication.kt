package com.asadbyte.adsapp

import android.app.Application
import com.asadbyte.adsapp.ads.AdManager
import com.google.android.gms.ads.MobileAds

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
    }
}
