package dev.sleepguard.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import dev.sleepguard.logic.ScheduleChecker
import dev.sleepguard.model.*
import java.time.LocalTime

@Composable fun HomeScreen(settings: GuardSettings, alerts: List<AlertTrigger>, update: SettingsChange,
    navigate: (String) -> Unit, test: (Boolean) -> Unit, ready: Boolean) {
    Section("Your night, protected") {
        Text(if (!settings.enabled) "OFF" else if (ScheduleChecker.isActive(settings.schedule, LocalTime.now())) "ON · monitoring now" else "ON · waiting for sleep window",
            style = MaterialTheme.typography.headlineSmall)
        Toggle("Enable Sleep Guard", settings.enabled, { value -> update { it.copy(enabled = value) } })
        Text(if (settings.schedule.allDay) "Monitor all day" else "${formatMinute(settings.schedule.startMinute)} → ${formatMinute(settings.schedule.endMinute)}")
        Text("Mentions stay on this phone. Your chosen keywords decide what wakes you.")
        if (!ready) Text("Setup needs attention. Background alarms may be restricted.", color = MaterialTheme.colorScheme.error)
        OutlinedButton(onClick = { navigate("Schedule") }) { Text("Edit Schedule") }
    }
    Section("Watching for you") {
        MonitoredApps.supported.forEach { Text("${if (it.packageName in settings.packages) "✓" else "—"} ${it.label}") }
        Text(settings.keywords.filter { it.enabled }.joinToString(" · ") { it.text }.ifEmpty { "No enabled keywords" })
        OutlinedButton(onClick = { navigate("Keywords") }) { Text("Edit Keywords") }
        OutlinedButton(onClick = { navigate("Apps") }) { Text("Monitored Apps") }
    }
    Section("Check your alarm") {
        Text("Uses your device alarm volume. Test before relying on Sleep Guard.")
        Button(onClick = { test(false) }) { Text("Test Alarm") }
        OutlinedButton(onClick = { test(true) }) { Text("Test in 10 seconds · lock phone now") }
    }
    Section("Recent alerts") {
        if (alerts.isEmpty()) Text("No alerts yet")
        alerts.take(3).forEach { Text("${it.appName} · ${it.keyword} · ${if (it.dismissed) "dismissed" else "not dismissed"}") }
        OutlinedButton(onClick = { navigate("History") }) { Text("Recent Alerts") }
    }
    Button(onClick = { navigate("Setup") }) { Text("Setup / Permissions") }
    TextButton(onClick = { navigate("Privacy") }) { Text("About / Privacy") }
}
