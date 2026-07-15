package com.salahguard.app.domain.usecase

import com.salahguard.app.domain.model.PrayerStatus
import com.salahguard.app.domain.repository.PrayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetWeeklyProgressUseCase @Inject constructor(
    private val repository: PrayerRepository
) {
    operator fun invoke(): Flow<List<Boolean>> {
        val today = LocalDate.now()
        val startDate = today.minusDays(6) // Last 7 days including today
        
        return repository.getPrayersForRange(startDate, today).map { prayers ->
            val groupedByDate = prayers.groupBy { it.date }
            
            (0..6).map { dayOffset ->
                val date = startDate.plusDays(dayOffset.toLong())
                val dayPrayers = groupedByDate[date] ?: emptyList()
                
                // Consider a day "completed" if all 5 main prayers are COMPLETED or RECOVERED
                val mainPrayers = dayPrayers.filter { it.name.name != "SUNRISE" }
                mainPrayers.isNotEmpty() && mainPrayers.all { 
                    it.status == PrayerStatus.COMPLETED || it.status == PrayerStatus.RECOVERED 
                }
            }
        }
    }
}
