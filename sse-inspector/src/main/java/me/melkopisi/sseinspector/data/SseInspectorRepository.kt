package me.melkopisi.sseinspector.data

import me.melkopisi.sseinspector.model.SseLogLine
import me.melkopisi.sseinspector.model.SseSessionRecord
import me.melkopisi.sseinspector.model.SseSessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SseInspectorRepository @Inject constructor() {

    private val logIdGenerator = AtomicLong(1L)
    private val lock = Any()

    private val _sessions = MutableStateFlow<List<SseSessionRecord>>(emptyList())
    val sessions: StateFlow<List<SseSessionRecord>> = _sessions.asStateFlow()

    fun recordSessionStart(
        sessionId: String,
        url: String,
        path: String,
        headers: Map<String, String>
    ) = synchronized(lock) {
        val session = SseSessionRecord(
            id = sessionId,
            url = url,
            path = path,
            requestHeaders = headers,
            startTimeMs = System.currentTimeMillis(),
            status = SseSessionStatus.CONNECTING
        )
        session.logs.add(
            SseLogLine(
                id = logIdGenerator.getAndIncrement(),
                timestampMs = System.currentTimeMillis(),
                tag = "INIT",
                content = "Connection initiated to: $url"
            )
        )

        val updated = mutableListOf<SseSessionRecord>()
        updated.add(session)
        updated.addAll(_sessions.value.filter { it.id != sessionId })
        if (updated.size > MAX_SESSIONS) {
            _sessions.value = updated.take(MAX_SESSIONS)
        } else {
            _sessions.value = updated
        }
    }

    fun recordConnected(sessionId: String, code: Int, headers: Map<String, String>) = synchronized(lock) {
        val current = _sessions.value.find { it.id == sessionId } ?: return@synchronized
        current.status = SseSessionStatus.LIVE
        current.httpStatusCode = code
        val headersSummary = if (headers.isNotEmpty()) {
            headers.entries.joinToString(" | ") { "${it.key}: ${it.value}" }
        } else {
            "none"
        }
        appendLog(
            current,
            "OPEN",
            "Connected (HTTP $code text/event-stream)\nResponse Headers: $headersSummary"
        )
        triggerUpdate()
    }

    fun recordEvent(sessionId: String, id: String?, type: String?, data: String) = synchronized(lock) {
        val current = _sessions.value.find { it.id == sessionId } ?: return@synchronized
        current.eventCount += 1
        val tag = "EVENT #${current.eventCount}"
        appendLog(current, tag, data, eventId = id, eventType = type)
        triggerUpdate()
    }

    fun recordClosed(sessionId: String) = synchronized(lock) {
        val current = _sessions.value.find { it.id == sessionId } ?: return@synchronized
        current.status = SseSessionStatus.CLOSED_BY_SERVER
        current.endTimeMs = System.currentTimeMillis()
        appendLog(current, "CLOSE", "Stream closed cleanly by server")
        triggerUpdate()
    }

    fun recordCancel(sessionId: String) = synchronized(lock) {
        val current = _sessions.value.find { it.id == sessionId } ?: return@synchronized
        // Only mark cancelled if not already finished
        if (current.status == SseSessionStatus.CONNECTING || current.status == SseSessionStatus.LIVE) {
            current.status = SseSessionStatus.CANCELLED_BY_CLIENT
            current.endTimeMs = System.currentTimeMillis()
            appendLog(current, "CANCEL", "Stream disconnected by client")
            triggerUpdate()
        }
    }

    fun recordFailure(
        sessionId: String,
        throwable: Throwable?,
        responseCode: Int?,
        responseBody: String?
    ) = synchronized(lock) {
        val current = _sessions.value.find { it.id == sessionId } ?: return@synchronized
        current.status = SseSessionStatus.FAILED
        current.endTimeMs = System.currentTimeMillis()
        current.httpStatusCode = responseCode
        current.failureReason = throwable?.message ?: responseBody ?: "Unknown error"
        val desc = buildString {
            append("Failure: ")
            if (responseCode != null) append("HTTP $responseCode ")
            if (throwable != null) append("(${throwable.javaClass.simpleName}: ${throwable.message}) ")
            if (!responseBody.isNullOrBlank()) append("\nBody: $responseBody")
        }
        appendLog(current, "FAIL", desc)
        triggerUpdate()
    }

    fun getSessionFlow(sessionId: String): Flow<SseSessionRecord?> =
        sessions.map { list -> list.find { it.id == sessionId } }

    fun getActiveStreamCount(): Int = synchronized(lock) {
        _sessions.value.count {
            it.status == SseSessionStatus.CONNECTING || it.status == SseSessionStatus.LIVE
        }
    }

    fun clearAll() = synchronized(lock) {
        _sessions.value = emptyList()
    }

    private fun appendLog(
        session: SseSessionRecord,
        tag: String,
        content: String,
        eventId: String? = null,
        eventType: String? = null
    ) {
        if (session.logs.size >= MAX_LOGS_PER_SESSION) {
            session.logs.removeAt(0)
        }
        session.logs.add(
            SseLogLine(
                id = logIdGenerator.getAndIncrement(),
                timestampMs = System.currentTimeMillis(),
                tag = tag,
                content = content,
                eventId = eventId,
                eventType = eventType
            )
        )
    }

    private fun triggerUpdate() {
        _sessions.value = ArrayList(_sessions.value)
    }

    companion object {
        const val MAX_SESSIONS = 25
        const val MAX_LOGS_PER_SESSION = 1_000
    }
}
