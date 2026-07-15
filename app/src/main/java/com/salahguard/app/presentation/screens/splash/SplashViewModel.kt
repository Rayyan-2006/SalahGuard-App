package com.salahguard.app.presentation.screens.splash

import androidx.lifecycle.ViewModel
import com.salahguard.app.domain.repository.UserPreferencesRepository
import com.salahguard.app.domain.usecase.StartupSyncUseCase
import com.salahguard.app.presentation.navigation.SalahGuardDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val startupSyncUseCase: StartupSyncUseCase
) : ViewModel() {

    /**
     * Suspends until we know whether onboarding is done, then returns the
     * route to navigate to. Called once from SplashScreen's LaunchedEffect.
     */
    suspend fun resolveStartDestination(): String {
        // Run startup sync in background (seeding Quran, etc)
        startupSyncUseCase()

        val completed = userPreferencesRepository.isOnboardingCompleted().first()
        return if (completed) {
            SalahGuardDestination.Home.route
        } else {
            SalahGuardDestination.Onboarding.route
        }
    }
}
