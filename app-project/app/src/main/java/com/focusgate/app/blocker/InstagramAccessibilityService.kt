package com.focusgate.app.blocker

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.focusgate.app.FocusGateApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Watches which app is in the foreground (via window-state-changed accessibility events,
 * which the system delivers the instant a new window takes focus — no polling involved) and
 * opens FocusGate's own challenge screen on top the moment Instagram becomes foreground while
 * the gate is locked.
 *
 * Android gives no API to literally prevent another app's window from drawing, so the
 * mechanism here is: detect the switch as fast as the OS tells us about it, then immediately
 * launch [BlockOverlayActivity] as a new task so it covers the screen. In practice this can
 * leave Instagram visible for a brief instant (typically well under half a second) before the
 * challenge screen takes over — see the project README for this documented limitation.
 */
class InstagramAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var lastForegroundPackage: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        if (packageName == lastForegroundPackage) return

        val previousPackage = lastForegroundPackage
        lastForegroundPackage = packageName

        when {
            packageName == INSTAGRAM_PACKAGE -> handleInstagramForeground()
            previousPackage == INSTAGRAM_PACKAGE && packageName != OWN_PACKAGE -> {
                // Left Instagram for something that isn't FocusGate's own gate screen: the
                // unlock pass is consumed, so the next Instagram open needs a fresh challenge.
                UnlockManager.consumeUnlock()
            }
        }
    }

    private fun handleInstagramForeground() {
        if (UnlockManager.isUnlocked()) return

        serviceScope.launch {
            val app = applicationContext as? FocusGateApplication ?: return@launch
            val settings = app.container.settingsDataStore.settingsFlow.first()
            if (!settings.blockingEnabled) return@launch

            val intent = Intent(this@InstagramAccessibilityService, BlockOverlayActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            runCatching { startActivity(intent) }
                .onFailure { Log.w(TAG, "Could not launch BlockOverlayActivity", it) }
        }
    }

    override fun onInterrupt() {
        // Required override; nothing to clean up.
    }

    override fun onDestroy() {
        super.onDestroy()
        lastForegroundPackage = null
    }

    companion object {
        private const val TAG = "FocusGateA11y"
        const val INSTAGRAM_PACKAGE = "com.instagram.android"
        const val OWN_PACKAGE = "com.focusgate.app"
    }
}
