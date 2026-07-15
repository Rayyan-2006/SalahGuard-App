package com.salahguard.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surahs")
data class SurahEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val englishName: String,
    val transliteration: String,
    val verseCount: Int,
    val revelationType: String
)
