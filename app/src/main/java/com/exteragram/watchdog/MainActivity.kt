package com.exteragram.watchdog

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    private var connected = false
    private lateinit var status: TextView
    private lateinit var shield: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 70, 32, 32)
            setBackgroundColor(Color.rgb(10, 12, 18))
        }

        val title = TextView(this).apply {
            text = "KRAKEN VPN"
            textSize = 30f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        status = TextView(this).apply {
            text = "VPN отключён"
            textSize = 17f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        shield = TextView(this).apply {
            text = "♜"
            textSize = 76f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(30, 34, 45))
            }
            setOnClickListener {
                if (!connected) {
                    requestVpn()
                } else {
                    stopVpn()
                }
            }
        }

        val location = TextView(this).apply {
            text = "🌍  Автоматическая локация"
            textSize = 17f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(20, 35, 20, 35)
        }

        val update = Button(this).apply {
            text = "↻  Обновить подписку"
            setOnClickListener {
                Toast.makeText(
                    this@MainActivity,
                    "Список серверов обновлён",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        root.addView(title)
        root.addView(status)
        root.addView(shield, LinearLayout.LayoutParams(230, 230).apply {
            topMargin = 60
            bottomMargin = 35
        })
        root.addView(location)
        root.addView(update)

        setContentView(root)
    }

    private fun requestVpn() {
        val intent = VpnService.prepare(this)

        if (intent != null) {
            startActivityForResult(intent, 100)
        } else {
            startVpn()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            startVpn()
        }
    }

    private fun startVpn() {
        val intent = Intent(this, KrakenVpnService::class.java)
        startService(intent)

        connected = true
        status.text = "VPN подключён"
        shield.text = "♜"
    }

    private fun stopVpn() {
        stopService(Intent(this, KrakenVpnService::class.java))

        connected = false
        status.text = "VPN отключён"
    }
}
