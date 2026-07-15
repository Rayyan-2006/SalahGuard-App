package com.salahguard.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ayahs",
    foreignKeys = [
        ForeignKey(
            entity = SurahEntity::class,
            parentColumns = ["id"],
            childColumns = ["surahId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["surahId"])]
)
data class AyahEntity(
    @PrimaryKey val id: Int,
    val number: Int,
    val text: String,
    val translation: String,
    val surahId: Int
)
