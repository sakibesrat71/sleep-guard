package dev.sleepguard.notifications

import android.app.Notification
import android.content.Context
import android.os.Bundle
import android.os.UserHandle
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class NotificationTextExtractorTest {
    private val extractor = NotificationTextExtractor()
    @Test fun standardAndSafeCustomFieldsDeduplicateText() {
        val extras = Bundle().apply {
            putCharSequence(Notification.EXTRA_TITLE, "John Smith")
            putCharSequence(Notification.EXTRA_TEXT, "Sakib Esrat test")
            putCharSequence(Notification.EXTRA_BIG_TEXT, "  Sakib  Esrat test ")
            putCharSequence(Notification.EXTRA_SUB_TEXT, "Subtext")
            putCharSequence(Notification.EXTRA_SUMMARY_TEXT, "Summary")
            putCharSequence(Notification.EXTRA_CONVERSATION_TITLE, "Production")
            putCharSequenceArray(Notification.EXTRA_TEXT_LINES, arrayOf("Line one", "Line two"))
            putString("vendor.message.preview", "Custom visible text")
            putString("vendor.internal.id", "Private identifier")
            putInt("vendor.message.counter", 4)
        }
        val fields = extractor.fields(extras)
        assertEquals(1, fields.count { it.text.contains("Sakib") })
        assertTrue(fields.any { it.text == "Line two" }); assertTrue(fields.any { it.text == "Custom visible text" })
        assertFalse(fields.any { it.text == "Private identifier" })
    }
    @Test fun missingAndUnexpectedFieldsAreSafe() {
        assertTrue(extractor.fields(Bundle()).isEmpty())
        val wrong = Bundle().apply { putInt(Notification.EXTRA_TEXT, 12); putBundle(Notification.EXTRA_BIG_TEXT, Bundle()) }
        assertTrue(extractor.fields(wrong).isEmpty())
    }
    @Test fun messagingStyleYieldsIndividualMessagesAndSenders() {
        val context: Context = RuntimeEnvironment.getApplication()
        val self = Person.Builder().setName("Me").build()
        val sender = Person.Builder().setName("John Smith").build()
        val style = NotificationCompat.MessagingStyle(self).setConversationTitle("Production").setGroupConversation(true)
            .addMessage("Sakib Esrat first", 100, sender).addMessage("Deployment complete", 200, sender)
        val n = NotificationCompat.Builder(context, "test").setSmallIcon(android.R.drawable.ic_dialog_info).setStyle(style).build()
        val sbn = StatusBarNotification("com.microsoft.teams", "com.microsoft.teams", 1, null, 1000, 0, n, UserHandle.of(0), null, 300)
        val events = extractor.extract(sbn)
        assertEquals(2, events.size); assertEquals("John Smith", events.first().sender)
        assertEquals("Production", events.first().conversation)
        assertTrue(events.first().fields.any { it.text == "Sakib Esrat first" })
        assertFalse(events.last().fields.any { it.text.contains("Sakib Esrat") })
        assertNotEquals(events.first().messageIdentity, events.last().messageIdentity)
    }
}
