package dev.sleepguard.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.sleepguard.model.GuardSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.settingsStore by preferencesDataStore("guard_settings")
internal val storageJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }
class SettingsRepository(context: Context) {
    private val store = context.applicationContext.settingsStore
    private val key = stringPreferencesKey("settings_json")
    val settings: Flow<GuardSettings> = store.data.catch { if (it is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw it }
        .map { prefs -> decode(prefs[key]) }
    private fun decode(value: String?): GuardSettings = value?.let {
        runCatching { storageJson.decodeFromString<GuardSettings>(it) }.getOrNull()
    } ?: GuardSettings()
    suspend fun update(transform: (GuardSettings) -> GuardSettings) {
        store.edit { prefs -> prefs[key] = storageJson.encodeToString(transform(decode(prefs[key]))) }
    }
}
