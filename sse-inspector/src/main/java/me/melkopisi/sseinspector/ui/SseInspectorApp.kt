package me.melkopisi.sseinspector.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.melkopisi.sseinspector.data.SseInspectorRepository
import me.melkopisi.sseinspector.ui.screen.SseSessionsScreen
import me.melkopisi.sseinspector.ui.screen.SseTerminalScreen
import me.melkopisi.sseinspector.ui.theme.SseTerminalTheme

@Composable
fun SseInspectorApp(
    repository: SseInspectorRepository,
    modifier: Modifier = Modifier
) {
    val sessions by repository.sessions.collectAsState()
    var selectedSessionId by remember { mutableStateOf<String?>(null) }

    SseTerminalTheme {
        Box(modifier = modifier) {
            if (selectedSessionId == null) {
                SseSessionsScreen(
                    sessions = sessions,
                    onSelectSession = { selectedSessionId = it.id },
                    onClearAll = { repository.clearAll() }
                )
            } else {
                BackHandler {
                    selectedSessionId = null
                }
                val currentSession = sessions.find { it.id == selectedSessionId }
                SseTerminalScreen(
                    session = currentSession,
                    onBack = { selectedSessionId = null }
                )
            }
        }
    }
}
