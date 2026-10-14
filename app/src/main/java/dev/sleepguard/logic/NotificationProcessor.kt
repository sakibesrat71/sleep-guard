package dev.sleepguard.logic

import dev.sleepguard.model.*
import java.time.LocalTime
import java.util.UUID

class NotificationProcessor(
    private val matcher: MentionMatcher = KeywordMatcher(),
    private val duplicates: DuplicateDetector = DuplicateDetector(),
    private val cooldown: ConversationCooldown = ConversationCooldown()
) {
    fun process(event: NotificationEvent, settings: GuardSettings, time: LocalTime, elapsed: Long): AlertTrigger? {
        if (!settings.enabled || event.packageName !in settings.packages ||
            MonitoredApps.supported.none { it.packageName == event.packageName } ||
            !ScheduleChecker.isActive(settings.schedule, time)) return null
        val match = matcher.match(event.fields, settings.keywords) ?: return null
        val message = event.fields.firstOrNull { it.name == "message" }?.text
            ?: event.fields.firstOrNull { it.name == "big text" }?.text
            ?: event.fields.firstOrNull { it.name == "text" }?.text
        // Cosmetic summary / badge-count updates must not change an otherwise identical message identity.
        val content = message?.let(::normalize) ?: event.fields.joinToString("\n") { normalize(it.text) }
        val fingerprint = DuplicateDetector.fingerprint(event.packageName, event.key, event.sender,
            event.conversation, event.messageIdentity ?: content)
        if (!duplicates.accept(fingerprint, elapsed)) return null
        // Unknown conversations never suppress unrelated messages via a guessed shared key.
        val conversationKey = event.conversation?.let { "${event.packageName}:$it" }
        if (!cooldown.accept(conversationKey, settings.cooldownMinutes, elapsed)) return null
        return AlertTrigger(UUID.randomUUID().toString(), event.receivedAt, event.packageName,
            MonitoredApps.label(event.packageName), event.sender, event.conversation,
            match.keyword.text, match.field.name, (message ?: match.field.text).take(8192))
    }
}
