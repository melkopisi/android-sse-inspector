package me.melkopisi.sseinspector

import okhttp3.Request
import okhttp3.Response

interface SseInspector {

    /** Called when an SSE HTTP connection request is initiated */
    fun onSessionStart(sessionId: String, request: Request)

    /**
     * Called when the HTTP 200 text/event-stream handshake succeeds.
     * NOTE: Implementations must ONLY inspect response.code and response.headers.
     * NEVER consume or read response.body.
     */
    fun onConnected(sessionId: String, response: Response)

    /** Called after the app has received and processed the event */
    fun onEvent(sessionId: String, id: String?, type: String?, data: String)

    /** Called when server closes the connection cleanly */
    fun onClosed(sessionId: String)

    /** Called when client closes / cancels the stream (e.g. coroutine scope cancelled) */
    fun onCancel(sessionId: String)

    /** Called on connection or network failure */
    fun onFailure(sessionId: String, throwable: Throwable?, responseCode: Int?, responseBody: String?)
}
