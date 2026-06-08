# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & install

```bash
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
./gradlew assembleDebug                                    # build debug APK
$ANDROID_HOME/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
$ANDROID_HOME/platform-tools/adb shell am start -n com.huza.bridge/.MainActivity
```

Gradle wrapper is committed; `gradle --version` is 9.5.1 local, wrapper targets 8.11.1. AGP 8.7.3, Kotlin 2.1.0, compileSdk 35, minSdk 28, targetSdk 35.

## Architecture

This is a single Android app (`com.huza.bridge`) that forwards notifications from the device to a Mac via **ntfy.sh** (free pub-sub relay). Android captures every posted notification through `NotificationListenerService`, POSTs the title/text/app to `https://ntfy.sh/<topic>`, and Chrome on the Mac subscribed to the same topic surfaces them as native macOS Notification Center alerts.

### Data flow

```
Android app posts notification
  → NotificationBridgeService (NotificationListenerService)
    → filters by package whitelist (all vs whitelist mode)
    → NtfySender (OkHttp POST to ntfy.sh/<topic> with Title header)
      → ntfy.sh pub-sub relay
        → Chrome on Mac subscribed via SSE
          → macOS Notification Center
```

### Key components

- **`NotificationBridgeService`** — extends `NotificationListenerService`. Runs as a foreground service to avoid being killed. On start, creates a low-importance persistent notification. On each posted notification, extracts title/text/bigText/appLabel, checks filter mode, then sends via `NtfySender`. On disconnect, calls `requestRebind(ComponentName)` to self-recover.
- **`NtfySender`** — singleton, OkHttp client with 15s timeouts. POSTs plain text body to `$server/$topic`, sets `Title` header to `"$app — $title"`, `Priority: default`, `Tags: incoming_envelope`. Runs on `Dispatchers.IO`.
- **`SecurePreferences`** — wraps `EncryptedSharedPreferences` (AES-256-GCM, Android Keystore backed). Stores `ntfyServer` (default `https://ntfy.sh`), `ntfyTopic` (auto-generated `huza-XXXXXXXX` UUID prefix), filter mode, package whitelist.
- **`SettingsActivity`** — Material TextInputLayout fields for server URL and topic name. Auto-generates topic on first open. "Generate New Topic" button replaces the topic.
- **`MainActivity`** — checks whether notification listener is enabled in system settings (`enabled_notification_listeners`) and whether a ntfy topic is configured. Links to system notification access settings and to `SettingsActivity`.
- **`BootReceiver`** — placeholder `BroadcastReceiver` for `BOOT_COMPLETED`; the `NotificationListenerService` is system-bound after boot, so no explicit restart is needed.

### Expected Mac-side setup

The Mac just opens `https://ntfy.sh/<topic>` in Chrome, clicks Subscribe, and allows browser notifications. No local server, no Apple Developer account.
