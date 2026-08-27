package com.exteragram.watchdog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if ((intent.action == Intent.ACTION_BOOT_COMPLETED ||
             intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) &&
            Prefs.enabled(context)) {
            ContextCompat.startForegroundService(
                context, Intent(context, WatchdogService::class.java)
            )
        }
    }
}
