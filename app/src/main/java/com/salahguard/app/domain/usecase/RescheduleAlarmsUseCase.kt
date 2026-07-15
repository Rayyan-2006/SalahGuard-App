package com.salahguard.app.domain.usecase

import com.salahguard.app.data.service.AlarmScheduler
import com.salahguard.app.domain.repository.PrayerRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class RescheduleAlarmsUseCase @Inject constructor(
    private val prayerRepository: PrayerRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke() {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        
        listOf(today, tomorrow).forEach { date ->
            val prayers = prayerRepository.getPrayersForDate(date).first()
            prayers.forEach { prayer ->
                if (prayer.status != com.salahguard.app.domain.model.PrayerStatus.COMPLETED) {
                    alarmScheduler.schedulePrayerReminders(prayer)
                }
            }
        }
    }
}
