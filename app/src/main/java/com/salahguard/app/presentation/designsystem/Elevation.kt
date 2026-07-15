package com.salahguard.app.presentation.designsystem

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * SalahGuard elevation scale.
 *
 * Kept intentionally shallow - this is a calm, glass-first design language,
 * not a heavily-shadowed material one. [SGElevation.hero] is reserved for
 * the single hero card on a screen.
 */
object SGElevation {
    val flat: Dp = 0.dp
    val standard: Dp = 4.dp
    val raised: Dp = 8.dp
    val hero: Dp = 16.dp
}
