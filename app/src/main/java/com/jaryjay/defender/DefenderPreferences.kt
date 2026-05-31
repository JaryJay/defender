package com.jaryjay.defender

import android.content.Context

object DefenderPreferences {
    private const val PREFS_NAME = "defender_prefs"
    private const val KEY_HAPTIC_ENABLED = "haptic_enabled"
    private const val KEY_SUMMARY_ENABLED = "summary_enabled"
    private const val KEY_SUMMARY_MINUTES = "summary_minutes"
    private const val DEFAULT_SUMMARY_MINUTES = 20 * 60

    fun isHapticEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_HAPTIC_ENABLED, true)
    }

    fun setHapticEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_HAPTIC_ENABLED, enabled).apply()
    }

    fun isSummaryEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_SUMMARY_ENABLED, true)
    }

    fun setSummaryEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_SUMMARY_ENABLED, enabled).apply()
    }

    fun getSummaryMinutes(context: Context): Int {
        return prefs(context).getInt(KEY_SUMMARY_MINUTES, DEFAULT_SUMMARY_MINUTES)
    }

    fun setSummaryMinutes(context: Context, minutes: Int) {
        prefs(context).edit().putInt(KEY_SUMMARY_MINUTES, minutes).apply()
    }

    fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
