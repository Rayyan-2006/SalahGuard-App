package com.salahguard.app.domain.usecase

import com.salahguard.app.domain.model.VerseOfTheDay
import com.salahguard.app.domain.repository.QuranRepository
import java.time.LocalDate
import javax.inject.Inject

class GetVerseOfTheDayUseCase @Inject constructor(
    private val repository: QuranRepository
) {
    suspend operator fun invoke(): VerseOfTheDay? {
        val count = repository.getAyahCount()
        if (count == 0) {
            repository.syncQuranData()
        }
        
        val totalCount = repository.getAyahCount()
        if (totalCount == 0) return null

        val day = LocalDate.now().toEpochDay()
        val offset = (day % totalCount).toInt()
        
        val ayah = repository.getAyahByOffset(offset) ?: return null
        val surah = repository.getSurahById(ayah.surahId)
        
        val reference = if (surah != null) {
            "${surah.englishName} ${ayah.surahId}:${ayah.number}"
        } else {
            "Quran ${ayah.surahId}:${ayah.number}"
        }

        return VerseOfTheDay(
            arabicText = ayah.text,
            translation = ayah.translation,
            reference = reference
        )
    }
}
