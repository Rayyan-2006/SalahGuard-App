package com.salahguard.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.salahguard.app.data.local.entity.PrayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayers WHERE date = :date")
    fun getPrayersForDate(date: String): Flow<List<PrayerEntity>>

    @Query("SELECT * FROM prayers WHERE date = :date")
    suspend fun getPrayersForDateOnce(date: String): List<PrayerEntity>

    @Query("SELECT * FROM prayers WHERE date BETWEEN :startDate AND :endDate")
    fun getPrayersForRange(startDate: String, endDate: String): Flow<List<PrayerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(prayer: PrayerEntity)

    @Update
    suspend fun update(prayer: PrayerEntity)
}
