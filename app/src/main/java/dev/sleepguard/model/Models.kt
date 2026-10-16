package dev.sleepguard.model

import kotlinx.serialization.Serializable

@Serializable data class Keyword(val id: String, val text: String, val enabled: Boolean = true)
@Serializable data class SleepSchedule(val startMinute: Int = 23 * 60 + 30, val endMinute: Int = 8 * 60, val allDay: Boolean = false)
@Serializable data class GuardSettings(
    val enabled: Boolean = false,
    val schedule: SleepSchedule = SleepSchedule(),
    val keywords: List<Keyword> = emptyList(),
    val packages: Set<String> = setOf("com.microsoft.teams", "com.Slack"),
    val cooldownMinutes: Int = 0,
    val onboardingDone: Boolean = false,
    val notificationDisclosureAccepted: Boolean = false
)
data class TextField(val name: String, val text: String)
data class NotificationEvent(
    val packageName: String, val key: String, val receivedAt: Long,
    val sender: String?, val conversation: String?, val fields: List<TextField>,
    val messageIdentity: String? = null
)
@Serializable data class AlertTrigger(
    val id: String, val timestamp: Long, val packageName: String, val appName: String,
    val sender: String?, val conversation: String?, val keyword: String,
    val matchedField: String, val text: String, val dismissed: Boolean = false,
    val test: Boolean = false
)
