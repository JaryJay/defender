package com.jaryjay.defender

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.os.SystemClock

class BlockerAccessibilityService : AccessibilityService() {
    private lateinit var repository: BlockListRepository
    private lateinit var pauseManager: PauseManager
    private lateinit var statsTracker: StatsTracker
    private var lastBackActionMs = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        repository = BlockListRepository(this)
        pauseManager = PauseManager(this)
        statsTracker = StatsTracker(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        if (!shouldHandleEvent(event, packageName)) return

        val paused = pauseManager.isPaused()
        if (repository.isAppBlocked(packageName, paused)) {
            performBlock(BlockType.APP)
            return
        }

        if (isBrowserPackage(packageName)) {
            val url = BrowserUrlExtractor.extractUrl(rootInActiveWindow, packageName)
            if (!url.isNullOrBlank() && repository.isSiteBlocked(url, paused)) {
                performBlock(BlockType.SITE)
            }
        }
    }

    override fun onInterrupt() {
        // no-op
    }

    private fun shouldHandleEvent(event: AccessibilityEvent, packageName: String): Boolean {
        if (packageName == this.packageName) return false
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return false
        }
        val now = SystemClock.elapsedRealtime()
        if (now - lastBackActionMs < 800) return false
        return true
    }

    private fun performBlock(type: BlockType) {
        val now = SystemClock.elapsedRealtime()
        lastBackActionMs = now
        HapticFeedback.vibrateIfEnabled(this)
        performGlobalAction(GLOBAL_ACTION_BACK)
        when (type) {
            BlockType.APP -> statsTracker.recordAppBlock()
            BlockType.SITE -> statsTracker.recordSiteBlock()
        }
    }

    private fun isBrowserPackage(packageName: String): Boolean {
        return packageName in setOf(
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.microsoft.emmx",
            "com.brave.browser",
            "com.opera.browser"
        )
    }

    private enum class BlockType {
        APP,
        SITE
    }
}
