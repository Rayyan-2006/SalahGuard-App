package com.salahguard.app.data.repository

import com.salahguard.app.data.local.dao.QuranDao
import com.salahguard.app.data.local.datasource.QuranAssetDataSource
import com.salahguard.app.data.local.entity.AyahEntity
import com.salahguard.app.data.local.entity.SurahEntity
import com.salahguard.app.domain.model.Ayah
import com.salahguard.app.domain.model.Surah
import com.salahguard.app.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Room is used purely as a local cache here: the real source of truth is
 * the Quran bundled under `assets/quran/` (114 surahs, 6,236 verses -
 * the complete Mushaf). [syncQuranData] seeds Room from those assets once;
 * every read (surah list, verses for a surah, verse of the day) is served
 * straight from Room via [Flow], exactly as before.
 */
class QuranRepositoryImpl @Inject constructor(
    private val quranDao: QuranDao,
    private val assetDataSource: QuranAssetDataSource
) : QuranRepository {

    override fun getSurahs(): Flow<List<Surah>> =
        quranDao.getSurahs().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getVersesForSurah(surahId: Int): Flow<List<Ayah>> =
        quranDao.getAyahsForSurah(surahId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getAyahByOffset(offset: Int): Ayah? =
        quranDao.getAyahByOffset(offset)?.toDomain()

    override suspend fun getAyahCount(): Int =
        quranDao.getAyahCount()

    override suspend fun getSurahById(id: Int): Surah? =
        quranDao.getSurahById(id)?.toDomain()

    override suspend fun syncQuranData() {
        // Idempotent: the full Mushaf is cached once. Skip re-parsing the
        // ~2.4MB JSON asset on every app startup once it's already seeded.
        if (quranDao.getSurahCount() >= TOTAL_SURAH_COUNT) return

        val metadataById = assetDataSource.loadSurahMetadata().associateBy { it.id }
        val surahsWithVerses = assetDataSource.loadFullQuran()

        val surahEntities = surahsWithVerses.map { surahDto ->
            val meta = metadataById[surahDto.id]
            SurahEntity(
                id = surahDto.id,
                name = meta?.name ?: surahDto.name,
                englishName = meta?.englishName ?: surahDto.translation,
                transliteration = meta?.transliteration ?: surahDto.transliteration,
                verseCount = meta?.verseCount ?: surahDto.total_verses,
                revelationType = meta?.revelationType
                    ?: surahDto.type.replaceFirstChar { it.uppercase() }
            )
        }

        val ayahEntities = surahsWithVerses.flatMap { surahDto ->
            surahDto.verses.map { verseDto ->
                AyahEntity(
                    // Globally unique across the whole book: no surah has
                    // more than 286 verses, so this never collides.
                    id = surahDto.id * 1000 + verseDto.id,
                    number = verseDto.id,
                    text = verseDto.text,
                    translation = verseDto.translation,
                    surahId = surahDto.id
                )
            }
        }

        quranDao.insertSurahs(surahEntities)
        quranDao.insertAyahs(ayahEntities)
    }

    private companion object {
        /** Total surah count in the Quran; used to detect a fully-seeded cache. */
        const val TOTAL_SURAH_COUNT = 114
    }
}

private fun SurahEntity.toDomain() = Surah(
    id = id,
    name = name,
    englishName = englishName,
    transliteration = transliteration,
    verseCount = verseCount,
    revelationType = revelationType
)

private fun AyahEntity.toDomain() = Ayah(
    id = id,
    number = number,
    text = text,
    translation = translation,
    surahId = surahId
)
