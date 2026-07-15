package com.salahguard.app.domain.model

data class Surah(
    val id: Int,
    val name: String,
    val englishName: String,
    val transliteration: String,
    val verseCount: Int,
    val revelationType: String
)

data class Ayah(
    val id: Int,
    val number: Int,
    val text: String,
    val translation: String,
    val surahId: Int
)
