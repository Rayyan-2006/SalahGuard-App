package com.salahguard.app.presentation.screens.settings

import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salahguard.app.data.service.SoundModeManager
import com.salahguard.app.domain.model.PrayerName
import com.salahguard.app.domain.repository.UserPreferencesRepository
import com.salahguard.app.domain.usecase.GetTodayPrayersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class SalahGuardSettingsUiState(
    val selectedMode: String = "DISABLED",
    val hasDndPermission: Boolean = false,
    val hasWriteSettingsPermission: Boolean = false,
    val isBatteryOptimizationIgnored: Boolean = true,
    val isFocusModeEnabled: Boolean = false,
    val isBrightnessDimEnabled: Boolean = false,
    val isScreenTimeoutEnabled: Boolean = false,
    val automaticPrayers: List<String> = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA"),
    val activationsToday: Int = 3, // This would ideally come from a repository
    val currentPrayerName: String? = null
)

@HiltViewModel
class SalahGuardSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val soundModeManager: SoundModeManager,
    private val getTodayPrayersUseCase: GetTodayPrayersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalahGuardSettingsUiState())
    val uiState: StateFlow<SalahGuardSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.getSalahGuardMode().collect { mode ->
                _uiState.update { it.copy(selectedMode = mode) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.isFocusModeEnabled().collect { enabled ->
                _uiState.update { it.copy(isFocusModeEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.isBrightnessDimEnabled().collect { enabled ->
                _uiState.update { it.copy(isBrightnessDimEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.isScreenTimeoutEnabled().collect { enabled ->
                _uiState.update { it.copy(isScreenTimeoutEnabled = enabled) }
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
        // In a real app, we'd also collect the automated prayers list from preferences
        checkPermission()
    }

    fun toggleAutomaticPrayer(prayer: String) {
        _uiState.update { state ->
            val newList = if (state.automaticPrayers.contains(prayer)) {
                state.automaticPrayers.filter { it != prayer }
            } else {
                state.automaticPrayers + prayer
            }
            state.copy(automaticPrayers = newList)
        }
        // Save to repository...
    }

    fun checkPermission() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        val isIgnoringBattery = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }

        _uiState.update { 
            it.copy(
                hasDndPermission = soundModeManager.hasDndPermission(),
                hasWriteSettingsPermission = soundModeManager.canWriteSettings(),
                isBatteryOptimizationIgnored = isIgnoringBattery
            ) 
        }
    }

    fun setMode(mode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setSalahGuardMode(mode)
        }
    }

    fun toggleFocusMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setFocusModeEnabled(enabled)
        }
    }

    fun toggleBrightnessDim(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setBrightnessDimEnabled(enabled)
        }
    }

    fun toggleScreenTimeout(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setScreenTimeoutEnabled(enabled)
        }
    }
}
