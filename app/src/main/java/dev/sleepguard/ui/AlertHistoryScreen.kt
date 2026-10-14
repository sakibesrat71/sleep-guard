package dev.sleepguard.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import dev.sleepguard.model.AlertTrigger
import java.text.DateFormat
import java.util.Date

@Composable fun AlertHistoryScreen(alerts: List<AlertTrigger>, clear: () -> Unit) {
    var confirming by remember { mutableStateOf(false) }
    Text("Latest 100 alerts · stored only on this device")
    OutlinedButton(onClick = { confirming = true }, enabled = alerts.isNotEmpty()) { Text("Clear history") }
    if (confirming) AlertDialog(onDismissRequest = { confirming = false }, title = { Text("Clear recent alerts?") },
        text = { Text("This permanently removes locally stored message details.") },
        confirmButton = { TextButton(onClick = { clear(); confirming = false }) { Text("Clear") } },
        dismissButton = { TextButton(onClick = { confirming = false }) { Text("Cancel") } })
    alerts.forEach { alert ->
        Section("${alert.appName}${if (alert.test) " · test" else ""}") {
            Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(alert.timestamp)))
            alert.sender?.let { Text("Sender: $it") }
            alert.conversation?.let { Text("Conversation: $it") }
            Text(alert.text)
            Text("Matched: ${alert.keyword} · ${alert.matchedField}")
            Text(if (alert.dismissed) "Dismissed" else "Not explicitly dismissed")
        }
    }
    if (alerts.isEmpty()) Text("No recent alerts")
}
