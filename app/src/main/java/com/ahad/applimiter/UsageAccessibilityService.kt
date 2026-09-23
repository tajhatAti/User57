package com.ahad.applimiter

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.ahad.applimiter.data.LimitPrefs
import com.ahad.applimiter.data.UsageStatsHelper

/**
 * Watches which app comes to the foreground. Whenever the app switches,
 * it checks today's total usage (from UsageStatsManager) against the
 * limit the user set for that package. If the limit is exceeded, it
 * sends the device Home and shows the "Time's up" block screen.
 */
class UsageAccessibilityService : AccessibilityService() {

    private lateinit var limitPrefs: LimitPrefs

    // Avoid re-triggering the block screen over and over for the same app.
    private var lastBlockedPackage: String? = null
    private var lastBlockedAt: Long = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        limitPrefs = LimitPrefs(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == this.packageName) return

        val limitMinutes = limitPrefs.getLimitMinutes(packageName) ?: return
        val limitMillis = limitMinutes * 60_000L

        val usedMillis = UsageStatsHelper.getTodayUsageMillis(this, packageName)

        if (usedMillis >= limitMillis) {
            val now = System.currentTimeMillis()
            // Don't spam the block screen for the same app within 3 seconds.
            if (packageName == lastBlockedPackage && now - lastBlockedAt < 3000) return
            lastBlockedPackage = packageName
            lastBlockedAt = now

            performGlobalAction(GLOBAL_ACTION_HOME)

            val blockIntent = Intent(this, BlockActivity::class.java)
            blockIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            startActivity(blockIntent)
        }
    }

    override fun onInterrupt() {
        // Required override; nothing to clean up.
    }
}
