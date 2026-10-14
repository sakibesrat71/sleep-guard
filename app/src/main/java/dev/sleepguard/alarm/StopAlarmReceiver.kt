package dev.sleepguard.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.sleepguard.GuardApplication

class StopAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as GuardApplication).alarms.stop()
    }
}
