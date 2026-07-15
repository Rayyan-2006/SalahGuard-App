package com.salahguard.app.di

import android.content.Context
import androidx.room.Room
import com.salahguard.app.data.local.SalahGuardDatabase
import com.salahguard.app.data.local.dao.PrayerDao
import com.salahguard.app.data.local.dao.QuranDao
import com.salahguard.app.data.local.dao.ReflectionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SalahGuardDatabase =
        Room.databaseBuilder(
            context,
            SalahGuardDatabase::class.java,
            "salahguard.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providePrayerDao(database: SalahGuardDatabase): PrayerDao =
        database.prayerDao()

    @Provides
    fun provideQuranDao(database: SalahGuardDatabase): QuranDao =
        database.quranDao()

    @Provides
    fun provideReflectionDao(database: SalahGuardDatabase): ReflectionDao =
        database.reflectionDao()
}
