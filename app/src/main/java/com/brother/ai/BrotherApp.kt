package com.brother.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class BrotherApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_VOICE,
                "Brother Voice Engine",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Keeps Brother listening for your voice" }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ALERTS,
                "Brother Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Brother responses and confirmations" }
        )
    }

    companion object {
        const val CHANNEL_VOICE = "brother_voice"
        const val CHANNEL_ALERTS = "brother_alerts"
    }
}