package dev.sleepguard.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.sleepguard.model.*

@Composable fun AppSelectionScreen(settings: GuardSettings, update: SettingsChange) {
    val context = LocalContext.current
    Section("Monitored applications") {
        MonitoredApps.supported.forEach { app ->
            val installed = runCatching { context.packageManager.getApplicationInfo(app.packageName, 0) }.isSuccess
            Toggle(app.label, app.packageName in settings.packages, { enabled -> update {
                it.copy(packages = if (enabled) it.packages + app.packageName else it.packages - app.packageName)
            } }, if (installed) "Installed · ${app.packageName}" else "Not installed in this profile")
        }
        Text("Enable message previews in Teams and Slack. Sleep Guard can only see text included in Android notifications. Work-profile notifications may be blocked by organisation policy.")
    }
}
