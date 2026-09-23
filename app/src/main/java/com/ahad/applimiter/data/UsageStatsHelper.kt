package com.ahad.applimiter.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import java.util.Calendar

object UsageStatsHelper {

    /** Returns true if the user has granted "Usage Access" for this app. */
    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /** Returns total foreground time (ms) used today, per package name, since midnight. */
    fun getTodayUsageMap(context: Context): Map<String, Long> {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val statsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )

        val result = mutableMapOf<String, Long>()
        statsList?.forEach { stats ->
            val existing = result[stats.packageName] ?: 0L
            result[stats.packageName] = existing + stats.totalTimeInForeground
        }
        return result
    }

    fun getTodayUsageMillis(context: Context, packageName: String): Long {
        return getTodayUsageMap(context)[packageName] ?: 0L
    }
}
