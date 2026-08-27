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

            checkTarget()

            handler.postDelayed(
                this,
                Prefs.interval(this@WatchdogService) * 1000L
            )
        }
    }

    override fun onCreate() {
        super.onCreate()

        createChannel()

        startForeground(
            NOTIFICATION_ID,
            notification()
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        checking = true

        Prefs.setEnabled(this, true)

        handler.removeCallbacks(checker)
        handler.post(checker)

        return START_STICKY
    }

    private fun checkTarget() {
        /*
         * ВАЖНО:
         * Watchdog больше НЕ запускает Activity Exteragram.
         *
         * Поэтому он не сможет вытащить Exteragram
         * поверх браузера, игры или другого приложения.
         *
         * Здесь пока только проверяем наличие приложения.
         */
        if (!isPackageInstalled(PKG)) return

        isTargetRunning()
    }

    private fun isPackageInstalled(pkg: String): Boolean {
        return try {
            packageManager.getPackageInfo(pkg, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    @Suppress("DEPRECATION")
    private fun isTargetRunning(): Boolean {
        val am =
            getSystemService(Context.ACTIVITY_SERVICE)
                    as ActivityManager

        return am.runningAppProcesses?.any { process ->
            process.pkgList?.contains(PKG) == true
        } == true
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL,
                    "Exteragram Watchdog",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    private fun notification(): Notification {
        return NotificationCompat.Builder(
            this,
            CHANNEL
        )
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentTitle("Exteragram Watchdog")
            .setContentText("Сторож работает в фоне")
            .setOngoing(true)
            .setCategory(
                NotificationCompat.CATEGORY_SERVICE
            )
            .build()
    }

    override fun onDestroy() {
        checking = false
        handler.removeCallbacksAndMessages(null)
        Prefs.setEnabled(this, false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null
}
