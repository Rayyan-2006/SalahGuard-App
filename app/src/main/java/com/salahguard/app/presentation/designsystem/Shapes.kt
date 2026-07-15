package com.salahguard.app.presentation.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * SalahGuard corner-radius scale.
 *
 * [radius] values are exposed for cases that need the raw Dp (e.g. blur /
 * clip math); [shape] values are the ready-to-use RoundedCornerShape for
 * Card / Box / Modifier.clip usage.
 */
object SGShapes {
    val smallRadius: Dp = 12.dp
    val mediumRadius: Dp = 16.dp
    val largeRadius: Dp = 24.dp
    val heroRadius: Dp = 32.dp

    val small: Shape = RoundedCornerShape(smallRadius)
    val medium: Shape = RoundedCornerShape(mediumRadius)
    val large: Shape = RoundedCornerShape(largeRadius)
    val hero: Shape = RoundedCornerShape(heroRadius)

    /** Fully rounded shape, used for chips, pills, and circular icon buttons. */
    val pill: Shape = RoundedCornerShape(50)
}
