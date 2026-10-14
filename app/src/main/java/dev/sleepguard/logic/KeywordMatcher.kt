package dev.sleepguard.logic

import dev.sleepguard.model.Keyword
import dev.sleepguard.model.TextField
import java.util.Locale

fun normalize(text: String): String = text.trim().replace(Regex("[\\s\\p{Z}]+"), " ").lowercase(Locale.ROOT)
data class Match(val keyword: Keyword, val field: TextField)
interface MentionMatcher { fun match(fields: List<TextField>, keywords: List<Keyword>): Match? }
class KeywordMatcher : MentionMatcher {
    override fun match(fields: List<TextField>, keywords: List<Keyword>): Match? {
        for (keyword in keywords.filter { it.enabled && normalize(it.text).isNotEmpty() }) {
            val needle = normalize(keyword.text)
            fields.firstOrNull { normalize(it.text).contains(needle) }?.let { return Match(keyword, it) }
        }
        return null
    }
}
