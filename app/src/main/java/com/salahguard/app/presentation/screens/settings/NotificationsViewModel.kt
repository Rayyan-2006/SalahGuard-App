package com.salahguard.app.presentation.screens.settings

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salahguard.app.data.service.AlarmScheduler
import com.salahguard.app.domain.model.PrayerName
import com.salahguard.app.domain.repository.UserPreferencesRepository
import com.salahguard.app.domain.usecase.GetTodayPrayersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class NotificationsUiState(
    val hasAlarmPermission: Boolean = false,
    val isBatteryOptimizationIgnored: Boolean = true,
    val isNotificationsEnabled: Boolean = true,
    val reminderTimeOffset: Int = 15,
    val prayerNotifications: Map<String, Boolean> = emptyMap(),
    val prayerAlarms: Map<String, Boolean> = emptyMap(),
    val alarmSound: String = "DEFAULT",
    val isExtraFajrReminderEnabled: Boolean = false,
    val extraFajrOffset: Int = 15,
    val currentPrayerName: String? = null
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val alarmScheduler: AlarmScheduler,
    private val getTodayPrayersUseCase: GetTodayPrayersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.isNotificationsEnabled().collect { enabled ->
                _uiState.update { it.copy(isNotificationsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.getReminderTimeOffset().collect { offset ->
                _uiState.update { it.copy(reminderTimeOffset = offset) }
            }
        }
        
        val prayers = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA")
        
        viewModelScope.launch {
            combine(
                prayers.map { prayer ->
                    userPreferencesRepository.isPrayerNotificationEnabled(prayer).map { prayer to it }
                }
            ) { it.toMap() }.collect { notificationsMap ->
                _uiState.update { it.copy(prayerNotifications = notificationsMap) }
            }
        }

        viewModelScope.launch {
            combine(
                prayers.map { prayer ->
                    userPreferencesRepository.isPrayerAlarmEnabled(prayer).map { prayer to it }
                }
            ) { it.toMap() }.collect { alarmsMap ->
                _uiState.update { it.copy(prayerAlarms = alarmsMap) }
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.getAlarmSound().collect { sound ->
                _uiState.update { it.copy(alarmSound = sound) }
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.isExtraFajrReminderEnabled().collect { enabled ->
                _uiState.update { it.copy(isExtraFajrReminderEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.getExtraFajrOffset().collect { offset ->
                _uiState.update { it.copy(extraFajrOffset = offset) }
            }
        }

        viewModelScope.launch {
            getTodayPrayersUseCase().collect { prayers ->
                if (prayers.isNotEmpty()) {
                    val now = LocalTime.now()
                    val actualPrayers = prayers.filter { it.name != PrayerName.SUNRISE }
                    val current = actualPrayers.lastOrNull { !it.scheduledTime.isAfter(now) }
                        ?: actualPrayers.last()
                    
                    val prayerName = current.name.name.lowercase().replaceFirstChar { it.uppercase() }
                    _uiState.update { it.copy(currentPrayerName = prayerName) }
                }
            }
        }

        checkPermission()
    }

    fun checkPermission() {
        val powerManager = alarmScheduler.getPowerManager()
        val isIgnoringBattery = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(alarmScheduler.getPackageName())
        } else {
            true
        }

        _uiState.update { 
            it.copy(
                hasAlarmPermission = alarmScheduler.canScheduleExactAlarms(),
                isBatteryOptimizationIgnored = isIgnoringBattery
            ) 
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setNotificationsEnabled(enabled)
            rescheduleAllAlarms()
        }
    }

    fun setReminderTimeOffset(minutes: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setReminderTimeOffset(minutes)
            rescheduleAllAlarms()
        }
    }

    fun togglePrayerNotification(prayerName: String, enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setPrayerNotificationEnabled(prayerName, enabled)
            rescheduleAllAlarms()
        }
    }

    fun togglePrayerAlarm(prayerName: String, enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setPrayerAlarmEnabled(prayerName, enabled)
            rescheduleAllAlarms()
        }
    }

    fun setAlarmSound(sound: String) {
        viewModelScope.launch {
            userPreferencesRepository.setAlarmSound(sound)
        }
    }

    fun toggleExtraFajrReminder(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setExtraFajrReminderEnabled(enabled)
            rescheduleAllAlarms()
        }
    }

    fun setExtraFajrOffset(minutes: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setExtraFajrOffset(minutes)
            rescheduleAllAlarms()
        }
    }

    private fun rescheduleAllAlarms() {
        viewModelScope.launch {
            val prayers = getTodayPrayersUseCase().first()
            prayers.forEach { prayer ->
                alarmScheduler.schedulePrayerReminders(prayer)
            }
        }
    }
}
