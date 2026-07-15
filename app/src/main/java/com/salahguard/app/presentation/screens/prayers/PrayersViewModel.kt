package com.salahguard.app.presentation.screens.prayers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salahguard.app.domain.model.Prayer
import com.salahguard.app.domain.model.PrayerStatus
import com.salahguard.app.domain.model.Reflection
import com.salahguard.app.domain.repository.LocationRepository
import com.salahguard.app.domain.repository.ReflectionRepository
import com.salahguard.app.domain.sensor.CompassSensor
import com.salahguard.app.domain.usecase.GetTodayPrayersUseCase
import com.salahguard.app.domain.usecase.SyncPrayerTimesUseCase
import com.salahguard.app.domain.usecase.UpdatePrayerStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class PrayersViewModel @Inject constructor(
    private val getTodayPrayersUseCase: GetTodayPrayersUseCase,
    private val updatePrayerStatusUseCase: UpdatePrayerStatusUseCase,
    private val syncPrayerTimesUseCase: SyncPrayerTimesUseCase,
    private val locationRepository: LocationRepository,
    private val reflectionRepository: ReflectionRepository,
    private val compassSensor: CompassSensor
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrayersUiState())
    val uiState: StateFlow<PrayersUiState> = _uiState.asStateFlow()

    private var locationJob: Job? = null
    private var prayersJob: Job? = null

    init {
        loadData()
        startCompass()
    }

    private fun startCompass() {
        viewModelScope.launch {
            compassSensor.getAzimuth().collect { azimuth ->
                _uiState.update { it.copy(azimuth = azimuth) }
            }
        }
    }

    fun loadData() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Sync from location if possible
            if (locationRepository.hasLocationPermission()) {
                locationRepository.getCurrentLocation().collect { location ->
                    if (location != null) {
                        _uiState.update { it.copy(locationError = null) }
                        syncPrayerTimesUseCase(LocalDate.now(), location.first, location.second)
                    } else {
                        _uiState.update { 
                            it.copy(
                                locationError = "Unable to fetch location. Please check if GPS is enabled.",
                                isLoading = false
                            )
                        }
                    }
                }
            } else {
                _uiState.update { 
                    it.copy(
                        locationError = "Location permission is required for accurate prayer times.",
                        isLoading = false 
                    )
                }
            }
        }

        if (prayersJob != null) return
        prayersJob = viewModelScope.launch {
            getTodayPrayersUseCase().collect { prayers ->
                _uiState.update { 
                    it.copy(
                        prayers = prayers,
                        isLoading = false
                    ) 
                }
            }
        }
    }

    fun togglePrayerStatus(prayer: Prayer) {
        viewModelScope.launch {
            if (prayer.status == PrayerStatus.COMPLETED || prayer.status == PrayerStatus.RECOVERED) {
                updatePrayerStatusUseCase(prayer, PrayerStatus.PENDING)
            } else if (prayer.status == PrayerStatus.MISSED) {
                updatePrayerStatusUseCase(prayer, PrayerStatus.RECOVERED)
            } else {
                updatePrayerStatusUseCase(prayer, PrayerStatus.COMPLETED)
            }
        }
    }

    fun saveReflection(prayer: Prayer, text: String, mood: String?) {
        viewModelScope.launch {
            val reflection = Reflection(
                prayerName = prayer.name.name,
                date = LocalDate.now(),
                time = LocalTime.now(),
                reflectionText = text,
                mood = mood
            )
            reflectionRepository.saveReflection(reflection)
        }
    }
}
