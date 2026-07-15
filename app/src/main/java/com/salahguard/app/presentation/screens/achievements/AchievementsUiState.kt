package com.salahguard.app.presentation.screens.achievements

import com.salahguard.app.domain.model.Achievement

data class AchievementsUiState(
    val achievements: List<Achievement> = emptyList(),
    val isLoading: Boolean = true,
    val unlockedCount: Int = 0,
    val totalCount: Int = 0,
    val newlyUnlocked: Achievement? = null
)
