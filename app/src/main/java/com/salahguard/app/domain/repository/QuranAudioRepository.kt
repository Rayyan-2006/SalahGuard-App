package com.salahguard.app.domain.repository

import com.salahguard.app.domain.model.Ayah
import com.salahguard.app.domain.model.Reciter

interface QuranAudioRepository {
    fun getAudioUrl(reciter: Reciter, surahId: Int, ayahNumber: Int): String
    fun getAudioUrlsForSurah(reciter: Reciter, verses: List<Ayah>): List<String>
}
