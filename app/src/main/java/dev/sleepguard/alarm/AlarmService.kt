package dev.sleepguard.alarm

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.*
import android.os.*
import dev.sleepguard.GuardApplication
import kotlinx.coroutines.*

class AlarmService : Service() {
    private val app get() = application as GuardApplication
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var focus: AudioFocusRequest? = null
    private var wakeLock: PowerManager.WakeLock? = null
    override fun onBind(intent: Intent?) = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alert = app.alarms.active.value.lastOrNull() ?: run { stopSelf(); return START_NOT_STICKY }
        val notifications = AlarmNotifications(this)
        try {
            if (Build.VERSION.SDK_INT >= 29) startForeground(AlarmNotifications.ID, notifications.build(alert), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            else startForeground(AlarmNotifications.ID, notifications.build(alert))
        } catch (_: Exception) {
            notifications.publish(alert, restricted = true)
            stopSelf()
            return START_NOT_STICKY
        }
        if (!app.alarms.serviceRunning) {
            app.alarms.serviceRunning = true
            startAlarm()
            scope.launch { app.alarms.active.collect { alerts ->
                alerts.lastOrNull()?.let { notifications.publish(it) }
            } }
        }
        return START_NOT_STICKY
    }
    private fun startAlarm() {
        // Brief CPU wake lock covers activation only; MediaPlayer manages playback wakefulness.
        wakeLock = getSystemService(PowerManager::class.java).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SleepGuard:activation")
            .apply { acquire(10_000) }
        val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        val audio = getSystemService(AudioManager::class.java)
        focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(attributes).setOnAudioFocusChangeListener { /* Alarm continues until explicit stop. */ }.build()
        runCatching { audio.requestAudioFocus(focus!!) }
        player = MediaPlayer().apply {
            setAudioAttributes(attributes)
            setWakeMode(this@AlarmService, PowerManager.PARTIAL_WAKE_LOCK)
            isLooping = true
            setOnErrorListener { mp, _, _ -> mp.reset(); true }
        }
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        try { player?.setDataSource(this, sound); player?.prepare(); player?.start() }
        catch (_: Exception) {
            // Bundled tone is available even if the configured device alarm URI is inaccessible.
            runCatching {
                player?.reset(); player?.setAudioAttributes(attributes)
                resources.openRawResourceFd(dev.sleepguard.R.raw.alarm_tone).use { fd ->
                    player?.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                }
                player?.isLooping = true; player?.prepare(); player?.start()
            }
        }
        vibrator = if (Build.VERSION.SDK_INT >= 31) getSystemService(VibratorManager::class.java).defaultVibrator
            else getSystemService(Vibrator::class.java)
        runCatching { vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 700, 350, 700, 350), 0), attributes) }
        wakeLock?.let { if (it.isHeld) it.release() }
    }
    override fun onDestroy() {
        scope.cancel()
        runCatching { player?.release() }; player = null
        runCatching { vibrator?.cancel() }
        focus?.let { runCatching { getSystemService(AudioManager::class.java).abandonAudioFocusRequest(it) } }
        wakeLock?.let { if (it.isHeld) it.release() }
        app.alarms.serviceDestroyed()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}
