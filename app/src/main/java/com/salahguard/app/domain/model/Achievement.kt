package com.salahguard.app.domain.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val icon: String, // Can be emoji or resource ID string
    val isUnlocked: Boolean = false,
    val progress: Float = 0f, // 0.0 to 1.0
    val targetValue: Int = 1,
    val currentValue: Int = 0
)

enum class AchievementCategory {
    PRAYER_CONSISTENCY,
    REFLECTION_JOURNEY,
    PRAYER_MILESTONES,
    GROWTH
}
