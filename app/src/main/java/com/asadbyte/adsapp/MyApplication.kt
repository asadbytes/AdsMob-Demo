package com.asadbyte.adsapp

import android.app.Application
import com.asadbyte.adsapp.ads.AdManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val adManager = AdManager.getInstance(this)
        adManager.initialize(this)
    }
}
