package me.melkopisi.sseinspector.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.melkopisi.sseinspector.model.SseLogLine
import me.melkopisi.sseinspector.model.SseSessionRecord
import me.melkopisi.sseinspector.model.SseSessionStatus
import me.melkopisi.sseinspector.ui.theme.TerminalColors
import me.melkopisi.sseinspector.ui.util.JsonSyntaxHighlighter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SseTerminalScreen(
    session: SseSessionRecord?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var autoScrollEnabled by remember { mutableStateOf(true) }
    var filterQuery by remember { mutableStateOf("") }
    var headersExpanded by remember { mutableStateOf(false) }

    if (session == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(TerminalColors.Background),
            contentAlignment = Alignment.Center
        ) {
            Text("Session not found", color = TerminalColors.TextSecondary)
        }
        return
    }

    val statusColor = resolveStatusColor(session.status)

    val filteredLogs = remember(session.logs.size, filterQuery) {
        if (filterQuery.isBlank()) {
            session.logs.toList()
        } else {
            session.logs.filter {
                it.tag.contains(filterQuery, ignoreCase = true) ||
                    it.content.contains(filterQuery, ignoreCase = true) ||
                    it.eventType?.contains(filterQuery, ignoreCase = true) == true ||
                    it.eventId?.contains(filterQuery, ignoreCase = true) == true
            }
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(filteredLogs.size, autoScrollEnabled) {
        if (autoScrollEnabled && filteredLogs.isNotEmpty()) {
            listState.animateScrollToItem(filteredLogs.size - 1)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TerminalTopBar(
                session = session,
                statusColor = statusColor,
                onBack = onBack,
                onCopyTranscript = {
                    copyToClipboard(context, "SSE Transcript", formatTranscript(session))
                }
            )
        },
        containerColor = TerminalColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TerminalHeaderBanner(
                session = session,
                expanded = headersExpanded,
                onToggle = { headersExpanded = !headersExpanded },
                onCopyUrl = { copyToClipboard(context, "URL", session.url) }
            )

            TerminalToolbar(
                filterQuery = filterQuery,
                onFilterChange = { filterQuery = it },
                autoScrollEnabled = autoScrollEnabled,
                onToggleAutoScroll = { autoScrollEnabled = !autoScrollEnabled }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TerminalColors.Background)
                    .border(1.dp, TerminalColors.Border, RoundedCornerShape(8.dp))
            ) {
                if (filteredLogs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (session.logs.isEmpty()) "Waiting for incoming events..." else "No matching logs",
                            color = TerminalColors.TextSecondary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredLogs, key = { it.id }) { log ->
                            TerminalLogItem(
                                log = log,
                                onCopy = {
                                    val pretty = JsonSyntaxHighlighter.prettify(log.content)
                                    copyToClipboard(context, "Log [${log.tag}]", pretty)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TerminalTopBar(
    session: SseSessionRecord,
    statusColor: Color,
    onBack: () -> Unit,
    onCopyTranscript: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = session.path.ifBlank { "Live Terminal" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerminalColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${session.status.name} • ${session.eventCount} events",
                        fontSize = 11.sp,
                        color = statusColor
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TerminalColors.TextPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = onCopyTranscript) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy all",
                    tint = TerminalColors.TextSecondary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = TerminalColors.Surface
        )
    )
}

@Composable
private fun TerminalHeaderBanner(
    session: SseSessionRecord,
    expanded: Boolean,
    onToggle: () -> Unit,
    onCopyUrl: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TerminalColors.Surface)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = session.url,
                color = TerminalColors.TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 2,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onCopyUrl)
            )
            val toggleIcon = if (expanded) {
                Icons.Default.KeyboardArrowUp
            } else {
                Icons.Default.KeyboardArrowDown
            }
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = toggleIcon,
                    contentDescription = "Toggle headers",
                    tint = TerminalColors.TextSecondary
                )
            }
        }

        if (expanded && session.requestHeaders.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Request Headers:",
                color = TerminalColors.TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            session.requestHeaders.forEach { (k, v) ->
                Text(
                    text = "$k: $v",
                    color = TerminalColors.TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun TerminalToolbar(
    filterQuery: String,
    onFilterChange: (String) -> Unit,
    autoScrollEnabled: Boolean,
    onToggleAutoScroll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        BasicTextField(
            value = filterQuery,
            onValueChange = onFilterChange,
            textStyle = TextStyle(
                color = TerminalColors.TextPrimary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            ),
            cursorBrush = SolidColor(TerminalColors.CancelledCyan),
            singleLine = true,
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TerminalColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (filterQuery.isEmpty()) {
                            Text(
                                text = "Filter logs...",
                                color = TerminalColors.TextSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        innerTextField()
                    }
                    if (filterQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onFilterChange("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TerminalColors.TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TerminalColors.SurfaceVariant)
                .border(1.dp, TerminalColors.Border, RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedButton(
            onClick = onToggleAutoScroll,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (autoScrollEnabled) TerminalColors.LiveGreen else TerminalColors.TextSecondary
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp),
            modifier = Modifier.height(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VerticalAlignBottom,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (autoScrollEnabled) "Auto-Scroll: ON" else "PAUSED",
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun TerminalLogItem(
    log: SseLogLine,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tagColor = when {
        log.tag.startsWith("EVENT") -> TerminalColors.EventPurple
        log.tag == "OPEN" -> TerminalColors.LiveGreen
        log.tag == "INIT" -> TerminalColors.CancelledCyan
        log.tag == "CLOSE" -> TerminalColors.ClosedGray
        log.tag == "CANCEL" -> TerminalColors.ConnectingAmber
        log.tag == "FAIL" -> TerminalColors.FailedRed
        else -> TerminalColors.TextSecondary
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }
    val timeStr = remember(log.timestampMs) { timeFormat.format(Date(log.timestampMs)) }

    val isJson = remember(log.content) { JsonSyntaxHighlighter.isJson(log.content) }
    val formattedContent = remember(log.content, isJson) {
        if (isJson) {
            JsonSyntaxHighlighter.colorize(log.content)
        } else {
            AnnotatedString(log.content)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(TerminalColors.SurfaceVariant)
            .clickable(onClick = onCopy)
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = timeStr,
                color = TerminalColors.TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "[${log.tag}]",
                color = tagColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            if (!log.eventType.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "(${log.eventType})",
                    color = TerminalColors.CancelledCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            if (!log.eventId.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "#${log.eventId}",
                    color = TerminalColors.ConnectingAmber,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formattedContent,
            color = TerminalColors.TextPrimary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 16.sp
        )
    }
}

private fun resolveStatusColor(status: SseSessionStatus): Color {
    return when (status) {
        SseSessionStatus.LIVE -> TerminalColors.LiveGreen
        SseSessionStatus.CONNECTING -> TerminalColors.ConnectingAmber
        SseSessionStatus.CANCELLED_BY_CLIENT -> TerminalColors.CancelledCyan
        SseSessionStatus.CLOSED_BY_SERVER -> TerminalColors.ClosedGray
        SseSessionStatus.FAILED -> TerminalColors.FailedRed
    }
}

private fun formatTranscript(session: SseSessionRecord): String {
    return buildString {
        appendLine("SSE Stream: ${session.url}")
        appendLine("Status: ${session.status.name}")
        appendLine("Total Events: ${session.eventCount}")
        appendLine("--- LOGS ---")
        session.logs.forEach { log ->
            val content = JsonSyntaxHighlighter.prettify(log.content)
            appendLine("[${log.tag}] $content")
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
}
