package com.salahguard.app.data.local.datasource

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * One surah entry inside `assets/quran/metadata.json`.
 * This file is the source of truth for surah-level metadata: it's the only
 * one of the three bundled files that carries `englishName`, which the
 * domain [com.salahguard.app.domain.model.Surah] model requires.
 */
data class SurahMetadataDto(
    val id: Int,
    val name: String,
    val englishName: String,
    val transliteration: String,
    val verseCount: Int,
    val revelationType: String
)

/**
 * One verse inside a surah entry of `assets/quran/quran_en.json`.
 * This file conveniently carries both the Arabic `text` and the English
 * `translation` for every verse, so it's the only verse-level file we need
 * to read (the Arabic-only `quran.json` is redundant with it and is left
 * unused on purpose to avoid parsing ~1.4MB we don't need).
 */
data class QuranVerseDto(
    val id: Int,
    val text: String,
    val translation: String
)

/** One surah entry inside `assets/quran/quran_en.json`, verses included. */
data class QuranSurahDto(
    val id: Int,
    val name: String,
    val transliteration: String,
    val translation: String,
    val type: String,
    val total_verses: Int,
    val verses: List<QuranVerseDto>
)

/**
 * Reads and parses the Quran JSON files bundled under `assets/quran/`.
 *
 * This is a pure data-layer concern: it knows nothing about Room or the
 * domain model. [com.salahguard.app.data.repository.QuranRepositoryImpl]
 * is the only caller, and is responsible for turning these DTOs into Room
 * entities and caching them.
 */
class QuranAssetDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    /** Surah metadata (id, names, verse count, revelation type) for all 114 surahs. */
    suspend fun loadSurahMetadata(): List<SurahMetadataDto> = withContext(Dispatchers.IO) {
        readAsset(METADATA_FILE) { reader ->
            val type = object : TypeToken<List<SurahMetadataDto>>() {}.type
            gson.fromJson(reader, type)
        }
    }

    /** All 114 surahs with every verse's Arabic text + English translation. */
    suspend fun loadFullQuran(): List<QuranSurahDto> = withContext(Dispatchers.IO) {
        readAsset(QURAN_EN_FILE) { reader ->
            val type = object : TypeToken<List<QuranSurahDto>>() {}.type
            gson.fromJson(reader, type)
        }
    }

    private fun <T> readAsset(fileName: String, parse: (InputStreamReader) -> T): T =
        context.assets.open(fileName).use { stream ->
            InputStreamReader(stream, Charsets.UTF_8).use { reader ->
                parse(reader)
            }
        }

    companion object {
        private const val METADATA_FILE = "quran/metadata.json"
        private const val QURAN_EN_FILE = "quran/quran_en.json"
    }
}
