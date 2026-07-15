package com.salahguard.app.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "prayers",
    primaryKeys = ["name", "date"]
)
data class PrayerEntity(
    val name: String,        // stores PrayerName enum as string
    val date: String,        // ISO date string, e.g. "2026-07-08"
    val scheduledTime: String, // ISO time string, e.g. "05:12"
    val status: String,      // stores PrayerStatus enum as string
    val completedAt: String? = null
)
