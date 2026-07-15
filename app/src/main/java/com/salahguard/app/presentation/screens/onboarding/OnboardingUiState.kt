package com.salahguard.app.presentation.screens.onboarding

data class OnboardingStep(
    val title: String,
    val message: String
)

data class OnboardingUiState(
    val steps: List<OnboardingStep> = listOf(
        OnboardingStep(
            title = "Return to Peace",
            message = "SalahGuard is your private companion that helps you return to Allah through hope, consistency, and gentle guidance."
        ),
        OnboardingStep(
            title = "Every Prayer is a New Beginning",
            message = "Missing one prayer is not the end of your journey. Every next prayer is another opportunity to return."
        ),
        OnboardingStep(
            title = "Build Your Journey",
            message = "Track prayers, read Quran, reflect, learn, and grow—one day at a time."
        ),
        OnboardingStep(
            title = "Begin Your Journey",
            message = "May Allah make this journey easy and accept every step you take."
        )
    )
)
