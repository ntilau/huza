# Huza Bridge

Forward Android notifications to macOS — no Apple Developer account, no local server, no cloud account required.

Android captures every notification and posts its title, text, and app name to a [ntfy.sh](https://ntfy.sh) topic via a single HTTPS POST. Chrome on the Mac subscribes to the same topic over SSE (Server-Sent Events) and surfaces each one as a native macOS Notification Center alert. It's a dumb pipe.

## How it works

```
Android notification posted
  → NotificationBridgeService (NotificationListenerService)
    → whitelist check
    → OkHttp POST to ntfy.sh/<topic>
      → ntfy.sh pub-sub relay (SSE)
        → Chrome on Mac subscribed via browser notification API
          → macOS Notification Center
```

- **No local server** — ntfy.sh is a free, open-source pub-sub relay. You can also self-host it.
- **No Apple Developer account** — Chrome's built-in notification bridge is all the Mac needs.
- **No cloud account** — the topic name is the only shared secret; pick a random one and it's private.

## Android setup

1. Build and install the APK:
   ```bash
   export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
   ./gradlew assembleDebug
   $ANDROID_HOME/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
2. Open the app, go to **Settings**, and note the auto-generated topic (or set your own).
3. Grant **Notification Access** from the main screen (system settings → Notification listeners → Huza Bridge).

## Mac setup

1. Open Chrome and navigate to `https://ntfy.sh/<your-topic>`.
2. Click **Subscribe** (or open the topic URL directly — ntfy's web UI auto-subscribes).
3. Allow browser notifications when Chrome prompts.

Notifications will now appear in macOS Notification Center as they arrive.

### Optional: run at login

Wrap Chrome in a small helper so it starts on login but stays out of the way. Save this as an app (Script Editor → Export → File Format: Application, check "Stay open after run handler") and add it to **System Settings → General → Login Items**:

```applescript
do shell script "open -j -a 'Google Chrome' 'https://ntfy.sh/<your-topic>'"
```

The `-j` flag launches Chrome hidden so it doesn't steal focus on login.

## Settings

- **ntfy.sh Server** — default `https://ntfy.sh`. Point to a self-hosted instance if you prefer.
- **Topic** — auto-generated (`huza-XXXXXXXX`) on first open. Regenerate anytime with "Generate New Topic". Both devices must use the same topic.
- **Filter mode** — all notifications or whitelist-only. When whitelist mode is on, only notifications from apps whose package names match an entry in the whitelist are forwarded.

## Tech

Kotlin, Android 9+ (API 28), AGP 8.7.3, Kotlin 2.1.0, OkHttp 4.12, EncryptedSharedPreferences (AES-256-GCM, Android Keystore backed). Target SDK 35.

## Privacy

Notifications never touch a database or third-party server you don't control. The ntfy.sh public server is [open source](https://github.com/binwiederhier/ntfy) and discards messages after delivery to subscribers. If you self-host ntfy, no data leaves your network.

## License

MIT — see [LICENSE](LICENSE).
