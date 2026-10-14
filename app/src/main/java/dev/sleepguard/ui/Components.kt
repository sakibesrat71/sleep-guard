package dev.sleepguard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sleepguard.model.GuardSettings
import java.time.LocalTime
import java.time.format.DateTimeFormatter

typealias SettingsChange = ((GuardSettings) -> GuardSettings) -> Unit
fun formatMinute(minute: Int): String = LocalTime.of(minute / 60, minute % 60).format(DateTimeFormatter.ofPattern("h:mm a"))
@Composable fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}
@Composable fun Toggle(label: String, checked: Boolean, change: (Boolean) -> Unit, detail: String? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(label)
            detail?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
        Switch(checked = checked, onCheckedChange = change)
    }
}
