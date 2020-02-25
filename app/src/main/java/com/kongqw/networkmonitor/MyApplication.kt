package com.kongqw.networkmonitor

import android.app.Application
import com.kongqw.network.monitor.NetworkMonitorManager

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        NetworkMonitorManager.getInstance().init(this)
    }
}