package com.brother.ai.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

/**
 * Brother's hands/eyes on the phone:
 * - performs gestures (tap, swipe, scroll) via dispatchGesture
 * - reads screen content for context
 * - executes global actions (Back, Home, Recents, Notifications, Quick Settings)
 */
class BrotherAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        running = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Reserved: event stream can be used for deep app automation context later.
    }

    override fun onInterrupt() { /* no-op */ }

    override fun onDestroy() {
        running = false
        instance = null
        super.onDestroy()
    }

    fun performGlobal(action: Int): Boolean = performGlobalAction(action)

    fun tap(x: Float, y: Float, onComplete: (() -> Unit)? = null) {
        dispatchGesture(
            android.accessibilityservice.GestureDescription.Builder()
                .addStroke(
                    android.accessibilityservice.GestureDescription.StrokeDescription(
                        android.graphics.Path().apply { moveTo(x, y) }, 0L, 60L
                    )
                )
                .build(),
            callback(onComplete), null
        )
    }

    fun swipe(x1: Float, y1: Float, x2: Float, y2: Float, ms: Long = 300, onComplete: (() -> Unit)? = null) {
        dispatchGesture(
            android.accessibilityservice.GestureDescription.Builder()
                .addStroke(
                    android.accessibilityservice.GestureDescription.StrokeDescription(
                        android.graphics.Path().apply {
                            moveTo(x1, y1); lineTo(x2, y2)
                        }, 0L, ms
                    )
                )
                .build(),
            callback(onComplete), null
        )
    }

    private fun callback(done: (() -> Unit)?) =
        object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) { done?.invoke() }
            override fun onCancelled(gestureDescription: GestureDescription?) { done?.invoke() }
        }

    companion object {
        @Volatile var instance: BrotherAccessibilityService? = null
            private set
        @Volatile var running: Boolean = false
            private set

        fun openApp(context: android.content.Context, packageName: String): Boolean {
            return try {
                context.startActivity(
                    context.packageManager.getLaunchIntentForPackage(packageName)
                        ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}