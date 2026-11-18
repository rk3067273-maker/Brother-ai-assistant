package com.brother.ai.core

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import com.brother.ai.termux.TermuxCommander
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Brother's command brain — maps spoken commands (bn/en) to real actions:
 * apps, system toggles, volume, web, calls, and Termux.
 */
class CommandEngine(private val context: Context) {

    data class Result(val spoken: String)

    suspend fun execute(raw: String): Result = withContext(Dispatchers.Default) {
        val cmd = raw.trim().lowercase()
        Result(route(cmd))
    }

    private fun route(cmd: String): String {
        // ── App opening (en + bn) ──
        val appMap = mapOf(
            "youtube" to "com.google.android.youtube",
            "whatsapp" to "com.whatsapp",
            "facebook" to "com.facebook.katana",
            "instagram" to "com.instagram.android",
            "chrome" to "com.android.chrome",
            "telegram" to "org.telegram.messenger",
            "settings" to "com.android.settings",
            "camera" to "com.android.camera",
            "play store" to "com.android.vending",
            "মেসেঞ্জার" to "com.facebook.orca"
        )
        for ((key, pkg) in appMap) {
            if (("open $key" in cmd) || ("$key খোলো" in cmd) || ("$key খোল" in cmd)) {
                val ok = com.brother.ai.automation.BrotherAccessibilityService
                    .openApp(context, pkg)
                return if (ok) "খুলে দিলাম $key ✔" else "$key খুলতে পারিনি"
            }
        }

        // ── Volume ──
        if ("volume up" in cmd || "ভলিউম বাড়াও" in cmd) {
            adjustVolume(AudioManager.ADJUST_RAISE)
            return "ভলিউম বাড়ালাম 🔊"
        }
        if ("volume down" in cmd || "ভলিউম কমাও" in cmd) {
            adjustVolume(AudioManager.ADJUST_LOWER)
            return "ভলিউম কমালাম 🔉"
        }

        // ── Web search ──
        if ("search" in cmd || "সার্চ কর" in cmd || "খোঁজ" in cmd) {
            val q = extractAfter(cmd, "search", "সার্চ কর")
            if (q.isNotBlank()) {
                webSearch(q)
                return "সার্চ করছি: $q"
            }
        }

        // ── Call ──
        if ("call" in cmd || "কল করো" in cmd || "কল দাও" in cmd) {
            val number = cmd.filter { it.isDigit() }
            if (number.length >= 6) {
                dial(number)
                return "ডায়াল করছি $number 📞"
            }
        }

        // ── Termux ──
        if ("termux" in cmd) {
            val script = extractAfter(cmd, "termux run", "termux")
            return if (script.isNotBlank() && TermuxCommander.runCommand(context, script)) {
                "Termux-এ চালালাম: $script"
            } else if (TermuxCommander.openTermux(context)) {
                "Termux খুলে দিলাম"
            } else {
                "Termux পাইনি — install করা আছে?"
            }
        }

        // ── Battery query (basic) ──
        if ("battery" in cmd || "ব্যাটারি" in cmd) {
            val bm = context.getSystemService(android.os.BatteryManager::class.java)
            val level = bm?.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            return if (level >= 0) "ব্যাটারি $level%" else "ব্যাটারি রিডিং পাইনি"
        }

        // ── Fallback ──
        return "বুঝতে পারিনি ভাই। আবার বলুন? (কমান্ড চাইলে dashboard scroll করুন)"
    }

    private fun adjustVolume(direction: Int) {
        val am = context.getSystemService(AudioManager::class.java)
        am?.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, 0)
    }

    private fun webSearch(query: String) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$query"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun dial(number: String) {
        context.startActivity(
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun extractAfter(cmd: String, vararg markers: String): String {
        for (m in markers) {
            val idx = cmd.indexOf(m)
            if (idx >= 0) return cmd.substring(idx + m.length).trim()
        }
        return ""
    }
}