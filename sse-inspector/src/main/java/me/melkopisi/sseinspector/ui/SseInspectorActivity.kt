package me.melkopisi.sseinspector.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import me.melkopisi.sseinspector.data.SseInspectorRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SseInspectorActivity : ComponentActivity() {

    @Inject
    lateinit var repository: SseInspectorRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SseInspectorApp(repository = repository)
        }
    }
}
