package com.wholesomeisland.ollamaclient

import android.app.Application
import android.net.wifi.WifiManager
import android.content.Context

class OllamaApp : Application() {
    private var wifiLock: WifiManager.WifiLock? = null

    override fun onCreate() {
        super.onCreate()
        val wm = getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "OllamaDiscoveryLock")
    }

    fun acquireWifiLock() {
        if (wifiLock?.isHeld == false) {
            wifiLock?.acquire()
        }
    }

    fun releaseWifiLock() {
        if (wifiLock?.isHeld == true) {
            wifiLock?.release()
        }
    }
}
