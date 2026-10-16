package dev.sleepguard.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.core.net.toUri
import dev.sleepguard.BuildConfig

@Composable fun OnboardingScreen(navigate: (String) -> Unit, finish: () -> Unit, test: () -> Unit) {
    Section("Welcome to Sleep Guard") {
        Text("Sleep while staying reachable for important mentions. Sleep Guard reads selected app notification text on your phone and sounds an alarm when your keywords appear during your sleep window.")
        Text("1. Allow notification permission.\n2. Grant Notification Access and review battery / full-screen settings.\n3. Set your sleep window.\n4. Add your mention keywords.\n5. Enable Teams and Slack message previews.\n6. Test sound, vibration and the locked phone.")
        Button(onClick = { navigate("Setup") }) { Text("Set up permissions") }
        OutlinedButton(onClick = { navigate("Schedule") }) { Text("Choose sleep times") }
        OutlinedButton(onClick = { navigate("Keywords") }) { Text("Choose keywords") }
        OutlinedButton(onClick = test) { Text("Test Alarm") }
        Text("If a notification says only ‘You have a new message’, Sleep Guard cannot know the hidden message. It never signs into or scrapes your apps.")
        Button(onClick = finish) { Text("Finish setup · open dashboard") }
    }
}
@Composable fun PrivacyScreen() {
    val context = LocalContext.current
    Section("Private by design") {
        Text("Sleep Guard ${BuildConfig.VERSION_NAME} · EN corporation")
        Text("Notification text is processed entirely on this Android phone. There is no backend, internet permission, analytics, advertising, AI, cloud sync, or account sign-in.")
        Text("Only Microsoft Teams and Slack notifications selected by you are examined. Matching is deterministic and case-insensitive. Notification Access is powerful: Android grants access broadly, but Sleep Guard filters to these selected apps before extracting text.")
        Text("Up to 100 recent alarm records, including available message text and sender, are stored in private app storage. Clear them from Recent Alerts. Android backup is disabled. Uninstalling removes local data.")
        Text("Alarm details can appear on your lock screen when full-screen alarm access is allowed. Notification previews themselves hide the private message body. Opening the source app uses its original notification destination when available.")
        Text("No message bodies are written to logs. Debug builds log only generic processing results. Sleep Guard never reads Slack or Teams databases or uses Accessibility.")
        Text("Support: sakibesrat71@gmail.com")
        TextButton(onClick = { runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, "https://sakibesrat71.github.io/sleep-guard/privacy.html".toUri()))
        } }) { Text("Full privacy policy · opens browser") }
    }
}
