package com.jaryjay.defender

import android.content.Context
import java.time.LocalDate

class PauseManager(context: Context) {
    private val prefs = DefenderPreferences.prefs(context)
    private val keyPauseUntil = "pause_until"
    private val keyPauseDate = "pause_date"

    fun isPaused(): Boolean {
        val until = prefs.getLong(keyPauseUntil, 0L)
        return System.currentTimeMillis() < until
    }

    fun remainingMinutes(): Long {
        val until = prefs.getLong(keyPauseUntil, 0L)
        val remainingMs = until - System.currentTimeMillis()
        return if (remainingMs > 0) remainingMs / 60000 else 0
    }

    fun canPause(): Boolean {
        val lastDate = prefs.getString(keyPauseDate, null)
        val today = LocalDate.now().toString()
        return lastDate != today
    }

    fun pauseFor(minutes: Int): Boolean {
        if (minutes <= 0) return false
        if (!canPause()) return false
        val until = System.currentTimeMillis() + minutes * 60_000L
        prefs.edit()
            .putLong(keyPauseUntil, until)
            .putString(keyPauseDate, LocalDate.now().toString())
            .apply()
        return true
    }
}
