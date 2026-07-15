package com.salahguard.app.presentation.screens.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salahguard.app.domain.model.Achievement
import com.salahguard.app.domain.repository.UserPreferencesRepository
import com.salahguard.app.domain.usecase.GetAchievementsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val getAchievementsUseCase: GetAchievementsUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    init {
        loadAchievements()
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            getAchievementsUseCase().collect { achievements ->
                val unlockedIds = userPreferencesRepository.getUnlockedAchievementIds().first()
                
                achievements.forEach { achievement ->
                    if (achievement.isUnlocked && !unlockedIds.contains(achievement.id)) {
                        markAsNewlyUnlocked(achievement)
                    }
                }

                _uiState.update { 
                    it.copy(
                        achievements = achievements,
                        isLoading = false,
                        unlockedCount = achievements.count { a -> a.isUnlocked },
                        totalCount = achievements.size
                    )
                }
            }
        }
    }

    private fun markAsNewlyUnlocked(achievement: Achievement) {
        viewModelScope.launch {
            userPreferencesRepository.markAchievementUnlocked(achievement.id)
            _uiState.update { it.copy(newlyUnlocked = achievement) }
        }
    }

    fun dismissUnlockedDialog() {
        _uiState.update { it.copy(newlyUnlocked = null) }
    }
}
