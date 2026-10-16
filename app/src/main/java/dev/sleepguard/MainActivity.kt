package dev.sleepguard

import android.os.Bundle
import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sleepguard.alarm.AlarmActivity
import dev.sleepguard.alarm.AlarmNotifications
import dev.sleepguard.model.*
import dev.sleepguard.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as GuardApplication
        AlarmNotifications(this).createChannel()
        setContent { SleepGuardTheme {
            val settings by app.settings.settings.collectAsStateWithLifecycle(initialValue = GuardSettings())
            val alerts by app.history.alerts.collectAsStateWithLifecycle(initialValue = emptyList())
            val active by app.alarms.active.collectAsStateWithLifecycle()
            var page by rememberSaveable { mutableStateOf("Home") }
            var refresh by remember { mutableIntStateOf(0) }
            val snackbar = remember { SnackbarHostState() }
            val uiScope = rememberCoroutineScope()
            val update: SettingsChange = { transform -> uiScope.launch {
                runCatching { app.settings.update(transform) }.onFailure { snackbar.showSnackbar("Could not save settings. Please retry.") }
            }; Unit }
            DisposableEffect(Unit) {
                val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) refresh++ }
                lifecycle.addObserver(observer)
                onDispose { lifecycle.removeObserver(observer) }
            }
            LaunchedEffect(Unit) { while (true) { delay(30_000); refresh++ } }
            val status = remember(refresh) { setupStatus(this@MainActivity) }
            val notificationRequest = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { refresh++ }
            val requestNotifications = {
                if (Build.VERSION.SDK_INT >= 33 && !status.notifications) notificationRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
                else openSettings(this@MainActivity, Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            }
            val test: (Boolean) -> Unit = { delayed ->
                if (delayed) uiScope.launch { snackbar.showSnackbar("Alarm will start in 10 seconds. Lock your phone.") }
                // Application scope deliberately survives backgrounding when testing a locked phone.
                app.scope.launch {
                    if (delayed) delay(10_000)
                    try {
                        app.alarms.test()
                        if (!delayed) startActivity(Intent(this@MainActivity, AlarmActivity::class.java))
                    } catch (_: Exception) { uiScope.launch { snackbar.showSnackbar("Could not start test alarm. Review setup.") } }
                }
            }
            val actualPage = if (page == "Home" && !settings.onboardingDone) "Welcome" else page
            BackHandler(enabled = page != "Home") { page = "Home" }
            Scaffold(topBar = { Surface {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (actualPage == "Home") "Sleep Guard" else actualPage, style = MaterialTheme.typography.headlineSmall)
                    if (page != "Home") TextButton(onClick = { page = "Home" }) { Text("Back") }
                }
            } }, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
                Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (active.isNotEmpty()) Button(onClick = { startActivity(Intent(this@MainActivity, AlarmActivity::class.java)) }) {
                        Text("Alarm active · open STOP controls")
                    }
                    when (actualPage) {
                        "Welcome" -> OnboardingScreen({ page = it }, { update { it.copy(onboardingDone = true) } }, { test(false) })
                        "Home" -> {
                            HomeScreen(settings, alerts, update, { page = it }, test, status.ready && settings.notificationDisclosureAccepted)
                            Section("Setup status") {
                                Text("Notification Access: ${status.listener}\nNotifications: ${status.notifications}\nBattery exempt: ${status.batteryExempt}\nFull-screen: ${status.fullScreen}\nDND access: ${status.dndAccess}")
                            }
                        }
                        "Schedule" -> ScheduleScreen(settings, update)
                        "Keywords" -> KeywordScreen(settings, update)
                        "Apps" -> AppSelectionScreen(settings, update)
                        "History" -> AlertHistoryScreen(alerts) { uiScope.launch {
                            runCatching { app.history.clear() }.onFailure { snackbar.showSnackbar("Could not clear history.") }
                        } }
                        "Setup" -> SetupScreen(this@MainActivity, status, requestNotifications, settings, update)
                        "Privacy" -> PrivacyScreen()
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        } }
    }
}
