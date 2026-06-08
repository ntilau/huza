package com.huza.bridge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = SecurePreferences(context)
            if (!prefs.isConfigured()) return
            // The NotificationListenerService is bound by the system after boot
            // when the user has granted notification access — no explicit start needed.
            // This receiver exists so we can re-check state; the service self-recovers
            // via onListenerDisconnected -> requestRebind.
        }
    }
}
