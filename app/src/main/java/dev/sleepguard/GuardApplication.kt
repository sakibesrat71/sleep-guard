package dev.sleepguard

import android.app.Application
import dev.sleepguard.data.AlertHistoryRepository
import dev.sleepguard.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class GuardApplication : Application() {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val settings by lazy { SettingsRepository(this) }
    val history by lazy { AlertHistoryRepository(this) }
}
