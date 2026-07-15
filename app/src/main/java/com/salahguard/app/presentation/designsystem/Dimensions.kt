package com.salahguard.app.presentation.designsystem

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * SalahGuard fixed-size tokens - heights, icon sizes, and stroke widths
 * that aren't part of the spacing scale but still shouldn't be
 * hardcoded inside individual components.
 */
object SGDimensions {
    // Buttons
    val buttonHeight: Dp = 64.dp
    val iconButtonSize: Dp = 48.dp
    val iconButtonSizeCompact: Dp = 40.dp

    // Icons
    val iconSize: Dp = 24.dp
    val iconSizeSmall: Dp = 18.dp

    // Card padding
    val heroCardPadding: Dp = SGSpacing.xl
    val glassCardPadding: Dp = SGSpacing.lg
    val surfaceCardPadding: Dp = SGSpacing.default

    // Borders
    val borderWidthThin: Dp = 1.dp
    val borderWidthStandard: Dp = 1.2.dp

    // Dividers
    val dividerThickness: Dp = 1.dp

    // Chips
    val chipHeight: Dp = 32.dp
    val chipHorizontalPadding: Dp = SGSpacing.md

    // Progress bar
    val progressBarHeight: Dp = 8.dp
    val progressBarHeightCompact: Dp = 6.dp
}
