package com.salahguard.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.salahguard.app.data.local.entity.AyahEntity
import com.salahguard.app.data.local.entity.SurahEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {
    @Query("SELECT * FROM surahs")
    fun getSurahs(): Flow<List<SurahEntity>>

    @Query("SELECT COUNT(*) FROM surahs")
    suspend fun getSurahCount(): Int

    @Query("SELECT * FROM ayahs WHERE surahId = :surahId ORDER BY number ASC")
    fun getAyahsForSurah(surahId: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs LIMIT 1 OFFSET :offset")
    suspend fun getAyahByOffset(offset: Int): AyahEntity?

    @Query("SELECT COUNT(*) FROM ayahs")
    suspend fun getAyahCount(): Int

    @Query("SELECT * FROM surahs WHERE id = :id")
    suspend fun getSurahById(id: Int): SurahEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<SurahEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<AyahEntity>)
}
