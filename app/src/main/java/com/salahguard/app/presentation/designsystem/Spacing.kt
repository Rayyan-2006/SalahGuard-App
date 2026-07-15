package com.salahguard.app.presentation.designsystem

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * SalahGuard spacing scale.
 *
 * Every reusable design-system component sources its padding, gaps, and
 * offsets from here. Only the values on this scale (4 / 8 / 12 / 16 / 20 /
 * 24 / 32 / 40) may be used - no arbitrary dp values inside components.
 */
object SGSpacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val default: Dp = 16.dp
    val lg: Dp = 20.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 40.dp
}
