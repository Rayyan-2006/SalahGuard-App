package com.salahguard.app.domain.usecase

import com.salahguard.app.domain.repository.PrayerRepository
import kotlinx.coroutines.flow.Flow
import com.salahguard.app.domain.model.Prayer
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use cases hold single pieces of business logic, injected into ViewModels.
 * Keeping this separate from HomeViewModel means the same use case can be
 * reused by a widget, a WorkManager job, or a different screen later.
 */
class GetTodayPrayersUseCase @Inject constructor(
    private val repository: PrayerRepository
) {
    operator fun invoke(): Flow<List<Prayer>> =
        repository.getPrayersForDate(LocalDate.now())
}
