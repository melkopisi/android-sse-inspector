package com.example.sse.sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.melkopisi.sseinspector.SseInspector
import me.melkopisi.sseinspector.ui.SseInspectorActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Request
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var inspector: SseInspector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var isStreaming by remember { mutableStateOf(false) }
                    var currentSessionId by remember { mutableStateOf<String?>(null) }
                    val scope = rememberCoroutineScope()
                    var streamJob by remember { mutableStateOf<Job?>(null) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SSE Inspector Demo App",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isStreaming) "Status: LIVE STREAMING" else "Status: IDLE",
                            color = if (isStreaming) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                if (isStreaming) {
                                    streamJob?.cancel()
                                    currentSessionId?.let {
                                        inspector.onCancel(it)
                                        Timber.d("Sample: Cancelled stream session $it")
                                    }
                                    isStreaming = false
                                } else {
                                    val sessionId = UUID.randomUUID().toString()
                                    currentSessionId = sessionId
                                    isStreaming = true
                                    Timber.d("Sample: Starting stream session $sessionId")

                                    val dummyReq = Request.Builder()
                                        .url("https://api.example.com/v1/live/events")
                                        .build()

                                    inspector.onSessionStart(sessionId, dummyReq)

                                    streamJob = scope.launch {
                                        delay(300)
                                        // Emit mock connected
                                        val dummyResp = okhttp3.Response.Builder()
                                            .request(dummyReq)
                                            .protocol(okhttp3.Protocol.HTTP_1_1)
                                            .code(200)
                                            .message("OK")
                                            .header("Content-Type", "text/event-stream")
                                            .header("Cache-Control", "no-cache")
                                            .build()
                                        inspector.onConnected(sessionId, dummyResp)
                                        Timber.d("Sample: Connected stream session $sessionId")

                                        var counter = 1
                                        while (isStreaming) {
                                            delay(1500)
                                            val jsonPayload = """
                                                {"id":$counter,"action":"telemetry.metrics","total":${counter * 100},"status":"ACTIVE"}
                                            """.trimIndent()
                                            inspector.onEvent(sessionId, counter.toString(), "system.telemetry", jsonPayload)
                                            Timber.d("Sample: Emitted event #$counter to inspector")
                                            counter++
                                        }
                                    }
                                }
                            }
                        ) {
                            Text(if (isStreaming) "Stop SSE Stream" else "Start Mock SSE Stream")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val intent = Intent(this@MainActivity, SseInspectorActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                startActivity(intent)
                            }
                        ) {
                            Text("Open SSE Inspector Window")
                        }
                    }
                }
            }
        }
    }
}
