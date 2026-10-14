package dev.sleepguard.logic

import java.security.MessageDigest

/** Monotonic elapsed time is supplied by caller. Timestamp of a notification repost is excluded. */
class DuplicateDetector(private val ttlMillis: Long = 5 * 60_000L, private val maxEntries: Int = 2048) {
    private val entries = linkedMapOf<String, Long>()
    @Synchronized fun accept(fingerprint: String, now: Long): Boolean {
        entries.entries.removeAll { now - it.value >= ttlMillis }
        if (entries.containsKey(fingerprint)) return false
        if (entries.size >= maxEntries) entries.remove(entries.keys.first())
        entries[fingerprint] = now
        return true
    }
    companion object {
        fun fingerprint(vararg parts: String?): String {
            val content = parts.joinToString("") { val value = it.orEmpty(); "${value.length}:$value" }
            return MessageDigest.getInstance("SHA-256").digest(content.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
        }
    }
}
