package dev.sleepguard.alarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sleepguard.GuardApplication
import dev.sleepguard.ui.SleepGuardTheme
import java.text.DateFormat
import java.util.Date

class AlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val controller = (application as GuardApplication).alarms
        // Activity visibility supplies a legitimate background-start exemption for fallback alerts.
        controller.resume()
        setContent {
            SleepGuardTheme {
                val alerts by controller.active.collectAsState()
                val alert = alerts.lastOrNull()
                LaunchedEffect(alert) { if (alert == null) finish() }
                BackHandler { /* Stopping is an explicit action. */ }
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.safeDrawingPadding().padding(24.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Text("SLEEP GUARD ALERT", style = MaterialTheme.typography.headlineLarge)
                        alert?.let {
                            Text(it.appName, style = MaterialTheme.typography.titleLarge)
                            it.sender?.let { name -> Text(name) }
                            it.conversation?.let { channel -> Text(channel) }
                            Text(it.text, style = MaterialTheme.typography.headlineSmall)
                            Text("Matched: ${it.keyword}\nField: ${it.matchedField}\nReceived: ${DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(it.timestamp))}")
                            if (alerts.size > 1) Text("${alerts.size} alerts in this alarm session")
                            Button(onClick = { controller.stop(); finish() }, modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                Text("STOP ALARM", style = MaterialTheme.typography.titleLarge)
                            }
                            if (!it.test) OutlinedButton(onClick = { controller.openSource(it) }, modifier = Modifier.fillMaxWidth()) {
                                Text("Open ${it.appName}")
                            }
                            Text("Open App keeps the alarm running. Press STOP ALARM to silence it.")
                        }
                    }
                }
            }
        }
    }
}
