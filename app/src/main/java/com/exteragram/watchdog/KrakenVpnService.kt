package com.exteragram.watchdog

import android.app.*
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor

class KrakenVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(2001, notification())

        if (vpnInterface == null) {
            vpnInterface = Builder()
                .setSession("KRAKEN VPN")
                .setMtu(1500)
                .addAddress("10.8.0.2", 32)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("1.1.1.1")
                .addDnsServer("8.8.8.8")
                .establish()
        }

        return START_STICKY
    }

    private fun notification(): Notification {
        val channelId = "kraken_vpn"

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    "KRAKEN VPN",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }

        return Notification.Builder(this, channelId)
            .setContentTitle("KRAKEN VPN")
            .setContentText("VPN работает")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        vpnInterface?.close()
        vpnInterface = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = super.onBind(intent)
}
