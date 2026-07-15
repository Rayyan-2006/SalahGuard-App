package com.salahguard.app.presentation.screens.reflection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salahguard.app.domain.model.Reflection
import com.salahguard.app.domain.repository.ReflectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class ReflectionViewModel @Inject constructor(
    private val repository: ReflectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReflectionUiState(isLoading = true))
    val uiState: StateFlow<ReflectionUiState> = _uiState.asStateFlow()

    init {
        loadReflections()
    }

    private fun loadReflections() {
        viewModelScope.launch {
            repository.getAllReflections().collect { reflections ->
                _uiState.update { 
                    it.copy(
                        reflections = reflections,
                        filteredReflections = applyFilters(reflections, it.searchQuery, it.selectedPrayerFilter),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { 
            it.copy(
                searchQuery = query,
                filteredReflections = applyFilters(it.reflections, query, it.selectedPrayerFilter)
            )
        }
    }

    fun onPrayerFilterChange(prayer: String?) {
        _uiState.update { 
            it.copy(
                selectedPrayerFilter = prayer,
                filteredReflections = applyFilters(it.reflections, it.searchQuery, prayer)
            )
        }
    }

    private fun applyFilters(
        reflections: List<Reflection>,
        query: String,
        prayerFilter: String?
    ): List<Reflection> {
        val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM d")
        return reflections.filter { reflection ->
            val matchesQuery = reflection.reflectionText.contains(query, ignoreCase = true) ||
                    reflection.prayerName.contains(query, ignoreCase = true) ||
                    reflection.date.format(dateFormatter).contains(query, ignoreCase = true)
            val matchesPrayer = prayerFilter == null || reflection.prayerName.equals(prayerFilter, ignoreCase = true)
            matchesQuery && matchesPrayer
        }
    }

    fun saveReflection(
        prayerName: String,
        text: String,
        mood: String?,
        id: Long = 0,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            // Check for existing reflection for this prayer today to avoid duplicates
            val existingId = if (id == 0L) {
                _uiState.value.reflections.find { 
                    it.prayerName.equals(prayerName, ignoreCase = true) && it.date == LocalDate.now() 
                }?.id ?: 0L
            } else id

            val reflection = Reflection(
                id = existingId,
                prayerName = prayerName,
                date = LocalDate.now(),
                time = LocalTime.now(),
                reflectionText = text,
                mood = mood
            )
            repository.saveReflection(reflection)
            onSuccess()
        }
    }

    fun deleteReflection(id: Long) {
        viewModelScope.launch {
            repository.deleteReflection(id)
        }
    }
}
