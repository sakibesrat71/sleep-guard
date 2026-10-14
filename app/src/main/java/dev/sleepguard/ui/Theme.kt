package dev.sleepguard.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SleepGuardTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Color(0xFF85DBC8), onPrimary = Color(0xFF00382E),
        background = Color(0xFF101820), surface = Color(0xFF17242D),
        surfaceVariant = Color(0xFF263A43)
    ), content = content)
}
