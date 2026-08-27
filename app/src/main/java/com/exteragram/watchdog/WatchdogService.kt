package com.exteragram.watchdog

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import androidx.core.app.NotificationCompat

class WatchdogService : Service() {
    companion object {
        private const val CHANNEL = "watchdog"
        private const val NOTIFICATION_ID = 1001
        private const val PKG = "com.exteragram.messenger"
    }

    private val handler = Handler(Looper.getMainLooper())
    private var checking = false

    private val checker = object : Runnable {
        override fun run() {
            if (!checking) return
            ensureTargetRunning()
            handler.postDelayed(this, Prefs.interval(this@WatchdogService) * 1000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIFICATION_ID, notification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        checking = true
        handler.removeCallbacks(checker)
        handler.post(checker)
        return START_STICKY
    }

    private fun ensureTargetRunning() {
        if (!isPackageInstalled(PKG)) return
        if (isPackageInRunningProcesses(PKG)) return

        try {
            val launch = packageManager.getLaunchIntentForPackage(PKG)
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                startActivity(launch)
            }
        } catch (_: Exception) {
            // Android may refuse the launch after a force-stop or under OEM restrictions.
        }
    }

    private fun isPackageInstalled(pkg: String): Boolean = try {
        packageManager.getPackageInfo(pkg, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    @Suppress("DEPRECATION")
    private fun isPackageInRunningProcesses(pkg: String): Boolean {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return am.runningAppProcesses?.any { p ->
            p.pkgList?.contains(pkg) == true
        } == true
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL, "Exteragram Watchdog", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun notification(): Notification =
        NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentTitle("Exteragram Watchdog")
            .setContentText("Сторож работает")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

    override fun onDestroy() {
        checking = false
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null
}
