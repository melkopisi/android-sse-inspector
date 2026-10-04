package me.melkopisi.sseinspector.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.melkopisi.sseinspector.model.SseSessionRecord
import me.melkopisi.sseinspector.model.SseSessionStatus
import me.melkopisi.sseinspector.ui.theme.TerminalColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SseSessionsScreen(
    sessions: List<SseSessionRecord>,
    onSelectSession: (SseSessionRecord) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredSessions = remember(sessions, searchQuery) {
        if (searchQuery.isBlank()) {
            sessions
        } else {
            sessions.filter {
                it.url.contains(searchQuery, ignoreCase = true) ||
                    it.path.contains(searchQuery, ignoreCase = true) ||
                    it.status.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SSE Inspector",
                        fontWeight = FontWeight.Bold,
                        color = TerminalColors.TextPrimary
                    )
                },
                actions = {
                    if (sessions.isNotEmpty()) {
                        IconButton(onClick = onClearAll) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear all sessions",
                                tint = TerminalColors.TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TerminalColors.Surface
                )
            )
        },
        containerColor = TerminalColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Filter by path or status...", color = TerminalColors.TextSecondary)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TerminalColors.TextSecondary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TerminalColors.TextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (filteredSessions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (sessions.isEmpty()) "No SSE sessions recorded yet" else "No matching sessions",
                        color = TerminalColors.TextSecondary,
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredSessions, key = { it.id }) { session ->
                        SseSessionCard(session = session, onClick = { onSelectSession(session) })
                    }
                }
            }
        }
    }
}

@Composable
fun SseSessionCard(
    session: SseSessionRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (session.status) {
        SseSessionStatus.LIVE -> TerminalColors.LiveGreen
        SseSessionStatus.CONNECTING -> TerminalColors.ConnectingAmber
        SseSessionStatus.CANCELLED_BY_CLIENT -> TerminalColors.CancelledCyan
        SseSessionStatus.CLOSED_BY_SERVER -> TerminalColors.ClosedGray
        SseSessionStatus.FAILED -> TerminalColors.FailedRed
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val startTimeStr = remember(session.startTimeMs) { timeFormat.format(Date(session.startTimeMs)) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalColors.Surface)
            .border(1.dp, TerminalColors.Border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            // Row 1: Status & HTTP code & Time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = session.status.name.replace("_", " "),
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (session.httpStatusCode != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HTTP ${session.httpStatusCode}",
                            color = TerminalColors.TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = startTimeStr,
                    color = TerminalColors.TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Path
            Text(
                text = session.path.ifBlank { session.url },
                color = TerminalColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Metrics (Events count, duration)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${session.eventCount} events received",
                    color = TerminalColors.EventPurple,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                val durationStr = remember(session.startTimeMs, session.endTimeMs) {
                    val end = session.endTimeMs ?: System.currentTimeMillis()
                    val diffSec = ((end - session.startTimeMs) / MS_PER_SEC).coerceAtLeast(0)
                    val mins = diffSec / SECS_PER_MIN
                    val secs = diffSec % SECS_PER_MIN
                    String.format(Locale.US, "%02d:%02d", mins, secs)
                }
                val isLive = session.status == SseSessionStatus.LIVE
                val liveSuffix = if (isLive) " (Live)" else ""
                Text(
                    text = "Duration: $durationStr$liveSuffix",
                    color = TerminalColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

private const val MS_PER_SEC = 1_000L
private const val SECS_PER_MIN = 60
