package com.huza.bridge

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationBridgeService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var prefs: SecurePreferences

    override fun onCreate() {
        super.onCreate()
        prefs = SecurePreferences(this)
        createForegroundNotification()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!prefs.isConfigured() || !prefs.isServiceEnabled) return

        val extras = sbn.notification.extras
        val packageName = sbn.packageName

        if (shouldSkip(packageName)) return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val appName = getAppLabel(packageName)

        // If there's a big text payload, prefer that for the body
        val body = if (extras.containsKey(Notification.EXTRA_BIG_TEXT)) {
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: text
        } else {
            text
        }

        scope.launch {
            NtfySender.send(
                server = prefs.ntfyServer,
                topic = prefs.ntfyTopic,
                title = title,
                text = body,
                app = appName,
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Not forwarded
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        prefs.isServiceEnabled = true
        Log.d(TAG, "Notification listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        prefs.isServiceEnabled = false
        Log.d(TAG, "Notification listener disconnected — requesting rebind")
        requestRebind(ComponentName(this, NotificationBridgeService::class.java))
    }

    private fun shouldSkip(packageName: String): Boolean {
        val mode = prefs.filterMode
        if (mode == SecurePreferences.FILTER_MODE_ALL) return false
        if (mode == SecurePreferences.FILTER_MODE_WHITELIST) {
            val whitelist = prefs.packageWhitelist
            return whitelist.none { packageName.startsWith(it) }
        }
        return false
    }

    private fun getAppLabel(packageName: String): String = try {
        packageManager.getApplicationLabel(
            packageManager.getApplicationInfo(packageName, 0)
        ).toString()
    } catch (e: Exception) {
        packageName
    }

    private fun createForegroundNotification() {
        val channelId = "huza_foreground"
        val channel = NotificationChannel(
            channelId,
            "Notification Bridge",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps notification bridge active"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Huza Bridge Active")
            .setContentText("Forwarding notifications to Mac")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(FOREGROUND_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(FOREGROUND_ID, notification)
        }
    }

    companion object {
        private const val TAG = "NotificationBridge"
        private const val FOREGROUND_ID = 4242
    }
}
