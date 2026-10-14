package dev.sleepguard.logic

import dev.sleepguard.model.*
import org.junit.Assert.*
import org.junit.Test

class KeywordMatcherTest {
    private val matcher = KeywordMatcher()
    private val keywords = listOf(Keyword("1", "Sakib Esrat"), Keyword("2", "Esrat"))
    private fun match(text: String) = matcher.match(listOf(TextField("message", text)), keywords)
    @Test fun matchesCaseInsensitivePartialSentence() {
        assertEquals("1", match("SAKIB ESRAT can you check production?")?.keyword?.id)
        assertEquals("message", match("Hello Sakib Esrat!")?.field?.name)
    }
    @Test fun normalizesUnicodeAndRepeatedWhitespace() {
        assertNotNull(match("  Sakib\n\t  Esrat  "))
        assertNotNull(match("Sakib\u00A0Esrat"))
    }
    @Test fun supportsMultipleKeywordsInStableOrder() { assertEquals("2", match("Can someone get Esrat?")?.keyword?.id) }
    @Test fun noMatchForUnrelatedText() { assertNull(match("Deployment completed successfully")) }
    @Test fun ignoresDisabledAndEmptyKeywords() {
        assertNull(matcher.match(listOf(TextField("text", "Sakib")), listOf(Keyword("1", "Sakib", false), Keyword("2", " "))))
    }
    @Test fun scansAllFields() {
        assertEquals("sender", matcher.match(listOf(TextField("text", "Hello"), TextField("sender", "Sakib Esrat")), keywords)?.field?.name)
    }
}
