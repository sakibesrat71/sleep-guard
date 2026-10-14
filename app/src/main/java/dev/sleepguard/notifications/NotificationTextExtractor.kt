package dev.sleepguard.notifications

import android.app.Notification
import android.os.Bundle
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import dev.sleepguard.logic.normalize
import dev.sleepguard.model.NotificationEvent
import dev.sleepguard.model.TextField

class NotificationTextExtractor {
    private data class VisibleMessage(val text: CharSequence?, val timestamp: Long, val sender: String?)
    private val standard = linkedMapOf(
        Notification.EXTRA_TITLE to "title", Notification.EXTRA_TEXT to "text",
        Notification.EXTRA_BIG_TEXT to "big text", Notification.EXTRA_SUB_TEXT to "sub text",
        Notification.EXTRA_SUMMARY_TEXT to "summary", Notification.EXTRA_CONVERSATION_TITLE to "conversation",
        Notification.EXTRA_TITLE_BIG to "big title", Notification.EXTRA_TEXT_LINES to "text lines"
    )
    private fun visible(value: Any?): List<String> = when (value) {
        is CharSequence -> listOf(value.toString().take(16_384))
        is Array<*> -> value.filterIsInstance<CharSequence>().map { it.toString().take(16_384) }
        is List<*> -> value.filterIsInstance<CharSequence>().map { it.toString().take(16_384) }
        else -> emptyList()
    }
    @Suppress("DEPRECATION")
    fun fields(extras: Bundle): List<TextField> {
        val result = mutableListOf<TextField>()
        for ((key, label) in standard) runCatching { visible(extras.get(key)) }.getOrDefault(emptyList())
            .forEach { result += TextField(label, it) }
        // Only text-shaped custom extras, never arbitrary Parcelable objects, intents, or identifiers.
        extras.keySet().filter { !standard.containsKey(it) && !it.startsWith("android.") &&
            Regex("(?i)(text|message|title|summary|sender|conversation)").containsMatchIn(it) }
            .take(64).forEach { key ->
                runCatching { visible(extras.get(key)) }.getOrDefault(emptyList()).forEach { result += TextField(key, it) }
            }
        return result.filter { normalize(it.text).isNotEmpty() }.distinctBy { normalize(it.text) }
    }
    @Suppress("DEPRECATION")
    fun extract(sbn: StatusBarNotification): List<NotificationEvent> {
        val n = sbn.notification
        val extras = n.extras ?: Bundle.EMPTY
        val style = runCatching { NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(n) }.getOrNull()
        val conversation = runCatching { extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString() }.getOrNull()
            ?: style?.conversationTitle?.toString()
        val messages = style?.messages.orEmpty().map {
            VisibleMessage(it.text, it.timestamp, it.person?.name?.toString() ?: it.sender?.toString())
        }.ifEmpty {
            // Some applications provide EXTRA_MESSAGES without the MessagingStyle template metadata.
            runCatching {
                val bundles = extras.getParcelableArray(Notification.EXTRA_MESSAGES) ?: return@runCatching emptyList()
                bundles.mapNotNull { value ->
                    val bundle = value as? Bundle ?: return@mapNotNull null
                    runCatching {
                        val text = bundle.getCharSequence("text") ?: return@runCatching null
                        val person = bundle.getParcelable<android.app.Person>("sender_person")
                        VisibleMessage(text, bundle.getLong("time"), person?.name?.toString() ?: bundle.getCharSequence("sender")?.toString())
                    }.getOrNull()
                }
            }.getOrDefault(emptyList())
        }
        if (messages.isNotEmpty()) {
            // Each message is its own event. Aggregated EXTRA_TEXT/BIG_TEXT may contain old messages.
            return messages.mapNotNull { message ->
                val text = message.text?.toString()?.take(16_384) ?: return@mapNotNull null
                val sender = message.sender
                val parts = buildList {
                    add(TextField("message", text))
                    sender?.let { add(TextField("sender", it)) }
                    conversation?.let { add(TextField("conversation", it)) }
                    runCatching { extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() }.getOrNull()
                        ?.let { add(TextField("title", it)) }
                }.distinctBy { normalize(it.text) }
                NotificationEvent(sbn.packageName, sbn.key, sbn.postTime, sender, conversation, parts,
                    "${message.timestamp}:${sender.orEmpty()}:$text")
            }
        }
        val parts = fields(extras)
        if (parts.isEmpty()) return emptyList()
        return listOf(NotificationEvent(sbn.packageName, sbn.key, sbn.postTime,
            parts.firstOrNull { it.name == "title" }?.text, conversation, parts))
    }
}

