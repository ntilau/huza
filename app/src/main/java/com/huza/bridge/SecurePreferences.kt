package com.huza.bridge

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.UUID

class SecurePreferences(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "huza_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var ntfyServer: String
        get() = prefs.getString(KEY_NTFY_SERVER, "https://ntfy.sh") ?: "https://ntfy.sh"
        set(value) = prefs.edit().putString(KEY_NTFY_SERVER, value).apply()

    var ntfyTopic: String
        get() = prefs.getString(KEY_NTFY_TOPIC, "") ?: ""
        set(value) = prefs.edit().putString(KEY_NTFY_TOPIC, value).apply()

    fun generateTopic(): String {
        val topic = "huza-${UUID.randomUUID().toString().take(8)}"
        ntfyTopic = topic
        return topic
    }

    var filterMode: String
        get() = prefs.getString(KEY_FILTER_MODE, FILTER_MODE_ALL) ?: FILTER_MODE_ALL
        set(value) = prefs.edit().putString(KEY_FILTER_MODE, value).apply()

    var packageWhitelist: Set<String>
        get() = prefs.getStringSet(KEY_PACKAGE_WHITELIST, defaultWhitelist()) ?: defaultWhitelist()
        set(value) = prefs.edit().putStringSet(KEY_PACKAGE_WHITELIST, value).apply()

    var isServiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SERVICE_ENABLED, value).apply()

    fun isConfigured(): Boolean = ntfyTopic.isNotBlank()

    private fun defaultWhitelist(): Set<String> = setOf(
        "com.whatsapp",
        "org.telegram.messenger",
        "com.google.android.apps.messaging",
        "com.facebook.orca",
        "com.instagram.android",
        "com.twitter.android",
        "com.snapchat.android",
        "com.discord",
        "com.slack",
        "com.microsoft.teams",
        "com.google.android.gm",
        "com.google.android.calendar",
    )

    companion object {
        private const val KEY_NTFY_SERVER = "ntfy_server"
        private const val KEY_NTFY_TOPIC = "ntfy_topic"
        private const val KEY_FILTER_MODE = "filter_mode"
        private const val KEY_PACKAGE_WHITELIST = "package_whitelist"
        private const val KEY_SERVICE_ENABLED = "service_enabled"

        const val FILTER_MODE_ALL = "all"
        const val FILTER_MODE_WHITELIST = "whitelist"
    }
}
