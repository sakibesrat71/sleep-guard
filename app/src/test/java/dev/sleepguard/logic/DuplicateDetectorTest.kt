package dev.sleepguard.logic

import org.junit.Assert.*
import org.junit.Test

class DuplicateDetectorTest {
    @Test fun identicalBlockedUntilExactExpiry() {
        val d = DuplicateDetector(300_000)
        assertTrue(d.accept("a", 1000)); assertFalse(d.accept("a", 1001))
        assertFalse(d.accept("a", 300_999)); assertTrue(d.accept("a", 301_000))
    }
    @Test fun changedMessageAllowed() {
        val d = DuplicateDetector()
        assertTrue(d.accept(DuplicateDetector.fingerprint("Teams", "id", "message one"), 0))
        assertTrue(d.accept(DuplicateDetector.fingerprint("Teams", "id", "message two"), 1))
    }
    @Test fun fingerprintHasUnambiguousPartBoundaries() {
        assertNotEquals(DuplicateDetector.fingerprint("a", "bc"), DuplicateDetector.fingerprint("ab", "c"))
    }
    @Test fun boundedCacheEvictsOldest() {
        val d = DuplicateDetector(maxEntries = 2)
        d.accept("a", 0); d.accept("b", 1); d.accept("c", 2)
        assertTrue(d.accept("a", 3))
    }
    @Test fun cooldownIsSeparateAndDisabledByDefault() {
        val c = ConversationCooldown()
        assertTrue(c.accept("Teams:channel", 0, 0)); assertTrue(c.accept("Teams:channel", 0, 1))
        assertTrue(c.accept("Teams:channel", 5, 2)); assertFalse(c.accept("Teams:channel", 5, 3))
        assertTrue(c.accept("Slack:channel", 5, 3)); assertTrue(c.accept("Teams:channel", 5, 300_002))
        assertTrue(c.accept(null, 5, 4)); assertTrue(c.accept(null, 5, 5))
    }
}
