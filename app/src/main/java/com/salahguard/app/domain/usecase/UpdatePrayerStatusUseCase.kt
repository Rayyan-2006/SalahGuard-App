package com.salahguard.app.domain.usecase

import com.salahguard.app.data.service.AlarmScheduler
import com.salahguard.app.domain.model.Prayer
import com.salahguard.app.domain.model.PrayerStatus
import com.salahguard.app.domain.repository.PrayerRepository
import javax.inject.Inject

class UpdatePrayerStatusUseCase @Inject constructor(
    private val repository: PrayerRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(prayer: Prayer, newStatus: PrayerStatus) {
        when (newStatus) {
            PrayerStatus.COMPLETED -> {
                repository.markCompleted(prayer)
                alarmScheduler.cancelReminders(prayer.name)
            }
            PrayerStatus.MISSED -> repository.markMissed(prayer)
            PrayerStatus.RECOVERED -> {
                repository.markRecovered(prayer)
                alarmScheduler.cancelReminders(prayer.name)
            }
            PrayerStatus.PENDING -> {
                repository.markPending(prayer)
                alarmScheduler.schedulePrayerReminders(prayer)
            }
        }
    }
}
