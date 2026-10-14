package dev.sleepguard.model

data class MonitoredApp(val packageName: String, val label: String)
object MonitoredApps {
    val supported = listOf(MonitoredApp("com.microsoft.teams", "Microsoft Teams"), MonitoredApp("com.Slack", "Slack"))
    fun label(packageName: String) = supported.firstOrNull { it.packageName == packageName }?.label ?: "Sleep Guard"
}
