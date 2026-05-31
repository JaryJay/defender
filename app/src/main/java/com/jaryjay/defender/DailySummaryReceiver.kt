package com.jaryjay.defender

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DailySummaryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (!DefenderPreferences.isSummaryEnabled(context)) return
        val summary = StatsTracker(context).getSummaryAndReset()
        NotificationHelper.showSummary(context, summary)
        SummaryScheduler.scheduleNext(context)
    }
}
