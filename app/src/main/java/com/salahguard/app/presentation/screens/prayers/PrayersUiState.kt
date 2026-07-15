package com.salahguard.app.presentation.screens.prayers

import com.salahguard.app.domain.model.Prayer
import java.time.LocalDate

data class PrayersUiState(
    val date: LocalDate = LocalDate.now(),
    val prayers: List<Prayer> = emptyList(),
    val isLoading: Boolean = false,
    val locationError: String? = null,
    val azimuth: Float = 0f
)
