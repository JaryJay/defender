package com.jaryjay.defender

import android.view.accessibility.AccessibilityNodeInfo

object BrowserUrlExtractor {
    private val knownUrlIds = mapOf(
        "com.android.chrome" to listOf(
            "com.android.chrome:id/url_bar",
            "com.android.chrome:id/omnibox_text"
        ),
        "com.microsoft.emmx" to listOf(
            "com.microsoft.emmx:id/url_bar"
        ),
        "org.mozilla.firefox" to listOf(
            "org.mozilla.firefox:id/mozac_browser_toolbar_url_view"
        ),
        "com.brave.browser" to listOf(
            "com.brave.browser:id/url_bar"
        ),
        "com.opera.browser" to listOf(
            "com.opera.browser:id/url_field"
        )
    )

    fun extractUrl(root: AccessibilityNodeInfo?, packageName: String): String? {
        if (root == null) return null
        val ids = knownUrlIds[packageName]
        if (ids != null) {
            ids.forEach { id ->
                val nodes = root.findAccessibilityNodeInfosByViewId(id)
                val text = nodes.firstOrNull()?.text?.toString()
                if (!text.isNullOrBlank()) return text
            }
        }
        return traverseForUrl(root, 0)
    }

    private fun traverseForUrl(node: AccessibilityNodeInfo, depth: Int): String? {
        if (depth > 40) return null
        val text = node.text?.toString()
        if (!text.isNullOrBlank() && looksLikeUrl(text)) {
            return text
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = traverseForUrl(child, depth + 1)
            if (result != null) return result
        }
        return null
    }

    private fun looksLikeUrl(text: String): Boolean {
        val value = text.trim().lowercase()
        if (value.contains(' ') || value.length < 4) return false
        val regex = Regex("([a-z0-9-]+\\.)+[a-z]{2,}")
        return regex.containsMatchIn(value)
    }
}
