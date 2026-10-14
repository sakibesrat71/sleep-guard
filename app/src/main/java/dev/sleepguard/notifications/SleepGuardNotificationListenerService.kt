package dev.sleepguard.notifications

import android.os.SystemClock
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import dev.sleepguard.BuildConfig
import dev.sleepguard.GuardApplication
import dev.sleepguard.logic.NotificationProcessor
import dev.sleepguard.model.MonitoredApps
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import java.time.LocalTime

class SleepGuardNotificationListenerService : NotificationListenerService() {
    private val app get() = application as GuardApplication
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val queue = Channel<StatusBarNotification>(Channel.UNLIMITED)
    private val extractor = NotificationTextExtractor()
    private val processor = NotificationProcessor()
    private var connectedAt = Long.MAX_VALUE
    override fun onCreate() {
        super.onCreate()
        scope.launch {
            for (sbn in queue) {
                try {
                    val settings = app.settings.settings.first()
                    if (!settings.enabled || sbn.packageName !in settings.packages) continue
                    for (event in extractor.extract(sbn)) {
                        val alert = processor.process(event, settings, LocalTime.now(), SystemClock.elapsedRealtime()) ?: continue
                        app.alarms.trigger(alert, sbn.notification.contentIntent)
                        if (BuildConfig.DEBUG) Log.d("SleepGuard", "Keyword match processed")
                    }
                } catch (_: Exception) {
                    // Never include exception details: vendor extras can contain private text.
                    if (BuildConfig.DEBUG) Log.w("SleepGuard", "Notification processing failed")
                }
            }
        }
    }
    override fun onListenerConnected() { connectedAt = System.currentTimeMillis() }
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null || sbn.postTime < connectedAt || MonitoredApps.supported.none { it.packageName == sbn.packageName }) return
        queue.trySend(sbn)
    }
    override fun onDestroy() { queue.close(); scope.cancel(); super.onDestroy() }
}
