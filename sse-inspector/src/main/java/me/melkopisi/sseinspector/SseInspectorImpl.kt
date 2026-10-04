package me.melkopisi.sseinspector

import me.melkopisi.sseinspector.data.SseInspectorRepository
import me.melkopisi.sseinspector.notification.SseNotificationManager
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SseInspectorImpl @Inject constructor(
    private val repository: SseInspectorRepository,
    private val notificationManager: SseNotificationManager
) : SseInspector {

    override fun onSessionStart(sessionId: String, request: Request) {
        val url = request.url.toString()
        val path = request.url.encodedPath
        val headers = mutableMapOf<String, String>()
        for (i in 0 until request.headers.size) {
            headers[request.headers.name(i)] = request.headers.value(i)
        }
        repository.recordSessionStart(sessionId, url, path, headers)
        notificationManager.updateStreamState(repository.getActiveStreamCount())
    }

    override fun onConnected(sessionId: String, response: Response) {
        val headers = mutableMapOf<String, String>()
        for (i in 0 until response.headers.size) {
            headers[response.headers.name(i)] = response.headers.value(i)
        }
        repository.recordConnected(sessionId, response.code, headers)
        notificationManager.updateStreamState(repository.getActiveStreamCount())
    }

    override fun onEvent(sessionId: String, id: String?, type: String?, data: String) {
        repository.recordEvent(sessionId, id, type, data)
    }

    override fun onClosed(sessionId: String) {
        repository.recordClosed(sessionId)
        notificationManager.updateStreamState(repository.getActiveStreamCount())
    }

    override fun onCancel(sessionId: String) {
        repository.recordCancel(sessionId)
        notificationManager.updateStreamState(repository.getActiveStreamCount())
    }

    override fun onFailure(
        sessionId: String,
        throwable: Throwable?,
        responseCode: Int?,
        responseBody: String?
    ) {
        repository.recordFailure(sessionId, throwable, responseCode, responseBody)
        notificationManager.updateStreamState(repository.getActiveStreamCount())
    }
}
