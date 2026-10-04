package me.melkopisi.sseinspector

import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SseInspectorNoOp @Inject constructor() : SseInspector {
    override fun onSessionStart(sessionId: String, request: Request) = Unit
    override fun onConnected(sessionId: String, response: Response) = Unit
    override fun onEvent(sessionId: String, id: String?, type: String?, data: String) = Unit
    override fun onClosed(sessionId: String) = Unit
    override fun onCancel(sessionId: String) = Unit
    override fun onFailure(
        sessionId: String,
        throwable: Throwable?,
        responseCode: Int?,
        responseBody: String?
    ) = Unit
}
