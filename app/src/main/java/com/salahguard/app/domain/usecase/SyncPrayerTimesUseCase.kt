package com.salahguard.app.domain.usecase

import com.salahguard.app.data.service.AlarmScheduler
import com.salahguard.app.domain.repository.PrayerRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class SyncPrayerTimesUseCase @Inject constructor(
    private val repository: PrayerRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(date: LocalDate, latitude: Double, longitude: Double) {
        val changed = repository.syncPrayerTimes(date, latitude, longitude)
        
        if (changed) {
            // After sync, schedule alarms for today if times changed
            val prayers = repository.getPrayersForDate(date).first()
            prayers.forEach { prayer ->
                if (prayer.status != com.salahguard.app.domain.model.PrayerStatus.COMPLETED) {
                    alarmScheduler.schedulePrayerReminders(prayer)
                }
            }
        }
    }
}
