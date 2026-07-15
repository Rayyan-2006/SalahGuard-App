package com.salahguard.app.domain.usecase

import com.salahguard.app.domain.model.PrayerStatus
import com.salahguard.app.domain.repository.PrayerRepository
import com.salahguard.app.presentation.screens.home.RecoveryStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetRecoveryStatsUseCase @Inject constructor(
    private val repository: PrayerRepository
) {
    operator fun invoke(): Flow<RecoveryStats> {
        val today = LocalDate.now()
        val startDate = today.minusDays(90) // Check last 90 days for stats
        
        return repository.getPrayersForRange(startDate, today).map { prayers ->
            val recoveredPrayers = prayers.filter { it.status == PrayerStatus.RECOVERED }
            val totalRecovered = recoveredPrayers.size
            
            // Calculate recovery streak (consecutive days with at least one recovered or all completed)
            // Actually, the requirement says "Successful recovery streak". 
            // Let's define it as consecutive days where if a prayer was missed, it was recovered.
            val prayersByDate = prayers.groupBy { it.date }
            var recoveryStreak = 0
            var tempDate = today
            
            while (true) {
                val dayPrayers = prayersByDate[tempDate]
                if (dayPrayers == null || dayPrayers.isEmpty()) {
                    if (tempDate == today) {
                        tempDate = tempDate.minusDays(1)
                        continue
                    } else break
                }
                
                val hasMissedUnrecovered = dayPrayers.any { it.status == PrayerStatus.MISSED }
                if (!hasMissedUnrecovered) {
                    // If they had any recovered, or everything was completed/pending
                    val hasRecovered = dayPrayers.any { it.status == PrayerStatus.RECOVERED }
                    if (hasRecovered) {
                         recoveryStreak++
                    } else {
                        // If everything was completed/pending, does it count for recovery streak?
                        // "Recovery streak" usually implies they had something to recover.
                        // But maybe we just keep it simple: days without "MISSED" status.
                        // If they missed nothing, they are on a regular streak.
                        // Let's count it as part of the "hope" streak.
                        recoveryStreak++
                    }
                    tempDate = tempDate.minusDays(1)
                } else break
            }

            // Weekly recovery progress: % of missed prayers that were recovered this week
            val thisWeekStart = today.minusDays(6)
            val thisWeekPrayers = prayers.filter { it.date >= thisWeekStart }
            val missedThisWeek = thisWeekPrayers.count { it.status == PrayerStatus.MISSED }
            val recoveredThisWeek = thisWeekPrayers.count { it.status == PrayerStatus.RECOVERED }
            val totalMissedOrRecovered = missedThisWeek + recoveredThisWeek
            
            val weeklyProgress = if (totalMissedOrRecovered > 0) {
                recoveredThisWeek.toFloat() / totalMissedOrRecovered.toFloat()
            } else 1.0f

            RecoveryStats(
                totalRecovered = totalRecovered,
                recoveryStreak = recoveryStreak,
                weeklyRecoveryProgress = weeklyProgress
            )
        }
    }
}
