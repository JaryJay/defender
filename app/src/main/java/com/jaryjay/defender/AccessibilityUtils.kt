package com.jaryjay.defender

import android.content.Context
import android.provider.Settings

object AccessibilityUtils {
    fun isAccessibilityEnabled(context: Context): Boolean {
        val enabled = runCatching {
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
        }.getOrNull() ?: 0
        if (enabled != 1) return false
        val enabledServices =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
                ?: return false
        val expectedService = "${context.packageName}/${BlockerAccessibilityService::class.java.name}"
        return enabledServices.split(':').any { it.equals(expectedService, ignoreCase = true) }
    }
}
