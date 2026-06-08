package com.huza.bridge

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: SecurePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = SecurePreferences(this)

        val serverInput = findViewById<TextInputEditText>(R.id.ntfy_server)
        val topicInput = findViewById<TextInputEditText>(R.id.ntfy_topic)

        serverInput.setText(prefs.ntfyServer)

        if (prefs.ntfyTopic.isBlank()) {
            prefs.generateTopic()
        }
        topicInput.setText(prefs.ntfyTopic)

        findViewById<android.widget.Button>(R.id.btn_save).setOnClickListener {
            val server = serverInput.text?.toString()?.trim()?.removeSuffix("/") ?: ""
            val topic = topicInput.text?.toString()?.trim() ?: ""

            if (server.isBlank()) {
                serverInput.error = "Server URL is required"
                return@setOnClickListener
            }
            if (topic.isBlank()) {
                topicInput.error = "Topic name is required"
                return@setOnClickListener
            }

            prefs.ntfyServer = server
            prefs.ntfyTopic = topic

            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<android.widget.Button>(R.id.btn_regenerate).setOnClickListener {
            prefs.generateTopic()
            topicInput.setText(prefs.ntfyTopic)
            Toast.makeText(this, "New topic generated", Toast.LENGTH_SHORT).show()
        }
    }
}
