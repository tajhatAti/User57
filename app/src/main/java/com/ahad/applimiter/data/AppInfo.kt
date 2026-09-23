package com.ahad.applimiter.data

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val usedTodayMillis: Long,
    val limitMinutes: Int?
)
