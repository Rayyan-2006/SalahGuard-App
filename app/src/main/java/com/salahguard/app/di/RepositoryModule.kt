package com.salahguard.app.di

import com.salahguard.app.data.repository.DailyIntentionRepositoryImpl
import com.salahguard.app.data.repository.LocationRepositoryImpl
import com.salahguard.app.data.repository.PrayerRepositoryImpl
import com.salahguard.app.data.repository.QuranAudioRepositoryImpl
import com.salahguard.app.data.repository.QuranRepositoryImpl
import com.salahguard.app.data.repository.ReflectionRepositoryImpl
import com.salahguard.app.data.repository.UserPreferencesRepositoryImpl
import com.salahguard.app.domain.repository.DailyIntentionRepository
import com.salahguard.app.domain.repository.LocationRepository
import com.salahguard.app.domain.repository.PrayerRepository
import com.salahguard.app.domain.repository.QuranAudioRepository
import com.salahguard.app.domain.repository.QuranRepository
import com.salahguard.app.domain.repository.ReflectionRepository
import com.salahguard.app.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPrayerRepository(
        impl: PrayerRepositoryImpl
    ): PrayerRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindQuranRepository(
        impl: QuranRepositoryImpl
    ): QuranRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        impl: LocationRepositoryImpl
    ): LocationRepository

    @Binds
    @Singleton
    abstract fun bindReflectionRepository(
        impl: ReflectionRepositoryImpl
    ): ReflectionRepository

    @Binds
    @Singleton
    abstract fun bindQuranAudioRepository(
        impl: QuranAudioRepositoryImpl
    ): QuranAudioRepository

    @Binds
    @Singleton
    abstract fun bindDailyIntentionRepository(
        impl: DailyIntentionRepositoryImpl
    ): DailyIntentionRepository
}
