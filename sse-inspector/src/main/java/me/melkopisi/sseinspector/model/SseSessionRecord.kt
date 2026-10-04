package me.melkopisi.sseinspector.model

enum class SseSessionStatus {
    CONNECTING,
    LIVE,
    CLOSED_BY_SERVER,
    CANCELLED_BY_CLIENT,
    FAILED
}

data class SseLogLine(
    val id: Long,
    val timestampMs: Long,
    val tag: String,
    val content: String,
    val eventId: String? = null,
    val eventType: String? = null
)

data class SseSessionRecord(
    val id: String,
    val url: String,
    val path: String,
    val method: String = "GET",
    val requestHeaders: Map<String, String> = emptyMap(),
    val startTimeMs: Long = System.currentTimeMillis(),
    var endTimeMs: Long? = null,
    var status: SseSessionStatus = SseSessionStatus.CONNECTING,
    var httpStatusCode: Int? = null,
    var failureReason: String? = null,
    var eventCount: Int = 0,
    val logs: MutableList<SseLogLine> = mutableListOf()
)
