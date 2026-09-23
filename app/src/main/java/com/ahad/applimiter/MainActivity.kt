package com.ahad.applimiter

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ahad.applimiter.data.AppInfo
import com.ahad.applimiter.data.LimitPrefs
import com.ahad.applimiter.data.UsageStatsHelper
import com.ahad.applimiter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AppListAdapter
    private lateinit var limitPrefs: LimitPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        limitPrefs = LimitPrefs(this)

        adapter = AppListAdapter(emptyList()) { app ->
            showSetLimitDialog(app)
        }
        binding.appList.layoutManager = LinearLayoutManager(this)
        binding.appList.adapter = adapter

        binding.btnUsageAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        binding.btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionBanner()
        loadApps()
    }

    private fun updatePermissionBanner() {
        val hasUsage = UsageStatsHelper.hasUsageAccess(this)
        val hasAccessibility = isAccessibilityServiceEnabled()
        binding.permissionBanner.visibility =
            if (!hasUsage || !hasAccessibility) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnUsageAccess.visibility =
            if (!hasUsage) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnAccessibility.visibility =
            if (!hasAccessibility) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expected = "$packageName/${UsageAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(":").any { it.equals(expected, ignoreCase = true) }
    }

    private fun loadApps() {
        val pm = packageManager
        val usageMap = if (UsageStatsHelper.hasUsageAccess(this)) {
            UsageStatsHelper.getTodayUsageMap(this)
        } else emptyMap()

        val launcherIntent = Intent(Intent.ACTION_MAIN, null)
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolvedApps = pm.queryIntentActivities(launcherIntent, 0)

        val appInfos = resolvedApps
            .map { it.activityInfo.applicationInfo }
            .distinctBy { it.packageName }
            .filter { it.packageName != packageName }
            .map { appInfo: ApplicationInfo ->
                AppInfo(
                    packageName = appInfo.packageName,
                    label = pm.getApplicationLabel(appInfo).toString(),
                    icon = try { pm.getApplicationIcon(appInfo.packageName) } catch (e: PackageManager.NameNotFoundException) { null },
                    usedTodayMillis = usageMap[appInfo.packageName] ?: 0L,
                    limitMinutes = limitPrefs.getLimitMinutes(appInfo.packageName)
                )
            }
            .sortedByDescending { it.usedTodayMillis }

        adapter.updateData(appInfos)
    }

    private fun showSetLimitDialog(app: AppInfo) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "মিনিট (যেমন: 30)"
        if (app.limitMinutes != null) {
            input.setText(app.limitMinutes.toString())
        }

        AlertDialog.Builder(this)
            .setTitle(app.label)
            .setMessage(getString(R.string.set_limit))
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                val minutes = input.text.toString().toIntOrNull()
                if (minutes != null && minutes > 0) {
                    limitPrefs.setLimitMinutes(app.packageName, minutes)
                    loadApps()
                }
            }
            .setNeutralButton(R.string.remove_limit) { _, _ ->
                limitPrefs.removeLimit(app.packageName)
                loadApps()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
