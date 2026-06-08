package com.huza.bridge

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SecurePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = SecurePreferences(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        refreshStatus()

        findViewById<android.widget.Button>(R.id.btn_notification_access).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        findViewById<android.widget.Button>(R.id.btn_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun refreshStatus() {
        val listenerStatus = findViewById<TextView>(R.id.listener_status)
        val configStatus = findViewById<TextView>(R.id.config_status)

        val listeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        ) ?: ""

        val ourComponent = ComponentName(this, NotificationBridgeService::class.java).flattenToString()
        val isListenerEnabled = listeners.contains(ourComponent)

        listenerStatus.text = if (isListenerEnabled) "Notification access: GRANTED"
        else "Notification access: NOT GRANTED"
        listenerStatus.setTextColor(
            getColor(if (isListenerEnabled) android.R.color.holo_green_dark else android.R.color.holo_red_dark)
        )

        val configured = prefs.isConfigured()
        configStatus.text = if (configured) "ntfy.sh topic: CONFIGURED"
        else "ntfy.sh topic: NOT SET"
        configStatus.setTextColor(
            getColor(if (configured) android.R.color.holo_green_dark else android.R.color.holo_red_dark)
        )
    }
}
