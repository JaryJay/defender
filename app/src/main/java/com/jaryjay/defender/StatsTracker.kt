package com.jaryjay.defender

import android.content.Context
import org.json.JSONObject
import java.time.LocalDate

class StatsTracker(context: Context) {
    private val prefs = DefenderPreferences.prefs(context)
    private val keyDate = "stats_date"
    private val keyAppBlocks = "stats_app_blocks"
    private val keySiteBlocks = "stats_site_blocks"
    private val keyAppCounts = "stats_app_counts"
    private val keySiteCounts = "stats_site_counts"

    fun recordAppBlock(packageName: String) {
        ensureDate()
        val count = prefs.getInt(keyAppBlocks, 0) + 1
        prefs.edit().putInt(keyAppBlocks, count).apply()
        incrementCount(keyAppCounts, packageName)
    }

    fun recordSiteBlock(host: String) {
        ensureDate()
        val count = prefs.getInt(keySiteBlocks, 0) + 1
        prefs.edit().putInt(keySiteBlocks, count).apply()
        incrementCount(keySiteCounts, host)
    }

    fun getSummaryAndReset(): Summary {
        val appCount = prefs.getInt(keyAppBlocks, 0)
        val siteCount = prefs.getInt(keySiteBlocks, 0)
        prefs.edit()
            .putInt(keyAppBlocks, 0)
            .putInt(keySiteBlocks, 0)
            .putString(keyDate, LocalDate.now().toString())
            .remove(keyAppCounts)
            .remove(keySiteCounts)
            .apply()
        return Summary(appCount, siteCount)
    }

    fun getCurrentSummary(): Summary {
        ensureDate()
        val appCount = prefs.getInt(keyAppBlocks, 0)
        val siteCount = prefs.getInt(keySiteBlocks, 0)
        return Summary(appCount, siteCount)
    }

    fun getTopAppBlocks(limit: Int): List<CountEntry> {
        return getTopCounts(keyAppCounts, limit)
    }

    fun getTopSiteBlocks(limit: Int): List<CountEntry> {
        return getTopCounts(keySiteCounts, limit)
    }

    private fun ensureDate() {
        val today = LocalDate.now().toString()
        val stored = prefs.getString(keyDate, null)
        if (stored != today) {
            prefs.edit()
                .putString(keyDate, today)
                .putInt(keyAppBlocks, 0)
                .putInt(keySiteBlocks, 0)
                .remove(keyAppCounts)
                .remove(keySiteCounts)
                .apply()
        }
    }

    private fun incrementCount(key: String, entry: String) {
        if (entry.isBlank()) return
        val counts = getCounts(key)
        val normalized = entry.lowercase()
        counts[normalized] = (counts[normalized] ?: 0) + 1
        saveCounts(key, counts)
    }

    private fun getTopCounts(key: String, limit: Int): List<CountEntry> {
        val counts = getCounts(key)
        return counts.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(limit)
            .map { CountEntry(it.key, it.value) }
    }

    private fun getCounts(key: String): MutableMap<String, Int> {
        val stored = prefs.getString(key, null) ?: return mutableMapOf()
        val obj = runCatching { JSONObject(stored) }.getOrNull() ?: return mutableMapOf()
        val map = mutableMapOf<String, Int>()
        obj.keys().forEach { entry ->
            map[entry] = obj.optInt(entry)
        }
        return map
    }

    private fun saveCounts(key: String, counts: Map<String, Int>) {
        val obj = JSONObject()
        counts.forEach { (entry, count) ->
            obj.put(entry, count)
        }
        prefs.edit().putString(key, obj.toString()).apply()
    }

    data class Summary(val appBlocks: Int, val siteBlocks: Int)
    data class CountEntry(val key: String, val count: Int)
}
