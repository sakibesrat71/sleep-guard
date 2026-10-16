package dev.sleepguard.logic

import dev.sleepguard.model.GuardSettings
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class SettingsDefaultsTest {
    @Test fun newInstallsRequireOwnKeywordsAndExplicitConsent() {
        val settings = GuardSettings()
        assertFalse(settings.enabled)
        assertFalse(settings.notificationDisclosureAccepted)
        assertTrue(settings.keywords.isEmpty())
    }
    @Test fun upgradeRetainsExistingKeywordsButRequiresNewConsent() {
        val settings = Json.decodeFromString<GuardSettings>("""{"enabled":true,"keywords":[{"id":"1","text":"My Name"}],"onboardingDone":true}""")
        assertEquals("My Name", settings.keywords.single().text)
        assertTrue(settings.enabled)
        assertFalse(settings.notificationDisclosureAccepted)
    }
}
