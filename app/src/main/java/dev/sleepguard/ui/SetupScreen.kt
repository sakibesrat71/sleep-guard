package dev.sleepguard.ui

import android.Manifest
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import dev.sleepguard.alarm.AlarmNotifications
import dev.sleepguard.notifications.SleepGuardNotificationListenerService

data class SetupStatus(val listener: Boolean, val notifications: Boolean, val batteryExempt: Boolean,
    val fullScreen: Boolean, val dndAccess: Boolean, val channelEnabled: Boolean, val volume: Int) {
    val ready get() = listener && notifications && batteryExempt && fullScreen && channelEnabled && volume > 0
}
fun setupStatus(context: Context): SetupStatus {
    val manager = context.getSystemService(NotificationManager::class.java)
    val listener = manager.isNotificationListenerAccessGranted(ComponentName(context, SleepGuardNotificationListenerService::class.java))
    val channel = manager.getNotificationChannel(AlarmNotifications.CHANNEL)
    return SetupStatus(listener, NotificationManagerCompat.from(context).areNotificationsEnabled(),
        context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName),
        Build.VERSION.SDK_INT < 34 || manager.canUseFullScreenIntent(), manager.isNotificationPolicyAccessGranted,
        channel == null || channel.importance >= NotificationManager.IMPORTANCE_HIGH,
        context.getSystemService(AudioManager::class.java).getStreamVolume(AudioManager.STREAM_ALARM))
}
fun openSettings(context: Context, action: String, withPackage: Boolean = false) {
    val intent = Intent(action)
    if (withPackage) intent.data = Uri.parse("package:${context.packageName}")
    if (runCatching { context.startActivity(intent) }.isFailure)
        runCatching { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))) }
}
@Composable fun SetupScreen(context: Context, status: SetupStatus, requestNotifications: () -> Unit) {
    Section("Alarm readiness") {
        Text("Return here after changing Android settings to refresh these checks.")
        Text("Notification Access: ${if (status.listener) "granted" else "missing"}")
        Button(onClick = { openSettings(context, Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS) }) { Text("Grant Notification Access") }
        Text("Notification permission: ${if (status.notifications) "enabled" else "disabled"}")
        Button(onClick = requestNotifications) { Text("Allow notifications") }
        Text("Battery optimisation: ${if (status.batteryExempt) "exempt" else "background alarms may be blocked"}")
        Button(onClick = { openSettings(context, Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS) }) { Text("Battery optimisation settings") }
        Text("Find Sleep Guard, choose All apps if needed, and select Don't optimise. This allows background alarm service starts on supported Android versions.")
        Button(onClick = { openSettings(context, Settings.ACTION_APPLICATION_DETAILS_SETTINGS, true) }) { Text("App battery / background settings") }
        Text("Full-screen alarm access: ${if (status.fullScreen) "allowed" else "restricted"}")
        if (Build.VERSION.SDK_INT >= 34) Button(onClick = { openSettings(context, Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, true) }) {
            Text("Full-screen alarm settings")
        }
        Text("Alarm channel: ${if (status.channelEnabled) "high importance" else "disabled or reduced importance"}")
        Button(onClick = {
            AlarmNotifications(context).createChannel()
            runCatching { context.startActivity(Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName).putExtra(Settings.EXTRA_CHANNEL_ID, AlarmNotifications.CHANNEL)) }
        }) { Text("Alarm channel settings") }
        Text("Alarm volume: ${status.volume}. Sleep Guard does not change global volume. Raise alarm volume manually, then test.")
        Text("DND Policy Access: ${if (status.dndAccess) "granted" else "optional · not granted"}")
        Button(onClick = { openSettings(context, Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS) }) { Text("DND access settings") }
        Text("Sleep Guard never toggles DND. Allow alarms in DND, and review the alarm channel's DND override where available. Existing channel settings remain under your control.")
    }
    Section("Samsung Galaxy S24 Ultra") {
        Text("In Settings → Apps → Sleep Guard → Battery, choose Unrestricted where offered. In Battery → Background usage limits, remove Sleep Guard from Sleeping and Deep sleeping apps, and add it to Never sleeping apps where available.")
        Text("Names and paths vary by One UI version. Notification Access and battery exemption are separate settings. Check both, allow notifications, and review full-screen alarm access. After updates or reboot, verify setup and run a locked-phone test.")
        Text("Teams and Slack must send notifications with message previews. Muted channels, paused notifications, organisation restrictions, hidden previews, and work profiles can prevent detection.")
    }
    Section("Android limitations") {
        Text("Android may show a heads-up alert instead of a full-screen screen, especially while unlocked. If background service launch is blocked, Sleep Guard posts a notification; tap it to start looping audio. A notification alone is not a guaranteed wake-up alarm.")
        Text("DND, alarm volume, notification settings, OEM power management, force-stop, and process termination can prevent or end alarms. No app can guarantee a wake-up in every device state. Validate your phone before relying on it.")
    }
}
