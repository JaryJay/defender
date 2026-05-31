package com.jaryjay.defender

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator

object HapticFeedback {
    fun vibrateIfEnabled(context: Context) {
        if (!DefenderPreferences.isHapticEnabled(context)) return
        val vibrator = context.getSystemService(Vibrator::class.java) ?: return
        if (!vibrator.hasVibrator()) return
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
