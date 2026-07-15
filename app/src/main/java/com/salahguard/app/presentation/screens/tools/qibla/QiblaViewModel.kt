package com.salahguard.app.presentation.screens.tools.qibla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salahguard.app.domain.repository.LocationRepository
import com.salahguard.app.domain.sensor.CompassSensor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*

data class QiblaUiState(
    val azimuth: Float = 0f,
    val qiblaDirection: Float = 0f,
    val distanceToKaaba: Double = 0.0,
    val userLocation: Pair<Double, Double>? = null
)

@HiltViewModel
class QiblaViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val compassSensor: CompassSensor
) : ViewModel() {

    private val _uiState = MutableStateFlow(QiblaUiState())
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()

    private val kaabaLat = 21.4225
    private val kaabaLng = 39.8262

    init {
        viewModelScope.launch {
            locationRepository.getCurrentLocation().collect { location ->
                if (location != null) {
                    val qibla = calculateQibla(location.first, location.second)
                    val distance = calculateDistance(location.first, location.second)
                    _uiState.update { it.copy(userLocation = location, qiblaDirection = qibla, distanceToKaaba = distance) }
                }
            }
        }

        viewModelScope.launch {
            compassSensor.getAzimuth().collect { azimuth ->
                _uiState.update { it.copy(azimuth = azimuth) }
            }
        }
    }

    private fun calculateQibla(lat: Double, lng: Double): Float {
        val phi1 = Math.toRadians(lat)
        val phi2 = Math.toRadians(kaabaLat)
        val deltaLambda = Math.toRadians(kaabaLng - lng)

        val y = sin(deltaLambda)
        val x = cos(phi1) * tan(phi2) - sin(phi1) * cos(deltaLambda)
        
        var qibla = atan2(y, x)
        qibla = Math.toDegrees(qibla)
        
        return ((qibla + 360) % 360).toFloat()
    }

    private fun calculateDistance(lat: Double, lng: Double): Double {
        val R = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(kaabaLat - lat)
        val dLng = Math.toRadians(kaabaLng - lng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat)) * cos(Math.toRadians(kaabaLat)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}
