package com.salahguard.app.presentation.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable "glass" surface treatment: a translucent fill over a clipped
 * shape with a hairline border. This is what makes SGHeroCard / SGGlassCard
 * / SGSurfaceCard read as frosted glass rather than flat Material cards.
 *
 * Not a true backdrop blur (Compose has no cross-platform backdrop-filter
 * in this SDK range) - the translucency plus soft border is what reads as
 * "glass" against SalahGuard's dark, photographic backgrounds. Use
 * [Modifier.sgBackgroundBlur] separately for blurring background imagery
 * layers, never on text-bearing content.
 */
fun Modifier.sgGlassSurface(
    shape: Shape = SGShapes.large,
    fillColor: Color = SGColors.glassFill,
    borderColor: Color = SGColors.glassBorder,
    borderWidth: Dp = SGDimensions.borderWidthStandard
): Modifier = this
    .clip(shape)
    .background(color = fillColor, shape = shape)
    .border(BorderStroke(borderWidth, borderColor), shape)

/**
 * Blurs whatever this modifier is applied to. Intended for background
 * imagery / glow layers sitting behind glass cards - never for modifiers
 * applied directly to text or icons, which would simply look broken.
 */
fun Modifier.sgBackgroundBlur(radius: Dp): Modifier =
    if (radius > 0.dp) this.blur(radius) else this

/**
 * Named glass presets matching the three SG card tiers, so components
 * never have to hand-tune fill/border combinations themselves.
 */
object SGGlass {
    fun hero(): Modifier = Modifier.sgGlassSurface(
        shape = SGShapes.hero,
        fillColor = SGColors.glassFillRaised,
        borderColor = SGColors.glassBorder,
        borderWidth = SGDimensions.borderWidthStandard
    )

    fun standard(): Modifier = Modifier.sgGlassSurface(
        shape = SGShapes.large,
        fillColor = SGColors.glassFill,
        borderColor = SGColors.glassBorder,
        borderWidth = SGDimensions.borderWidthThin
    )

    fun subtle(): Modifier = Modifier.sgGlassSurface(
        shape = SGShapes.medium,
        fillColor = SGColors.glassFillSubtle,
        borderColor = SGColors.glassBorderSubtle,
        borderWidth = SGDimensions.borderWidthThin
    )
}
