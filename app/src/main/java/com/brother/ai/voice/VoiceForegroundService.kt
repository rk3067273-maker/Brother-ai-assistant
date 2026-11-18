package com.brother.ai.voice

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.brother.ai.BrotherApp
import com.brother.ai.MainActivity
import com.brother.ai.R

/**
 * Foreground service that keeps the Brother voice engine alive so the
 * assistant can hear wake-word and commands any time.
 */
class VoiceForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification: Notification =
            NotificationCompat.Builder(this, BrotherApp.CHANNEL_VOICE)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Brother is listening")
                .setContentText("Voice engine active — say \"Hey Brother\"")
                .setOngoing(true)
                .setContentIntent(
                    PendingIntent.getActivity(
                        this, 0,
                        Intent(this, MainActivity::class.java),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .build()
        startForeground(NOTIF_ID, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIF_ID = 1001
        @Volatile var isRunning = false
            private set
    }
}