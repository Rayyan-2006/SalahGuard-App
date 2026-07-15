package com.salahguard.app.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import com.salahguard.app.presentation.designsystem.SGShapes

/**
 * Material3 shape scale, expressed in terms of the Design System's shape
 * tokens (`designsystem/Shapes.kt`) instead of a second, parallel set of
 * radius values.
 *
 * NOT currently wired into `MaterialTheme(shapes = ...)`. Several
 * screens rely on Material's *default* Shapes for components that don't
 * pass an explicit `shape =` - AlertDialog, OutlinedTextField, and the
 * chip-style controls in Settings/Prayers. Switching the theme default
 * would nudge those corner radii (e.g. dialogs 28.dp -> 32.dp, chips
 * 8.dp -> 12.dp) without any way to visually verify it in this
 * environment, which risks exactly the "no visual regressions"
 * requirement this ticket is meant to protect. [SalahGuardShapes] is
 * defined and ready so a screen-level ticket can either wire it globally
 * once those components are checked, or apply it selectively per
 * component - see "Remaining styling that still needs migration".
 */
val SalahGuardShapes = Shapes(
    extraSmall = RoundedCornerShape(SGShapes.smallRadius / 2),
    small = RoundedCornerShape(SGShapes.smallRadius),
    medium = RoundedCornerShape(SGShapes.mediumRadius),
    large = RoundedCornerShape(SGShapes.largeRadius),
    extraLarge = RoundedCornerShape(SGShapes.heroRadius)
)
