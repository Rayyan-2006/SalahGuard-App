package com.salahguard.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reflections")
data class ReflectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prayerName: String,
    val date: String,
    val time: String,
    val reflectionText: String,
    val mood: String?
)
