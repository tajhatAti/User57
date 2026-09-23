package com.ahad.applimiter.data

import android.content.Context

/**
 * Saves each package's daily usage limit (in minutes) to SharedPreferences.
 * Format: one preference key per package name, value = minutes (Int).
 */
class LimitPrefs(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("app_limits", Context.MODE_PRIVATE)

    fun getLimitMinutes(packageName: String): Int? {
        val value = prefs.getInt(packageName, -1)
        return if (value == -1) null else value
    }

    fun setLimitMinutes(packageName: String, minutes: Int) {
        prefs.edit().putInt(packageName, minutes).apply()
    }

    fun removeLimit(packageName: String) {
        prefs.edit().remove(packageName).apply()
    }

    fun getAllLimits(): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        for ((key, value) in prefs.all) {
            if (value is Int) {
                result[key] = value
            }
        }
        return result
    }
}
