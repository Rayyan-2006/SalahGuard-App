package com.salahguard.app.presentation.screens.journey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salahguard.app.domain.model.Prayer
import com.salahguard.app.domain.model.PrayerName
import com.salahguard.app.domain.model.PrayerStatus
import com.salahguard.app.domain.model.Reflection
import com.salahguard.app.domain.repository.PrayerRepository
import com.salahguard.app.domain.repository.ReflectionRepository
import com.salahguard.app.presentation.screens.home.RecoveryStats
import com.salahguard.app.util.capitalizeFirst
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class JourneyViewModel @Inject constructor(
    private val prayerRepository: PrayerRepository,
    private val reflectionRepository: ReflectionRepository,
    private val getRecoveryStatsUseCase: com.salahguard.app.domain.usecase.GetRecoveryStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(JourneyUiState())
    val uiState: StateFlow<JourneyUiState> = _uiState.asStateFlow()

    init {
        loadJourneyData()
    }

    private fun loadJourneyData() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val startDate = today.minusDays(90) // Fetch last 90 days for stats

            combine(
                prayerRepository.getPrayersForRange(startDate, today),
                reflectionRepository.getAllReflections(),
                getRecoveryStatsUseCase()
            ) { prayers, reflections, recoveryStats ->
                Triple(prayers, reflections, recoveryStats)
            }.collect { (prayers, reflections, recoveryStats) ->
                calculateStats(prayers, reflections, recoveryStats, today)
            }
        }
    }

    private fun calculateStats(
        allPrayers: List<Prayer>,
        allReflections: List<Reflection>,
        recoveryStats: RecoveryStats,
        today: LocalDate
    ) {
        val mainPrayers = allPrayers.filter { it.name != PrayerName.SUNRISE }
        val mainPrayersByDate = mainPrayers.groupBy { it.date }

        // Weekly Journey (Last 7 days)
        val weeklyJourney = (0..6).map { i ->
            val date = today.minusDays(6L - i)
            val dayPrayers = mainPrayersByDate[date] ?: emptyList()
            DayJourney(
                date = date,
                completedCount = dayPrayers.count { it.status == PrayerStatus.COMPLETED },
                missedCount = dayPrayers.count { it.status == PrayerStatus.MISSED },
                isFullyCompleted = dayPrayers.isNotEmpty() && dayPrayers.all { it.status == PrayerStatus.COMPLETED }
            )
        }

        // Monthly Stats
        val startOfMonth = today.withDayOfMonth(1)
        val monthlyPrayers = mainPrayers.filter { !it.date.isBefore(startOfMonth) }
        val monthlyCompleted = monthlyPrayers.count { it.status == PrayerStatus.COMPLETED }
        val monthlyMissed = monthlyPrayers.count { it.status == PrayerStatus.MISSED }
        val monthlyTotal = monthlyPrayers.size
        val monthlyPercentage = if (monthlyTotal > 0) (monthlyCompleted * 100) / monthlyTotal else 0

        // Best Performing Week calculation (simplified)
        val weeklyBuckets = mainPrayers.groupBy { 
            val weeksAgo = java.time.temporal.ChronoUnit.WEEKS.between(it.date, today)
            weeksAgo
        }.mapValues { entry ->
            val completed = entry.value.count { it.status == PrayerStatus.COMPLETED }
            val total = entry.value.size
            if (total > 0) (completed * 100) / total else 0
        }
        val bestWeekPercentage = weeklyBuckets.values.maxOrNull() ?: 0

        // Streaks
        var currentStreak = 0
        var tempDate = today
        while (true) {
            val dayPrayers = mainPrayersByDate[tempDate]
            if (dayPrayers != null && dayPrayers.isNotEmpty() && dayPrayers.all { it.status == PrayerStatus.COMPLETED }) {
                currentStreak++
                tempDate = tempDate.minusDays(1)
            } else {
                if (tempDate == today && (dayPrayers == null || dayPrayers.any { it.status == PrayerStatus.PENDING })) {
                    tempDate = tempDate.minusDays(1)
                    continue
                }
                break
            }
        }

        var longestStreak = 0
        var currentLongest = 0
        val allDatesSorted = mainPrayersByDate.keys.sorted()
        if (allDatesSorted.isNotEmpty()) {
            var expectedDate = allDatesSorted.first()
            for (date in allDatesSorted) {
                val dayPrayers = mainPrayersByDate[date]
                if (dayPrayers != null && dayPrayers.isNotEmpty() && dayPrayers.all { it.status == PrayerStatus.COMPLETED }) {
                    if (date == expectedDate) {
                        currentLongest++
                    } else {
                        currentLongest = 1
                    }
                    expectedDate = date.plusDays(1)
                } else {
                    currentLongest = 0
                    expectedDate = date.plusDays(1)
                }
                if (currentLongest > longestStreak) longestStreak = currentLongest
            }
        }

        // Prayer-wise Analytics
        val prayerStats = mainPrayers.groupBy { it.name }
            .mapValues { entry -> 
                val completed = entry.value.count { it.status == PrayerStatus.COMPLETED }
                val missed = entry.value.count { it.status == PrayerStatus.MISSED }
                val total = entry.value.size
                PrayerAnalytics(
                    percentage = if (total > 0) (completed * 100) / total else 0,
                    completedCount = completed,
                    missedCount = missed
                )
            }.mapKeys { it.key.name }

        // Weekly percentage
        val weeklyCompleted = weeklyJourney.sumOf { it.completedCount }
        val weeklyTotal = weeklyJourney.size * 5
        val weeklyPercentage = if (weeklyTotal > 0) (weeklyCompleted * 100) / weeklyTotal else 0

        // Reflection Analytics
        val reflectionStreak = calculateReflectionStreak(allReflections, today)
        val mostCommonMood = allReflections.filter { it.mood != null }
            .groupBy { it.mood }
            .maxByOrNull { it.value.size }?.key

        // Insights
        val insights = generateInsights(prayerStats, weeklyCompleted, mainPrayers, longestStreak, today)

        _uiState.update {
            it.copy(
                isLoading = false,
                weeklyCompletionPercentage = weeklyPercentage,
                monthlyCompletionPercentage = monthlyPercentage,
                totalCompletedPrayers = mainPrayers.count { it.status == PrayerStatus.COMPLETED },
                currentStreak = currentStreak,
                longestStreak = longestStreak,
                prayerStats = prayerStats,
                weeklyJourney = weeklyJourney,
                monthlyJourney = MonthlyStats(
                    percentage = monthlyPercentage,
                    totalPrayers = monthlyTotal,
                    completedPrayers = monthlyCompleted,
                    missedPrayers = monthlyMissed,
                    bestPerformingWeek = bestWeekPercentage
                ),
                insights = insights,
                reflectionAnalytics = ReflectionAnalytics(
                    totalReflections = allReflections.size,
                    mostCommonMood = mostCommonMood,
                    reflectionStreak = reflectionStreak
                ),
                recoveryStats = recoveryStats
            )
        }
    }

    private fun generateInsights(
        prayerStats: Map<String, PrayerAnalytics>,
        weeklyCompleted: Int,
        mainPrayers: List<Prayer>,
        longestStreak: Int,
        today: LocalDate
    ): List<String> {
        val insights = mutableListOf<String>()
        
        val mostConsistent = prayerStats.maxByOrNull { it.value.percentage }
        if (mostConsistent != null && mostConsistent.value.percentage > 0) {
            insights.add("${mostConsistent.key.capitalizeFirst()} has been your strongest prayer this week.")
        }

        val lastWeekStart = today.minusDays(13)
        val lastWeekEnd = today.minusDays(7)
        val lastWeekPrayers = mainPrayers.filter { it.date in lastWeekStart..lastWeekEnd }
        val lastWeekCompleted = lastWeekPrayers.count { it.status == PrayerStatus.COMPLETED }
        
        if (weeklyCompleted > lastWeekCompleted && lastWeekCompleted > 0) {
            insights.add("Your consistency improved compared to last week.")
        }
        
        if (weeklyCompleted > lastWeekCompleted) {
            insights.add("You've completed more prayers this week than last week.")
        }

        if (longestStreak > 0) {
            insights.add("Your longest streak is now $longestStreak days.")
        }

        if (insights.isEmpty()) {
            insights.add("Every prayer is a step closer to tranquility.")
        }
        
        return insights
    }

    private fun calculateReflectionStreak(reflections: List<Reflection>, today: LocalDate): Int {
        val reflectionDates = reflections.map { it.date }.toSet()
        var streak = 0
        var tempDate = today
        while (reflectionDates.contains(tempDate)) {
            streak++
            tempDate = tempDate.minusDays(1)
        }
        return streak
    }
}
