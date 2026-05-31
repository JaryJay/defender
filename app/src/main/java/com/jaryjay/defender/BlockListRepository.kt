package com.jaryjay.defender

import android.content.Context

class BlockListRepository(context: Context) {
    private val prefs = DefenderPreferences.prefs(context)

    private val keyAlwaysApps = "always_blocked_apps"
    private val keyBlockedApps = "blocked_apps"
    private val keyAlwaysSites = "always_blocked_sites"
    private val keyBlockedSites = "blocked_sites"

    fun getAlwaysBlockedApps(): List<String> = getSet(keyAlwaysApps)
    fun getBlockedApps(): List<String> = getSet(keyBlockedApps)
    fun getAlwaysBlockedSites(): List<String> = getSet(keyAlwaysSites)
    fun getBlockedSites(): List<String> = getSet(keyBlockedSites)

    fun addAlwaysBlockedApp(value: String) = addToSet(keyAlwaysApps, normalizeApp(value))
    fun addBlockedApp(value: String) = addToSet(keyBlockedApps, normalizeApp(value))
    fun addAlwaysBlockedSite(value: String) = addToSet(keyAlwaysSites, normalizeSite(value))
    fun addBlockedSite(value: String) = addToSet(keyBlockedSites, normalizeSite(value))

    fun removeAlwaysBlockedApp(value: String) = removeFromSet(keyAlwaysApps, normalizeApp(value))
    fun removeBlockedApp(value: String) = removeFromSet(keyBlockedApps, normalizeApp(value))
    fun removeAlwaysBlockedSite(value: String) = removeFromSet(keyAlwaysSites, normalizeSite(value))
    fun removeBlockedSite(value: String) = removeFromSet(keyBlockedSites, normalizeSite(value))

    fun isAppBlocked(packageName: String, paused: Boolean): Boolean {
        val normalized = normalizeApp(packageName)
        if (getAlwaysBlockedApps().contains(normalized)) return true
        if (paused) return false
        return getBlockedApps().contains(normalized)
    }

    fun isSiteBlocked(urlOrHost: String, paused: Boolean): Boolean {
        val host = WildcardMatcher.normalizeHost(urlOrHost) ?: return false
        if (WildcardMatcher.matchesAny(getAlwaysBlockedSites(), host)) return true
        if (paused) return false
        return WildcardMatcher.matchesAny(getBlockedSites(), host)
    }

    private fun getSet(key: String): List<String> {
        val set = prefs.getStringSet(key, emptySet()) ?: emptySet()
        return set.toList().sorted()
    }

    private fun addToSet(key: String, value: String) {
        if (value.isBlank()) return
        val set = (prefs.getStringSet(key, emptySet()) ?: emptySet()).toMutableSet()
        set.add(value)
        prefs.edit().putStringSet(key, set).apply()
    }

    private fun removeFromSet(key: String, value: String) {
        val set = (prefs.getStringSet(key, emptySet()) ?: emptySet()).toMutableSet()
        set.remove(value)
        prefs.edit().putStringSet(key, set).apply()
    }

    private fun normalizeApp(value: String): String = value.trim().lowercase()
    private fun normalizeSite(value: String): String = value.trim().lowercase()
}
