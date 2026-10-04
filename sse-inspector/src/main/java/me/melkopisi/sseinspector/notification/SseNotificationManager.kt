package me.melkopisi.sseinspector.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import me.melkopisi.sseinspector.ui.SseInspectorActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SseNotificationManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SSE Inspector",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Live streaming Server-Sent Events traffic"
                setShowBadge(false)
            }
            val systemManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            systemManager?.createNotificationChannel(channel)
        }
    }

    fun updateStreamState(activeStreamCount: Int) {
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }

        if (activeStreamCount <= 0) {
            runCatching { notificationManager.cancel(NOTIFICATION_ID) }
            return
        }

        val launchIntent = Intent(context, SseInspectorActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val streamWord = if (activeStreamCount == 1) "1 stream active" else "$activeStreamCount streams active"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("SSE Inspector • $streamWord")
            .setContentText("Tap to open live streaming terminal")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()

        runCatching {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    fun cancel() {
        runCatching { notificationManager.cancel(NOTIFICATION_ID) }
    }

    companion object {
        const val CHANNEL_ID = "me_melkopisi_sse_inspector"
        const val NOTIFICATION_ID = 42_890
    }
}
