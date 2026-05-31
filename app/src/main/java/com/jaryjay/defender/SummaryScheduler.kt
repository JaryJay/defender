package com.jaryjay.defender

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDateTime
import java.time.ZoneId

object SummaryScheduler {
    fun scheduleNext(context: Context) {
        if (!DefenderPreferences.isSummaryEnabled(context)) {
            cancel(context)
            return
        }
        val minutes = DefenderPreferences.getSummaryMinutes(context)
        val hour = minutes / 60
        val minute = minutes % 60
        val now = LocalDateTime.now()
        var scheduled = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!scheduled.isAfter(now)) {
            scheduled = scheduled.plusDays(1)
        }
        val triggerAt = scheduled.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent(context))
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, DailySummaryReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            1002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
