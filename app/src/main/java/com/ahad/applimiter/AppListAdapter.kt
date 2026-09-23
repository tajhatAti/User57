package com.ahad.applimiter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ahad.applimiter.data.AppInfo
import com.ahad.applimiter.databinding.ItemAppBinding

class AppListAdapter(
    private var apps: List<AppInfo>,
    private val onSetLimitClicked: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    inner class AppViewHolder(val binding: ItemAppBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.binding.appName.text = app.label
        holder.binding.appIcon.setImageDrawable(app.icon)

        val usedMinutes = (app.usedTodayMillis / 60000).toInt()
        holder.binding.appUsage.text = if (app.limitMinutes != null) {
            "আজ ব্যবহার: ${usedMinutes} মিনিট  •  লিমিট: ${app.limitMinutes} মিনিট"
        } else {
            "আজ ব্যবহার: ${usedMinutes} মিনিট"
        }

        holder.binding.btnSetLimit.setOnClickListener {
            onSetLimitClicked(app)
        }
    }

    override fun getItemCount(): Int = apps.size

    fun updateData(newApps: List<AppInfo>) {
        apps = newApps
        notifyDataSetChanged()
    }
}
