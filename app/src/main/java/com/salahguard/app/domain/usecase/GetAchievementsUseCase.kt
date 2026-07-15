package com.salahguard.app.domain.usecase

import com.salahguard.app.domain.model.Achievement
import com.salahguard.app.domain.model.AchievementCategory
import com.salahguard.app.domain.model.Prayer
import com.salahguard.app.domain.model.PrayerName
import com.salahguard.app.domain.model.PrayerStatus
import com.salahguard.app.domain.model.Reflection
import com.salahguard.app.domain.repository.PrayerRepository
import com.salahguard.app.domain.repository.ReflectionRepository
import com.salahguard.app.domain.repository.UserPreferencesRepository
import com.salahguard.app.util.capitalizeFirst
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class GetAchievementsUseCase @Inject constructor(
    private val prayerRepository: PrayerRepository,
    private val reflectionRepository: ReflectionRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<List<Achievement>> {
        val today = LocalDate.now()
        val startDate = today.minusDays(365) // Look back a year

        return combine(
            prayerRepository.getPrayersForRange(startDate, today),
            reflectionRepository.getAllReflections(),
            userPreferencesRepository.getUnlockedAchievementIds()
        ) { prayers, reflections, unlockedIds ->
            calculateAchievements(prayers, reflections, unlockedIds, today)
        }
    }

    private fun calculateAchievements(
        allPrayers: List<Prayer>,
        allReflections: List<Reflection>,
        unlockedIds: Set<String>,
        today: LocalDate
    ): List<Achievement> {
        val completedPrayers = allPrayers.filter { it.status == PrayerStatus.COMPLETED && it.name != PrayerName.SUNRISE }
        val prayersByDate = completedPrayers.groupBy { it.date }
        val fullDays = prayersByDate.filter { it.value.size >= 5 }.keys.sorted()

        val achievements = mutableListOf<Achievement>()

        // 1. Prayer Consistency
        achievements.add(createAchievement(
            "first_prayer", "First Step", "Complete your first prayer.",
            AchievementCategory.PRAYER_CONSISTENCY, "🌱", completedPrayers.isNotEmpty(), 1, completedPrayers.size.coerceAtMost(1), unlockedIds
        ))

        achievements.add(createAchievement(
            "first_full_day", "Full Circle", "Complete all 5 prayers in one day.",
            AchievementCategory.PRAYER_CONSISTENCY, "✨", fullDays.isNotEmpty(), 1, fullDays.size.coerceAtMost(1), unlockedIds
        ))

        val currentStreak = calculateStreak(prayersByDate.keys.toSet(), today)
        achievements.add(createAchievement(
            "streak_3", "Consistency", "Maintain a 3-day prayer streak.",
            AchievementCategory.PRAYER_CONSISTENCY, "🔥", currentStreak >= 3, 3, currentStreak, unlockedIds
        ))

        achievements.add(createAchievement(
            "streak_7", "Steady Path", "Maintain a 7-day prayer streak.",
            AchievementCategory.PRAYER_CONSISTENCY, "🌟", currentStreak >= 7, 7, currentStreak, unlockedIds
        ))

        achievements.add(createAchievement(
            "streak_30", "Devoted", "Maintain a 30-day prayer streak.",
            AchievementCategory.PRAYER_CONSISTENCY, "💎", currentStreak >= 30, 30, currentStreak, unlockedIds
        ))

        achievements.add(createAchievement(
            "total_100", "Centurion", "Complete 100 prayers in total.",
            AchievementCategory.PRAYER_CONSISTENCY, "🏆", completedPrayers.size >= 100, 100, completedPrayers.size, unlockedIds
        ))

        // 2. Reflection Journey
        achievements.add(createAchievement(
            "first_reflection", "Inner Voice", "Write your first reflection.",
            AchievementCategory.REFLECTION_JOURNEY, "📝", allReflections.isNotEmpty(), 1, allReflections.size.coerceAtMost(1), unlockedIds
        ))

        achievements.add(createAchievement(
            "reflections_10", "Mindful", "Write 10 reflections.",
            AchievementCategory.REFLECTION_JOURNEY, "🧘", allReflections.size >= 10, 10, allReflections.size, unlockedIds
        ))

        achievements.add(createAchievement(
            "reflections_30", "Soul Searcher", "Write 30 reflections.",
            AchievementCategory.REFLECTION_JOURNEY, "📖", allReflections.size >= 30, 30, allReflections.size, unlockedIds
        ))

        // 3. Prayer Milestones
        val prayerNames = listOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA)
        prayerNames.forEach { name ->
            val count = completedPrayers.count { it.name == name }
            achievements.add(createAchievement(
                "first_${name.name.lowercase()}", "First ${name.name.capitalizeFirst()}", "Complete your first ${name.name.capitalizeFirst()} prayer.",
                AchievementCategory.PRAYER_MILESTONES, "🕋", count >= 1, 1, count.coerceAtMost(1), unlockedIds
            ))
            
            if (name == PrayerName.FAJR) {
                achievements.add(createAchievement(
                    "fajr_100", "Fajr Light", "Complete 100 Fajr prayers.",
                    AchievementCategory.PRAYER_MILESTONES, "🌅", count >= 100, 100, count, unlockedIds
                ))
            }
        }

        // 4. Growth
        achievements.add(createAchievement(
            "five_perfect", "Perfect Week", "Complete 5 consecutive full days (all 5 prayers).",
            AchievementCategory.GROWTH, "⭐", calculateMaxConsecutiveFullDays(fullDays) >= 5, 5, calculateMaxConsecutiveFullDays(fullDays), unlockedIds
        ))
        val lastWeekStart = today.minusDays(13)
        val lastWeekEnd = today.minusDays(7)
        val thisWeekStart = today.minusDays(6)
        
        val lastWeekCount = completedPrayers.count { it.date in lastWeekStart..lastWeekEnd }
        val thisWeekCount = completedPrayers.count { it.date in thisWeekStart..today }
        
        achievements.add(createAchievement(
            "improved", "Rising Higher", "Complete more prayers this week than last week.",
            AchievementCategory.GROWTH, "📈", thisWeekCount > lastWeekCount && lastWeekCount > 0, 1, if (thisWeekCount > lastWeekCount) 1 else 0, unlockedIds
        ))

        return achievements
    }

    private fun createAchievement(
        id: String, title: String, description: String,
        category: AchievementCategory, icon: String,
        isUnlocked: Boolean, targetValue: Int, currentValue: Int,
        unlockedIds: Set<String>
    ): Achievement {
        return Achievement(
            id = id, title = title, description = description,
            category = category, icon = icon,
            isUnlocked = isUnlocked || unlockedIds.contains(id),
            progress = (currentValue.toFloat() / targetValue.toFloat()).coerceIn(0f, 1f),
            targetValue = targetValue,
            currentValue = currentValue
        )
    }

    private fun calculateStreak(dates: Set<LocalDate>, today: LocalDate): Int {
        var streak = 0
        var tempDate = today
        // A streak can still be active if today isn't finished yet
        // If today has no completed prayers, check if yesterday had
        if (!dates.contains(tempDate)) {
            tempDate = tempDate.minusDays(1)
        }
        
        while (dates.contains(tempDate)) {
            streak++
            tempDate = tempDate.minusDays(1)
        }
        return streak
    }

    private fun calculateMaxConsecutiveFullDays(dates: List<LocalDate>): Int {
        if (dates.isEmpty()) return 0
        var maxConsecutive = 0
        var currentConsecutive = 0
        var expectedDate = dates.first()
        
        for (date in dates) {
            if (date == expectedDate) {
                currentConsecutive++
                expectedDate = date.plusDays(1)
            } else {
                currentConsecutive = 1
                expectedDate = date.plusDays(1)
            }
            if (currentConsecutive > maxConsecutive) maxConsecutive = currentConsecutive
        }
        return maxConsecutive
    }
}
