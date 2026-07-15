package com.salahguard.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.salahguard.app.data.local.dao.PrayerDao
import com.salahguard.app.data.local.dao.QuranDao
import com.salahguard.app.data.local.dao.ReflectionDao
import com.salahguard.app.data.local.entity.AyahEntity
import com.salahguard.app.data.local.entity.PrayerEntity
import com.salahguard.app.data.local.entity.ReflectionEntity
import com.salahguard.app.data.local.entity.SurahEntity

@Database(
    entities = [
        PrayerEntity::class,
        SurahEntity::class,
        AyahEntity::class,
        ReflectionEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class SalahGuardDatabase : RoomDatabase() {
    abstract fun prayerDao(): PrayerDao
    abstract fun quranDao(): QuranDao
    abstract fun reflectionDao(): ReflectionDao
}
