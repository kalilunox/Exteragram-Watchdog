package com.exteragram.watchdog

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var statusSub: TextView
    private lateinit var shield: TextView
    private lateinit var intervalText: TextView
    private lateinit var interval: SeekBar

    private val bg = Color.rgb(10, 14, 24)
    private val card = Color.rgb(20, 27, 42)
    private val accent = Color.rgb(70, 130, 255)
    private val green = Color.rgb(55, 210, 130)
    private val white = Color.rgb(245, 247, 252)
    private val muted = Color.rgb(150, 160, 180)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        buildUi()

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                10
            )
        }

        updateUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 28, 28, 24)
            setBackgroundColor(bg)
        }

        val header = TextView(this).apply {
            text = "🛡  Exteragram Watchdog"
            textSize = 22f
            setTextColor(white)
            setPadding(0, 8, 0, 24)
            gravity = Gravity.CENTER_VERTICAL
        }
        root.addView(header)

        val shieldArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                -1, 0, 1.0f
            )
        }

        shield = TextView(this).apply {
            text = "✓"
            textSize = 54f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = shieldBackground()
            elevation = 12f
            setOnClickListener { toggleWatchdog() }
        }

        shieldArea.addView(
            shield,
            LinearLayout.LayoutParams(190, 190).apply {
                gravity = Gravity.CENTER
            }
        )

        statusText = TextView(this).apply {
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(white)
            setPadding(0, 22, 0, 4)
        }
        shieldArea.addView(statusText)

        statusSub = TextView(this).apply {
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(muted)
        }
        shieldArea.addView(statusSub)

        root.addView(shieldArea)

        val settingsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 18, 22, 18)
            background = rounded(card, 22f)
        }

        val intervalTitle = TextView(this).apply {
            text = "ИНТЕРВАЛ ПРОВЕРКИ"
            textSize = 12f
            setTextColor(muted)
        }
        settingsCard.addView(intervalTitle)

        intervalText = TextView(this).apply {
            textSize = 18f
            setTextColor(white)
            setPadding(0, 8, 0, 2)
        }
        settingsCard.addView(intervalText)

        interval = SeekBar(this).apply {
            min = 1
            max = 59
            progress = Prefs.interval(this@MainActivity) - 1

            setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        val seconds = progress + 1
                        intervalText.text = "$seconds секунд"
                        if (fromUser) {
                            Prefs.setInterval(
                                this@MainActivity,
                                seconds
                            )
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                }
            )
        }

        settingsCard.addView(interval)

        val target = TextView(this).apply {
            text = "\nExteragram\ncom.exteragram.messenger"
            textSize = 14f
            setTextColor(muted)
        }
        settingsCard.addView(target)

        root.addView(settingsCard)

        val hint = TextView(this).apply {
            text = "\nНажми на щит, чтобы включить или остановить сторож."
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(muted)
        }
        root.addView(hint)

        setContentView(root)
    }

    private fun toggleWatchdog() {
        if (Prefs.enabled(this)) {
            stopService(
                Intent(this, WatchdogService::class.java)
            )
            Prefs.setEnabled(this, false)
            Toast.makeText(
                this,
                "Сторож остановлен",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            ContextCompat.startForegroundService(
                this,
                Intent(this, WatchdogService::class.java)
            )
            Prefs.setEnabled(this, true)
            Toast.makeText(
                this,
                "Сторож включён",
                Toast.LENGTH_SHORT
            ).show()
        }

        updateUi()
    }

    private fun updateUi() {
        val enabled = Prefs.enabled(this)

        if (enabled) {
            statusText.text = "ЗАЩИЩЕНО"
            statusSub.text = "Watchdog работает в фоне"
            shield.text = "✓"
            shield.background = shieldBackground(green)
        } else {
            statusText.text = "СТОРОЖ ОТКЛЮЧЕН"
            statusSub.text = "Нажмите на щит для запуска"
            shield.text = "○"
            shield.background = shieldBackground(accent)
        }

        intervalText.text = "${Prefs.interval(this)} секунд"
    }

    private fun shieldBackground(color: Int = accent): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            setStroke(5, Color.argb(60, 255, 255, 255))
        }
    }

    private fun rounded(color: Int, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
        }
    }

    override fun onResume() {
        super.onResume()
        updateUi()
    }
}
