package com.salahguard.app.presentation.navigation

/**
 * Every screen in the app gets a route here.
 * Matches the build order in the vision doc:
 * Splash -> Onboarding -> Home -> Prayer -> Reflection -> Quran -> Settings
 */
sealed class SalahGuardDestination(val route: String) {
    data object Splash : SalahGuardDestination("splash")
    data object Onboarding : SalahGuardDestination("onboarding")
    data object Home : SalahGuardDestination("home")
    data object Prayer : SalahGuardDestination("prayer")
    data object ReflectionDest : SalahGuardDestination("reflection") {
        const val argInitialQuery = "initialQuery"
        val routeWithArgs = "$route?$argInitialQuery={$argInitialQuery}"
    }
    data object Quran : SalahGuardDestination("quran")
    data object Settings : SalahGuardDestination("settings")
    data object Notifications : SalahGuardDestination("notifications")
    data object Journey : SalahGuardDestination("journey")
    data object Achievements : SalahGuardDestination("achievements")
    data object Qibla : SalahGuardDestination("qibla")
    data object Mosques : SalahGuardDestination("mosques")
}
