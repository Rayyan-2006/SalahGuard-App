package com.salahguard.app.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.salahguard.app.presentation.screens.home.HomeScreen
import com.salahguard.app.presentation.screens.journey.JourneyScreen
import com.salahguard.app.presentation.screens.onboarding.OnboardingScreen
import com.salahguard.app.presentation.screens.reflection.ReflectionScreen
import com.salahguard.app.presentation.screens.achievements.AchievementsScreen
import com.salahguard.app.presentation.screens.prayers.PrayersScreen
import com.salahguard.app.presentation.screens.quran.QuranScreen
import com.salahguard.app.presentation.screens.settings.SalahGuardSettingsScreen
import com.salahguard.app.presentation.screens.settings.NotificationsScreen
import com.salahguard.app.presentation.screens.splash.SplashScreen
import com.salahguard.app.presentation.screens.tools.qibla.QiblaScreen
import com.salahguard.app.presentation.screens.tools.mosques.MosqueFinderScreen

/**
 * Central navigation graph. 
 */
@Composable
fun SalahGuardNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SalahGuardDestination.Splash.route,
        enterTransition = {
            fadeIn(animationSpec = tween(500, easing = EaseInOutQuart)) +
            scaleIn(initialScale = 0.96f, animationSpec = tween(500, easing = EaseInOutQuart)) +
            slideInHorizontally(initialOffsetX = { it / 10 }, animationSpec = tween(500, easing = EaseOutQuart))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(500, easing = EaseInOutQuart)) +
            scaleOut(targetScale = 1.04f, animationSpec = tween(500, easing = EaseInOutQuart))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(500, easing = EaseInOutQuart)) +
            scaleIn(initialScale = 1.04f, animationSpec = tween(500, easing = EaseInOutQuart))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(500, easing = EaseInOutQuart)) +
            scaleOut(targetScale = 0.96f, animationSpec = tween(500, easing = EaseInOutQuart)) +
            slideOutHorizontally(targetOffsetX = { it / 10 }, animationSpec = tween(500, easing = EaseOutQuart))
        }
    ) {
        composable(SalahGuardDestination.Splash.route) {
            SplashScreen(
                onFinished = { destination ->
                    navController.navigate(destination) {
                        popUpTo(SalahGuardDestination.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(SalahGuardDestination.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(SalahGuardDestination.Home.route) {
                        popUpTo(SalahGuardDestination.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(SalahGuardDestination.Home.route) {
            HomeScreen(
                onNavigateToPrayers = {
                    navController.navigate(SalahGuardDestination.Prayer.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToQuran = {
                    navController.navigate(SalahGuardDestination.Quran.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToJourney = {
                    navController.navigate(SalahGuardDestination.Journey.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToReflection = { query ->
                    val route = if (query != null) {
                        "${SalahGuardDestination.ReflectionDest.route}?${SalahGuardDestination.ReflectionDest.argInitialQuery}=$query"
                    } else {
                        SalahGuardDestination.ReflectionDest.route
                    }
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToNotifications = {
                    navController.navigate(SalahGuardDestination.Notifications.route)
                },
                onNavigateToSettings = {
                    navController.navigate(SalahGuardDestination.Settings.route)
                }
            )
        }
        
        composable(SalahGuardDestination.Settings.route) {
            SalahGuardSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNotifications = {
                    navController.navigate(SalahGuardDestination.Notifications.route) {
                        popUpTo(SalahGuardDestination.Home.route)
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(SalahGuardDestination.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = {
                    navController.navigate(SalahGuardDestination.Settings.route) {
                        popUpTo(SalahGuardDestination.Home.route)
                        launchSingleTop = true
                    }
                }
            )
        }
        
        composable(SalahGuardDestination.Prayer.route) {
            PrayersScreen(
                onNavigateToHome = {
                    navController.navigate(SalahGuardDestination.Home.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToQuran = {
                    navController.navigate(SalahGuardDestination.Quran.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToJourney = {
                    navController.navigate(SalahGuardDestination.Journey.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToReflection = { query ->
                    val route = if (query != null) {
                        "${SalahGuardDestination.ReflectionDest.route}?${SalahGuardDestination.ReflectionDest.argInitialQuery}=$query"
                    } else {
                        SalahGuardDestination.ReflectionDest.route
                    }
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToQibla = {
                    navController.navigate(SalahGuardDestination.Qibla.route)
                },
                onNavigateToMosques = {
                    navController.navigate(SalahGuardDestination.Mosques.route)
                }
            )
        }
        
        composable(SalahGuardDestination.Quran.route) {
            QuranScreen(
                onNavigateToHome = {
                    navController.navigate(SalahGuardDestination.Home.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToPrayers = {
                    navController.navigate(SalahGuardDestination.Prayer.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToJourney = {
                    navController.navigate(SalahGuardDestination.Journey.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToReflection = { query ->
                    val route = if (query != null) {
                        "${SalahGuardDestination.ReflectionDest.route}?${SalahGuardDestination.ReflectionDest.argInitialQuery}=$query"
                    } else {
                        SalahGuardDestination.ReflectionDest.route
                    }
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToQibla = {
                    navController.navigate(SalahGuardDestination.Qibla.route)
                },
                onNavigateToMosques = {
                    navController.navigate(SalahGuardDestination.Mosques.route)
                }
            )
        }
        
        composable(SalahGuardDestination.Journey.route) {
            JourneyScreen(
                onNavigateToHome = {
                    navController.navigate(SalahGuardDestination.Home.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToPrayers = {
                    navController.navigate(SalahGuardDestination.Prayer.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToQuran = {
                    navController.navigate(SalahGuardDestination.Quran.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToReflection = { query ->
                    val route = if (query != null) {
                        "${SalahGuardDestination.ReflectionDest.route}?${SalahGuardDestination.ReflectionDest.argInitialQuery}=$query"
                    } else {
                        SalahGuardDestination.ReflectionDest.route
                    }
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToAchievements = {
                    navController.navigate(SalahGuardDestination.Achievements.route)
                }
            )
        }
        
        composable(SalahGuardDestination.Achievements.route) {
            AchievementsScreen(
                onNavigateToHome = {
                    navController.navigate(SalahGuardDestination.Home.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToPrayers = {
                    navController.navigate(SalahGuardDestination.Prayer.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToQuran = {
                    navController.navigate(SalahGuardDestination.Quran.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToJourney = {
                    navController.navigate(SalahGuardDestination.Journey.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToReflection = { query ->
                    val route = if (query != null) {
                        "${SalahGuardDestination.ReflectionDest.route}?${SalahGuardDestination.ReflectionDest.argInitialQuery}=$query"
                    } else {
                        SalahGuardDestination.ReflectionDest.route
                    }
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToQibla = {
                    navController.navigate(SalahGuardDestination.Qibla.route)
                },
                onNavigateToMosques = {
                    navController.navigate(SalahGuardDestination.Mosques.route)
                }
            )
        }
        
        composable(
            route = SalahGuardDestination.ReflectionDest.routeWithArgs,
            arguments = listOf(
                navArgument(SalahGuardDestination.ReflectionDest.argInitialQuery) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val initialQuery = backStackEntry.arguments?.getString(SalahGuardDestination.ReflectionDest.argInitialQuery)
            ReflectionScreen(
                initialQuery = initialQuery,
                onNavigateToHome = {
                    navController.navigate(SalahGuardDestination.Home.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToPrayers = {
                    navController.navigate(SalahGuardDestination.Prayer.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToQuran = {
                    navController.navigate(SalahGuardDestination.Quran.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToJourney = {
                    navController.navigate(SalahGuardDestination.Journey.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToReflection = { query ->
                    val route = if (query != null) {
                        "${SalahGuardDestination.ReflectionDest.route}?${SalahGuardDestination.ReflectionDest.argInitialQuery}=$query"
                    } else {
                        SalahGuardDestination.ReflectionDest.route
                    }
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToQibla = {
                    navController.navigate(SalahGuardDestination.Qibla.route)
                },
                onNavigateToMosques = {
                    navController.navigate(SalahGuardDestination.Mosques.route)
                }
            )
        }
        
        // Single definition for tool screens
        composable(SalahGuardDestination.Qibla.route) {
            QiblaScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(SalahGuardDestination.Mosques.route) {
            MosqueFinderScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
