package com.salahguard.app.presentation.screens.quran

import com.salahguard.app.domain.model.Ayah
import com.salahguard.app.domain.model.Surah
import com.salahguard.app.domain.service.PlayerState

data class QuranUiState(
    val surahs: List<Surah> = emptyList(),
    val filteredSurahs: List<Surah> = emptyList(),
    val lastOpenedSurah: Surah? = null,
    val selectedSurah: Surah? = null,
    val verses: List<Ayah> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isReadingMode: Boolean = false,
    val playerState: PlayerState = PlayerState(),
    val isSurahMode: Boolean = false,
    val playingAyahId: Int? = null
)
