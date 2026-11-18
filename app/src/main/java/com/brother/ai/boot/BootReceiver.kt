package com.brother.ai.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.brother.ai.voice.VoiceForegroundService

/** Restarts Brother's voice engine after device reboot. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ContextCompat.startForegroundService(
                context, Intent(context, VoiceForegroundService::class.java)
            )
        }
    }
}