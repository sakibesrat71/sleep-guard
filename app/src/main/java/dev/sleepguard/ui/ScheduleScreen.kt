package dev.sleepguard.ui

import android.app.TimePickerDialog
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import dev.sleepguard.model.GuardSettings

@Composable fun ScheduleScreen(settings: GuardSettings, update: SettingsChange) {
    val context = LocalContext.current
    Section("Sleep window") {
        Toggle("Monitor all day", settings.schedule.allDay, { enabled -> update { it.copy(schedule = it.schedule.copy(allDay = enabled)) } })
        fun choose(start: Boolean) {
            val minute = if (start) settings.schedule.startMinute else settings.schedule.endMinute
            TimePickerDialog(context, { _, hour, min -> update {
                it.copy(schedule = if (start) it.schedule.copy(startMinute = hour * 60 + min) else it.schedule.copy(endMinute = hour * 60 + min))
            } }, minute / 60, minute % 60, android.text.format.DateFormat.is24HourFormat(context)).show()
        }
        OutlinedButton(onClick = { choose(true) }) { Text("Start: ${formatMinute(settings.schedule.startMinute)}") }
        OutlinedButton(onClick = { choose(false) }) { Text("End: ${formatMinute(settings.schedule.endMinute)}") }
        Text("Uses the phone's current local time. Overnight windows cross midnight. Start is included; end is excluded.")
        if (!settings.schedule.allDay && settings.schedule.startMinute == settings.schedule.endMinute)
            Text("Equal times create an empty window. Choose different times or Monitor all day.", color = MaterialTheme.colorScheme.error)
    }
    Section("Optional conversation cooldown") {
        Toggle("Enable cooldown", settings.cooldownMinutes > 0,
            { enabled -> update { it.copy(cooldownMinutes = if (enabled) 5 else 0) } })
        if (settings.cooldownMinutes > 0) {
            Text("${settings.cooldownMinutes} minutes")
            Slider(value = settings.cooldownMinutes.toFloat(), onValueChange = { value -> update { it.copy(cooldownMinutes = value.toInt()) } },
                valueRange = 1f..30f, steps = 28)
        }
        Text("Duplicate protection is always active for 5 minutes. Cooldown applies only when a conversation name is available.")
    }
}
