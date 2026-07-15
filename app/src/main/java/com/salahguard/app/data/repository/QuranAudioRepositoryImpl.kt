package com.salahguard.app.data.repository

import com.salahguard.app.domain.model.Ayah
import com.salahguard.app.domain.model.Reciter
import com.salahguard.app.domain.repository.QuranAudioRepository
import javax.inject.Inject

class QuranAudioRepositoryImpl @Inject constructor() : QuranAudioRepository {
    
    private val baseUrl = "https://everyayah.com/data"

    override fun getAudioUrl(reciter: Reciter, surahId: Int, ayahNumber: Int): String {
        val s = surahId.toString().padStart(3, '0')
        val a = ayahNumber.toString().padStart(3, '0')
        return "$baseUrl/${reciter.subFolder}/$s$a.mp3"
    }

    override fun getAudioUrlsForSurah(reciter: Reciter, verses: List<Ayah>): List<String> {
        return verses.map { ayah ->
            getAudioUrl(reciter, ayah.surahId, ayah.number)
        }
    }
}
