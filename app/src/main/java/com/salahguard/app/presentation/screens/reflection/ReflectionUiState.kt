package com.salahguard.app.presentation.screens.reflection

import com.salahguard.app.domain.model.Reflection

data class ReflectionUiState(
    val reflections: List<Reflection> = emptyList(),
    val filteredReflections: List<Reflection> = emptyList(),
    val searchQuery: String = "",
    val selectedPrayerFilter: String? = null,
    val isLoading: Boolean = false
)
