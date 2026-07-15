package com.salahguard.app.presentation.screens.home

import com.salahguard.app.domain.model.DailyIntention

data class HomeUiState(
    val userName: String = "Rayyan",
    val currentPrayerName: String? = null,
    val currentPrayerTime: String = "",
    val nextPrayerName: String = "Asr",
    val nextPrayerTime: String = "",
    val remainingSeconds: Long = 0,
    val weeklyProgress: List<Boolean> = List(7) { false },
    val showRecoveryCard: Boolean = false,
    val recoveryMessage: String = "",
    val dailyIntention: DailyIntention? = null,
    val isIntentionDismissed: Boolean = false,
    val intentionMessage: String? = null,
    val verseArabic: String = "",
    val verseTranslation: String = "",
    val verseReference: String = "",
    val isLoading: Boolean = false,
    val locationError: String? = null,
    val recoveryStats: RecoveryStats = RecoveryStats()
)

data class RecoveryStats(
    val totalRecovered: Int = 0,
    val recoveryStreak: Int = 0,
    val weeklyRecoveryProgress: Float = 0f
)
