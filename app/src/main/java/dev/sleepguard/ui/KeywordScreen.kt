package dev.sleepguard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import dev.sleepguard.logic.normalize
import dev.sleepguard.model.*
import java.util.UUID

@Composable fun KeywordScreen(settings: GuardSettings, update: SettingsChange) {
    var input by rememberSaveable { mutableStateOf("") }
    Section("Mention keywords") {
        Text("Case-insensitive partial matches. Repeated whitespace is normalized. Short names can match inside longer words.")
        OutlinedTextField(value = input, onValueChange = { input = it.take(200) }, label = { Text("Add keyword") }, singleLine = true)
        val valid = normalize(input).isNotEmpty() && settings.keywords.none { normalize(it.text) == normalize(input) } && settings.keywords.size < 100
        Button(enabled = valid, onClick = {
            val keyword = Keyword(UUID.randomUUID().toString(), input.trim())
            update { current -> current.copy(keywords = current.keywords + keyword) }; input = ""
        }) { Text("Add keyword") }
        settings.keywords.forEach { keyword ->
            Toggle(keyword.text, keyword.enabled, { enabled -> update { current ->
                current.copy(keywords = current.keywords.map { if (it.id == keyword.id) it.copy(enabled = enabled) else it })
            } })
            TextButton(onClick = { update { current -> current.copy(keywords = current.keywords.filterNot { it.id == keyword.id }) } }) {
                Text("Delete ${keyword.text}")
            }
        }
        if (settings.keywords.isEmpty()) Text("Add at least one keyword to detect mentions.")
    }
}
