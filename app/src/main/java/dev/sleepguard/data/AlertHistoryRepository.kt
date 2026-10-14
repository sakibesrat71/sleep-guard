package dev.sleepguard.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.sleepguard.model.AlertTrigger
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.historyStore by preferencesDataStore("alert_history")
/** Bounded to 100 records; never shared or backed up. Writes serialized by DataStore. */
class AlertHistoryRepository(context: Context) {
    private val store = context.applicationContext.historyStore
    private val key = stringPreferencesKey("alerts_json")
    private fun decode(value: String?): List<AlertTrigger> = value?.let {
        runCatching { storageJson.decodeFromString<List<AlertTrigger>>(it) }.getOrNull()
    } ?: emptyList()
    val alerts = store.data.catch { if (it is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw it }
        .map { decode(it[key]) }
    suspend fun add(alert: AlertTrigger) = store.edit {
        it[key] = storageJson.encodeToString((listOf(alert) + decode(it[key])).take(100))
    }
    suspend fun dismiss(ids: Set<String>) = store.edit {
        it[key] = storageJson.encodeToString(decode(it[key]).map { alert -> if (alert.id in ids) alert.copy(dismissed = true) else alert })
    }
    suspend fun clear() { store.edit { it.remove(key) } }
}
