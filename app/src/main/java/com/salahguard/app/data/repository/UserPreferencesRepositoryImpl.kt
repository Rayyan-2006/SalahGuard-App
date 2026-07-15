package com.salahguard.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.salahguard.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")
    private val lastDismissedRecoveryDateKey = stringPreferencesKey("last_dismissed_recovery_date")
    private val salahGuardModeKey = stringPreferencesKey("salahguard_mode")
    private val originalRingerModeKey = intPreferencesKey("original_ringer_mode")
    private val originalBrightnessKey = intPreferencesKey("original_brightness")
    private val originalTimeoutKey = intPreferencesKey("original_timeout")
    private val lastOpenedSurahIdKey = intPreferencesKey("last_opened_surah_id")
    private val focusModeEnabledKey = booleanPreferencesKey("focus_mode_enabled")
    private val brightnessDimEnabledKey = booleanPreferencesKey("brightness_dim_enabled")
    private val screenTimeoutEnabledKey = booleanPreferencesKey("screen_timeout_enabled")
    private val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")
    private val reminderTimeOffsetKey = intPreferencesKey("reminder_time_offset")
    private val unlockedAchievementsKey = stringSetPreferencesKey("unlocked_achievements")
    private val dailyIntentionDismissedDateKey = stringPreferencesKey("daily_intention_dismissed_date")
    private val dailyIntentionCompletedDateKey = stringPreferencesKey("daily_intention_completed_date")
    private val favoriteIntentionIdsKey = stringSetPreferencesKey("favorite_intention_ids")
    private val alarmSoundKey = stringPreferencesKey("alarm_sound")
    private val extraFajrReminderKey = booleanPreferencesKey("extra_fajr_reminder")
    private val extraFajrOffsetKey = intPreferencesKey("extra_fajr_offset")

    override fun isOnboardingCompleted(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[onboardingCompletedKey] ?: false }

    override suspend fun setOnboardingCompleted() {
        dataStore.edit { prefs -> prefs[onboardingCompletedKey] = true }
    }

    override fun getLastDismissedRecoveryDate(): Flow<String?> =
        dataStore.data.map { prefs -> prefs[lastDismissedRecoveryDateKey] }

    override suspend fun setLastDismissedRecoveryDate(date: String) {
        dataStore.edit { prefs -> prefs[lastDismissedRecoveryDateKey] = date }
    }

    override fun getSalahGuardMode(): Flow<String> =
        dataStore.data.map { prefs -> prefs[salahGuardModeKey] ?: "DISABLED" }

    override suspend fun setSalahGuardMode(mode: String) {
        dataStore.edit { prefs -> prefs[salahGuardModeKey] = mode }
    }

    override fun getOriginalRingerMode(): Flow<Int?> =
        dataStore.data.map { prefs -> prefs[originalRingerModeKey] }

    override suspend fun setOriginalRingerMode(mode: Int?) {
        dataStore.edit { prefs -> 
            if (mode == null) prefs.remove(originalRingerModeKey)
            else prefs[originalRingerModeKey] = mode 
        }
    }

    override fun getOriginalBrightness(): Flow<Int?> =
        dataStore.data.map { prefs -> prefs[originalBrightnessKey] }

    override suspend fun setOriginalBrightness(brightness: Int?) {
        dataStore.edit { prefs ->
            if (brightness == null) prefs.remove(originalBrightnessKey)
            else prefs[originalBrightnessKey] = brightness
        }
    }

    override fun getOriginalScreenTimeout(): Flow<Int?> =
        dataStore.data.map { prefs -> prefs[originalTimeoutKey] }

    override suspend fun setOriginalScreenTimeout(timeout: Int?) {
        dataStore.edit { prefs ->
            if (timeout == null) prefs.remove(originalTimeoutKey)
            else prefs[originalTimeoutKey] = timeout
        }
    }

    override fun getLastOpenedSurahId(): Flow<Int?> =
        dataStore.data.map { prefs -> prefs[lastOpenedSurahIdKey] }

    override suspend fun setLastOpenedSurahId(id: Int) {
        dataStore.edit { prefs -> prefs[lastOpenedSurahIdKey] = id }
    }

    override fun isFocusModeEnabled(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[focusModeEnabledKey] ?: false }

    override suspend fun setFocusModeEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[focusModeEnabledKey] = enabled }
    }

    override fun isBrightnessDimEnabled(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[brightnessDimEnabledKey] ?: false }

    override suspend fun setBrightnessDimEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[brightnessDimEnabledKey] = enabled }
    }

    override fun isScreenTimeoutEnabled(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[screenTimeoutEnabledKey] ?: false }

    override suspend fun setScreenTimeoutEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[screenTimeoutEnabledKey] = enabled }
    }

    override fun isNotificationsEnabled(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[notificationsEnabledKey] ?: true }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[notificationsEnabledKey] = enabled }
    }

    override fun getReminderTimeOffset(): Flow<Int> =
        dataStore.data.map { prefs -> prefs[reminderTimeOffsetKey] ?: 15 }

    override suspend fun setReminderTimeOffset(minutes: Int) {
        dataStore.edit { prefs -> prefs[reminderTimeOffsetKey] = minutes }
    }

    override fun isPrayerNotificationEnabled(prayerName: String): Flow<Boolean> =
        dataStore.data.map { prefs -> 
            prefs[booleanPreferencesKey("notification_$prayerName")] ?: true 
        }

    override suspend fun setPrayerNotificationEnabled(prayerName: String, enabled: Boolean) {
        dataStore.edit { prefs -> 
            prefs[booleanPreferencesKey("notification_$prayerName")] = enabled 
        }
    }

    override fun getUnlockedAchievementIds(): Flow<Set<String>> =
        dataStore.data.map { prefs -> prefs[unlockedAchievementsKey] ?: emptySet() }

    override suspend fun markAchievementUnlocked(id: String) {
        dataStore.edit { prefs ->
            val current = prefs[unlockedAchievementsKey] ?: emptySet()
            prefs[unlockedAchievementsKey] = current + id
        }
    }

    override fun getDailyIntentionDismissedDate(): Flow<String?> =
        dataStore.data.map { prefs -> prefs[dailyIntentionDismissedDateKey] }

    override suspend fun setDailyIntentionDismissedDate(date: String) {
        dataStore.edit { prefs -> prefs[dailyIntentionDismissedDateKey] = date }
    }

    override fun getDailyIntentionCompletedDate(): Flow<String?> =
        dataStore.data.map { prefs -> prefs[dailyIntentionCompletedDateKey] }

    override suspend fun setDailyIntentionCompletedDate(date: String) {
        dataStore.edit { prefs -> prefs[dailyIntentionCompletedDateKey] = date }
    }

    override fun getFavoriteIntentionIds(): Flow<Set<Int>> =
        dataStore.data.map { prefs -> 
            prefs[favoriteIntentionIdsKey]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet() 
        }

    override suspend fun toggleFavoriteIntention(id: Int) {
        dataStore.edit { prefs ->
            val current = prefs[favoriteIntentionIdsKey] ?: emptySet()
            val idStr = id.toString()
            if (current.contains(idStr)) {
                prefs[favoriteIntentionIdsKey] = current - idStr
            } else {
                prefs[favoriteIntentionIdsKey] = current + idStr
            }
        }
    }

    override fun isPrayerAlarmEnabled(prayerName: String): Flow<Boolean> =
        dataStore.data.map { prefs -> 
            prefs[booleanPreferencesKey("alarm_$prayerName")] ?: false 
        }

    override suspend fun setPrayerAlarmEnabled(prayerName: String, enabled: Boolean) {
        dataStore.edit { prefs -> 
            prefs[booleanPreferencesKey("alarm_$prayerName")] = enabled 
        }
    }

    override fun getAlarmSound(): Flow<String> =
        dataStore.data.map { prefs -> prefs[alarmSoundKey] ?: "DEFAULT" }

    override suspend fun setAlarmSound(sound: String) {
        dataStore.edit { prefs -> prefs[alarmSoundKey] = sound }
    }

    override fun isExtraFajrReminderEnabled(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[extraFajrReminderKey] ?: false }

    override suspend fun setExtraFajrReminderEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[extraFajrReminderKey] = enabled }
    }

    override fun getExtraFajrOffset(): Flow<Int> =
        dataStore.data.map { prefs -> prefs[extraFajrOffsetKey] ?: 15 }

    override suspend fun setExtraFajrOffset(minutes: Int) {
        dataStore.edit { prefs -> prefs[extraFajrOffsetKey] = minutes }
    }
}
