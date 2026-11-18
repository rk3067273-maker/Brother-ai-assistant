package com.brother.ai.termux

import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * Sends commands to Termux via its RUN_COMMAND intent API.
 * Requires Termux:API helper (com.termux.file) — falls back gracefully.
 */
object TermuxCommander {

    private const val TERMUX_PACKAGE = "com.termux"
    private const val RUN_COMMAND_ACTION = "com.termux.RUN_COMMAND"
    private const val RUN_COMMAND_PATH = "/data/data/com.termux/files/usr/bin/bash"
    private const val RUN_COMMAND_BASE = "/data/data/com.termux/files/home"

    fun isTermuxInstalled(context: Context): Boolean =
        try {
            context.packageManager.getPackageInfo(TERMUX_PACKAGE, 0)
            true
        } catch (e: Exception) {
            false
        }

    fun runCommand(context: Context, command: String): Boolean {
        if (!isTermuxInstalled(context)) return false
        return try {
            val intent = Intent(RUN_COMMAND_ACTION).apply {
                setComponent(
                    ComponentName(
                        TERMUX_PACKAGE,
                        "com.termux.app.RunCommandService"
                    )
                )
                putExtra("com.termux.RUN_COMMAND_PATH", RUN_COMMAND_PATH)
                putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-lc", command))
                putExtra("com.termux.RUN_COMMAND_WORKDIR", RUN_COMMAND_BASE)
                putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)
            }
            context.startService(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openTermux(context: Context): Boolean =
        try {
            context.startActivity(
                context.packageManager.getLaunchIntentForPackage(TERMUX_PACKAGE)
                    ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            true
        } catch (e: Exception) {
            false
        }
}