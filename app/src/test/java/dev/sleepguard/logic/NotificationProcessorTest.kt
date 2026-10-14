package dev.sleepguard.logic

import dev.sleepguard.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalTime

class NotificationProcessorTest {
    private val settings = GuardSettings(enabled = true)
    private fun event(text: String, pkg: String = "com.microsoft.teams", identity: String? = null) = NotificationEvent(
        pkg, "notification-42", 1000, "John Smith", "Production", listOf(TextField("message", text)), identity)
    private fun process(p: NotificationProcessor, e: NotificationEvent, s: GuardSettings = settings, elapsed: Long = 1000) =
        p.process(e, s, LocalTime.of(2, 34), elapsed)
    @Test fun centralAcceptanceMentionTriggersAndUnrelatedMessageDoesNot() {
        val p = NotificationProcessor()
        val alert = process(p, event("Sakib Esrat can you please check the deployment"))!!
        assertEquals("Microsoft Teams", alert.appName); assertEquals("Sakib Esrat", alert.keyword)
        assertEquals("John Smith", alert.sender); assertEquals("Production", alert.conversation)
        assertNull(process(p, event("Deployment completed successfully")))
    }
    @Test fun disabledOutsideScheduleAndOtherAppsIgnored() {
        val e = event("Sakib Esrat")
        assertNull(process(NotificationProcessor(), e, settings.copy(enabled = false)))
        assertNull(process(NotificationProcessor(), e, settings.copy(packages = emptySet())))
        assertNull(process(NotificationProcessor(), e, settings.copy(schedule = SleepSchedule(9 * 60, 17 * 60))))
        assertNull(process(NotificationProcessor(), event("Sakib Esrat", "attacker.app"), settings.copy(packages = setOf("attacker.app"))))
    }
    @Test fun repostTimestampDoesNotDefeatDuplicateProtection() {
        val p = NotificationProcessor(); val e = event("Sakib Esrat test")
        assertNotNull(process(p, e)); assertNull(process(p, e.copy(receivedAt = 2000), elapsed = 2000))
        assertNotNull(process(p, event("Sakib Esrat new content"), elapsed = 2001))
    }
    @Test fun distinctMessagingTimestampsAllowIdenticalNewMessages() {
        val p = NotificationProcessor()
        assertNotNull(process(p, event("Sakib Esrat", identity = "100:John:Sakib Esrat")))
        assertNull(process(p, event("Sakib Esrat", identity = "100:John:Sakib Esrat")))
        assertNotNull(process(p, event("Sakib Esrat", identity = "101:John:Sakib Esrat")))
    }
    @Test fun allDayOverridesSleepRestriction() {
        assertNotNull(process(NotificationProcessor(), event("Sakib Esrat"), settings.copy(schedule = SleepSchedule(600, 700, true))))
    }
}
