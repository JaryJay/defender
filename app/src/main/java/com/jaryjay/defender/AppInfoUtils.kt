package com.jaryjay.defender

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

object AppInfoUtils {
    data class AppInfo(val packageName: String, val label: String, val icon: Drawable)
    data class AppDisplayInfo(val label: String, val icon: Drawable)

    fun resolveAppInfo(context: Context, packageName: String): AppDisplayInfo {
        val pm = context.packageManager
        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val label = pm.getApplicationLabel(appInfo).toString()
            val icon = pm.getApplicationIcon(appInfo)
            AppDisplayInfo(label, icon)
        } catch (ex: PackageManager.NameNotFoundException) {
            AppDisplayInfo(packageName, defaultIcon(context))
        }
    }

    fun loadLaunchableApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .asSequence()
            .filter { it.packageName != context.packageName }
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .map {
                val label = pm.getApplicationLabel(it).toString()
                val icon = pm.getApplicationIcon(it)
                AppInfo(it.packageName, label, icon)
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    private fun defaultIcon(context: Context): Drawable {
        return ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon)
            ?: throw IllegalStateException("Missing default app icon")
    }
}
