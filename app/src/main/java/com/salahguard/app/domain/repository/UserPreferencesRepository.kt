package com.salahguard.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun isOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted()
    
    fun getLastDismissedRecoveryDate(): Flow<String?>
    suspend fun setLastDismissedRecoveryDate(date: String)

    fun getSalahGuardMode(): Flow<String>
    suspend fun setSalahGuardMode(mode: String)
    
    fun getOriginalRingerMode(): Flow<Int?>
    suspend fun setOriginalRingerMode(mode: Int?)

    fun getOriginalBrightness(): Flow<Int?>
    suspend fun setOriginalBrightness(brightness: Int?)

    fun getOriginalScreenTimeout(): Flow<Int?>
    suspend fun setOriginalScreenTimeout(timeout: Int?)

    fun getLastOpenedSurahId(): Flow<Int?>
    suspend fun setLastOpenedSurahId(id: Int)

    fun isFocusModeEnabled(): Flow<Boolean>
    suspend fun setFocusModeEnabled(enabled: Boolean)

    fun isBrightnessDimEnabled(): Flow<Boolean>
    suspend fun setBrightnessDimEnabled(enabled: Boolean)

    fun isScreenTimeoutEnabled(): Flow<Boolean>
    suspend fun setScreenTimeoutEnabled(enabled: Boolean)

    fun isNotificationsEnabled(): Flow<Boolean>
    suspend fun setNotificationsEnabled(enabled: Boolean)

    fun getReminderTimeOffset(): Flow<Int>
    suspend fun setReminderTimeOffset(minutes: Int)

    fun isPrayerNotificationEnabled(prayerName: String): Flow<Boolean>
    suspend fun setPrayerNotificationEnabled(prayerName: String, enabled: Boolean)

    fun isPrayerAlarmEnabled(prayerName: String): Flow<Boolean>
    suspend fun setPrayerAlarmEnabled(prayerName: String, enabled: Boolean)

    fun getAlarmSound(): Flow<String>
    suspend fun setAlarmSound(sound: String)

    fun isExtraFajrReminderEnabled(): Flow<Boolean>
    suspend fun setExtraFajrReminderEnabled(enabled: Boolean)

    fun getExtraFajrOffset(): Flow<Int>
    suspend fun setExtraFajrOffset(minutes: Int)

    fun getUnlockedAchievementIds(): Flow<Set<String>>
    suspend fun markAchievementUnlocked(id: String)

    fun getDailyIntentionDismissedDate(): Flow<String?>
    suspend fun setDailyIntentionDismissedDate(date: String)

    fun getDailyIntentionCompletedDate(): Flow<String?>
    suspend fun setDailyIntentionCompletedDate(date: String)

    fun getFavoriteIntentionIds(): Flow<Set<Int>>
    suspend fun toggleFavoriteIntention(id: Int)
}
