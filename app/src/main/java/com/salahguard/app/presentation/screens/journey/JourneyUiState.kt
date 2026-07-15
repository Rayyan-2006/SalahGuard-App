package com.salahguard.app.presentation.screens.journey

import java.time.LocalDate

data class JourneyUiState(
    val isLoading: Boolean = true,
    val weeklyCompletionPercentage: Int = 0,
    val monthlyCompletionPercentage: Int = 0,
    val totalCompletedPrayers: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val prayerStats: Map<String, PrayerAnalytics> = emptyMap(),
    val weeklyJourney: List<DayJourney> = emptyList(),
    val monthlyJourney: MonthlyStats = MonthlyStats(),
    val insights: List<String> = emptyList(),
    val reflectionAnalytics: ReflectionAnalytics = ReflectionAnalytics(),
    val recoveryStats: com.salahguard.app.presentation.screens.home.RecoveryStats = com.salahguard.app.presentation.screens.home.RecoveryStats()
)

data class PrayerAnalytics(
    val percentage: Int,
    val completedCount: Int,
    val missedCount: Int
)

data class DayJourney(
    val date: LocalDate,
    val completedCount: Int,
    val missedCount: Int,
    val isFullyCompleted: Boolean
)

data class MonthlyStats(
    val percentage: Int = 0,
    val totalPrayers: Int = 0,
    val completedPrayers: Int = 0,
    val missedPrayers: Int = 0,
    val bestPerformingWeek: Int = 0
)

data class ReflectionAnalytics(
    val totalReflections: Int = 0,
    val mostCommonMood: String? = null,
    val reflectionStreak: Int = 0
)
