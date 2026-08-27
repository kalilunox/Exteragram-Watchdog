package com.exteragram.watchdog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.core.content.ContextCompat

class MainActivity : android.app.Activity() {
    private lateinit var interval: SeekBar
    private lateinit var value: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 50, 40, 40)
        }

        val title = TextView(this).apply {
            text = "Exteragram Watchdog"
            textSize = 26f
        }
        val info = TextView(this).apply {
            text = "\nСледит за com.exteragram.messenger и запускает его снова, если процесс исчез.\n"
            textSize = 16f
        }

        value = TextView(this).apply { textSize = 18f }
        interval = SeekBar(this).apply {
            min = 1
            max = 59
            progress = Prefs.interval(this@MainActivity) - 1
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                    val sec = p + 1
                    value.text = "Интервал проверки: $sec сек."
                    Prefs.setInterval(this@MainActivity, sec)
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }
        value.text = "Интервал проверки: ${Prefs.interval(this)} сек."

        val start = Button(this).apply {
            text = "Запустить сторож"
            setOnClickListener { startWatchdog() }
        }
        val stop = Button(this).apply {
            text = "Остановить сторож"
            setOnClickListener {
                stopService(Intent(this@MainActivity, WatchdogService::class.java))
                Toast.makeText(this@MainActivity, "Сторож остановлен", Toast.LENGTH_SHORT).show()
            }
        }

        root.addView(title)
        root.addView(info)
        root.addView(value)
        root.addView(interval)
        root.addView(start)
        root.addView(stop)
        setContentView(root)

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 10)
        }
    }

    private fun startWatchdog() {
        val i = Intent(this, WatchdogService::class.java)
        ContextCompat.startForegroundService(this, i)
        Prefs.setEnabled(this, true)
        Toast.makeText(this, "Сторож запущен", Toast.LENGTH_SHORT).show()
    }
}
