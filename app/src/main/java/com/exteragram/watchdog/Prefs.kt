package com.exteragram.watchdog

import android.content.Context

object Prefs {
    private const val FILE = "watchdog"
    private const val INTERVAL = "interval"
    private const val ENABLED = "enabled"

    fun interval(c: Context): Int =
        c.getSharedPreferences(FILE, Context.MODE_PRIVATE).getInt(INTERVAL, 5).coerceIn(1, 60)

    fun setInterval(c: Context, seconds: Int) =
        c.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putInt(INTERVAL, seconds.coerceIn(1, 60)).apply()

    fun enabled(c: Context): Boolean =
        c.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(ENABLED, false)

    fun setEnabled(c: Context, enabled: Boolean) =
        c.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putBoolean(ENABLED, enabled).apply()
}
