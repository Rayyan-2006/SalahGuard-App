package com.salahguard.app.presentation.screens.tools.mosques

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salahguard.app.domain.model.Mosque
import com.salahguard.app.domain.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MosqueFinderUiState(
    val mosques: List<Mosque> = emptyList(),
    val userLocation: Pair<Double, Double>? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class MosqueFinderViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MosqueFinderUiState())
    val uiState: StateFlow<MosqueFinderUiState> = _uiState.asStateFlow()

    init {
        loadNearbyMosques()
    }

    private fun loadNearbyMosques() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            locationRepository.getCurrentLocation().collect { location ->
                if (location != null) {
                    // In a real app, this would call an API like Google Places.
                    // For this sprint, we provide mock data for the UI implementation.
                    val mockMosques = listOf(
                        Mosque("Masjid Al Noor", 1.4, 8, location.first + 0.01, location.second + 0.01),
                        Mosque("Islamic Center", 2.8, 12, location.first - 0.015, location.second + 0.02),
                        Mosque("Grand Masjid", 4.2, 18, location.first + 0.03, location.second - 0.01),
                        Mosque("Masjid Umar", 5.5, 22, location.first - 0.02, location.second - 0.025)
                    )
                    _uiState.update { it.copy(
                        userLocation = location,
                        mosques = mockMosques,
                        isLoading = false
                    ) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }
}
