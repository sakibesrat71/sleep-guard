package dev.sleepguard.alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import dev.sleepguard.model.AlertTrigger

class AlarmNotifications(private val context: Context) {
    private val manager = context.getSystemService(NotificationManager::class.java)
    fun createChannel() {
        val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
        val channel = NotificationChannel(CHANNEL, "Sleep Guard alarms", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Keyword alerts during your configured monitoring window"
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), attributes)
            // Opt-in policy access permits bypass; no DND state is changed.
            if (manager.isNotificationPolicyAccessGranted) setBypassDnd(true)
        }
        manager.createNotificationChannel(channel)
    }
    fun build(alert: AlertTrigger, restricted: Boolean = false): Notification {
        createChannel()
        val screen = PendingIntent.getActivity(context, 1,
            Intent(context, AlarmActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getBroadcast(context, 2, Intent(context, StopAlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(dev.sleepguard.R.drawable.ic_guard)
            .setContentTitle(if (restricted) "Sleep Guard: tap to start alarm" else "Sleep Guard alert · ${alert.appName}")
            .setContentText("Important keyword matched. Open to view details.")
            .setCategory(NotificationCompat.CATEGORY_ALARM).setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setOngoing(true).setAutoCancel(false)
            .setOnlyAlertOnce(true).setContentIntent(screen)
            .addAction(0, "STOP ALARM", stop)
        if (Build.VERSION.SDK_INT < 34 || manager.canUseFullScreenIntent()) builder.setFullScreenIntent(screen, true)
        return builder.build()
    }
    fun publish(alert: AlertTrigger, restricted: Boolean = false) {
        runCatching { manager.notify(ID, build(alert, restricted)) }
    }
    fun cancel() = manager.cancel(ID)
    companion object { const val CHANNEL = "sleep_guard_alarms"; const val ID = 710 }
}
