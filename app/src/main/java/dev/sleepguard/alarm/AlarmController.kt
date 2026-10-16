package dev.sleepguard.alarm

import android.app.PendingIntent
import android.annotation.SuppressLint
import android.content.Intent
import androidx.core.content.ContextCompat
import dev.sleepguard.GuardApplication
import dev.sleepguard.model.AlertTrigger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AlarmController(private val app: GuardApplication) {
    private val state = MutableStateFlow<List<AlertTrigger>>(emptyList())
    val active = state.asStateFlow()
    private val destinations = mutableMapOf<String, PendingIntent>()
    var serviceRunning = false
        internal set
    var startRestricted = false
        private set
    suspend fun trigger(alert: AlertTrigger, destination: PendingIntent? = null) {
        app.history.add(alert)
        state.value = (state.value + alert).takeLast(100)
        destination?.let { destinations[alert.id] = it }
        destinations.keys.retainAll(state.value.map { it.id }.toSet())
        resume()
    }
    fun resume() {
        if (state.value.isEmpty()) return
        startRestricted = false
        try { ContextCompat.startForegroundService(app, Intent(app, AlarmService::class.java)) }
        catch (_: IllegalStateException) { fallback() }
        catch (_: SecurityException) { fallback() }
    }
    private fun fallback() {
        startRestricted = true
        AlarmNotifications(app).publish(state.value.last(), restricted = true)
    }
    // stopService compares the resolved ComponentName, not Intent object identity (lint SAM false positive).
    @SuppressLint("ImplicitSamInstance")
    fun stop(onDismissed: () -> Unit = {}) {
        val ids = state.value.map { it.id }.toSet()
        state.value = emptyList()
        destinations.clear()
        app.stopService(Intent(app, AlarmService::class.java))
        AlarmNotifications(app).cancel()
        app.scope.launch {
            try { app.history.dismiss(ids) } catch (_: java.io.IOException) { /* Audio already stopped; history write failed. */ }
            finally { onDismissed() }
        }
    }
    fun serviceDestroyed() { serviceRunning = false }
    fun openSource(alert: AlertTrigger) {
        val original = destinations[alert.id]
        if (original != null) {
            try { original.send(); return } catch (_: PendingIntent.CanceledException) { /* launch app */ }
            catch (_: SecurityException) { /* launch app */ }
        }
        app.packageManager.getLaunchIntentForPackage(alert.packageName)?.let {
            runCatching { app.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        }
    }
    suspend fun test() = trigger(AlertTrigger(UUID.randomUUID().toString(), System.currentTimeMillis(),
        app.packageName, "Sleep Guard · test", "Test sender", "Alarm check", "Test keyword", "test message",
        "Test keyword, this is your local test alarm.", test = true))
}
