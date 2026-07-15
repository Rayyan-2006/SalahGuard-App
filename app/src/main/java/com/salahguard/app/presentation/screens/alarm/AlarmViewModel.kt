package com.salahguard.app.presentation.screens.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salahguard.app.data.service.AlarmScheduler
import com.salahguard.app.domain.model.PrayerName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
    private val userPreferencesRepository: com.salahguard.app.domain.repository.UserPreferencesRepository,
    private val updatePrayerStatusUseCase: com.salahguard.app.domain.usecase.UpdatePrayerStatusUseCase,
    private val getTodayPrayersUseCase: com.salahguard.app.domain.usecase.GetTodayPrayersUseCase
) : ViewModel() {

    fun snooze(prayerName: PrayerName, minutes: Int) {
        alarmScheduler.snoozeAlarm(prayerName, minutes)
    }

    fun markAsPrayed(prayerName: PrayerName) {
        viewModelScope.launch {
            val prayers = getTodayPrayersUseCase().first()
            val prayer = prayers.find { it.name == prayerName }
            prayer?.let {
                updatePrayerStatusUseCase(it, com.salahguard.app.domain.model.PrayerStatus.COMPLETED)
            }
        }
    }

    fun getAlarmSound() = userPreferencesRepository.getAlarmSound()
}
