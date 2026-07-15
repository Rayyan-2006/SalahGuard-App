package com.salahguard.app.domain.repository

import com.salahguard.app.domain.model.Prayer
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Domain layer defines WHAT the app needs; the data layer defines HOW.
 * This is the dependency-inversion piece of Clean Architecture:
 * ViewModels/UseCases depend on this interface, never on Room directly.
 */
interface PrayerRepository {
    fun getPrayersForDate(date: LocalDate): Flow<List<Prayer>>
    fun getPrayersForRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Prayer>>
    suspend fun markCompleted(prayer: Prayer)
    suspend fun markMissed(prayer: Prayer)
    suspend fun markRecovered(prayer: Prayer)
    suspend fun markPending(prayer: Prayer)
    suspend fun syncPrayerTimes(date: LocalDate, latitude: Double, longitude: Double): Boolean
}
