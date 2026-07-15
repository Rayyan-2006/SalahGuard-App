package com.salahguard.app.domain.repository

import com.salahguard.app.domain.model.Ayah
import com.salahguard.app.domain.model.Surah
import kotlinx.coroutines.flow.Flow

interface QuranRepository {
    fun getSurahs(): Flow<List<Surah>>
    fun getVersesForSurah(surahId: Int): Flow<List<Ayah>>
    suspend fun getAyahByOffset(offset: Int): Ayah?
    suspend fun getAyahCount(): Int
    suspend fun getSurahById(id: Int): Surah?
    suspend fun syncQuranData()
}
