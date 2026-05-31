package com.jaryjay.defender

import android.content.Context
import java.time.LocalDate

class StatsTracker(context: Context) {
    private val prefs = DefenderPreferences.prefs(context)
    private val keyDate = "stats_date"
    private val keyAppBlocks = "stats_app_blocks"
    private val keySiteBlocks = "stats_site_blocks"

    fun recordAppBlock() {
        ensureDate()
        val count = prefs.getInt(keyAppBlocks, 0) + 1
        prefs.edit().putInt(keyAppBlocks, count).apply()
    }

    fun recordSiteBlock() {
        ensureDate()
        val count = prefs.getInt(keySiteBlocks, 0) + 1
        prefs.edit().putInt(keySiteBlocks, count).apply()
    }

    fun getSummaryAndReset(): Summary {
        val appCount = prefs.getInt(keyAppBlocks, 0)
        val siteCount = prefs.getInt(keySiteBlocks, 0)
        prefs.edit()
            .putInt(keyAppBlocks, 0)
            .putInt(keySiteBlocks, 0)
            .putString(keyDate, LocalDate.now().toString())
            .apply()
        return Summary(appCount, siteCount)
    }

    private fun ensureDate() {
        val today = LocalDate.now().toString()
        val stored = prefs.getString(keyDate, null)
        if (stored != today) {
            prefs.edit()
                .putString(keyDate, today)
                .putInt(keyAppBlocks, 0)
                .putInt(keySiteBlocks, 0)
                .apply()
        }
    }

    data class Summary(val appBlocks: Int, val siteBlocks: Int)
}
