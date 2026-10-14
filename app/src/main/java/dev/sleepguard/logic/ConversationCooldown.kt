package dev.sleepguard.logic

class ConversationCooldown {
    private val last = linkedMapOf<String, Long>()
    fun accept(key: String?, minutes: Int, now: Long): Boolean {
        if (minutes <= 0 || key == null) return true
        last.entries.removeAll { now - it.value >= minutes * 60_000L }
        if (last.containsKey(key)) return false
        if (last.size >= 2048) last.remove(last.keys.first())
        last[key] = now
        return true
    }
}
