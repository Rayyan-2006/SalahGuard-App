package com.salahguard.app.presentation.designsystem

import androidx.compose.ui.graphics.Color
import com.salahguard.app.presentation.theme.DisabledText
import com.salahguard.app.presentation.theme.GlassHighlight
import com.salahguard.app.presentation.theme.Gold
import com.salahguard.app.presentation.theme.GoldBright
import com.salahguard.app.presentation.theme.GoldLeafGradient
import com.salahguard.app.presentation.theme.MissedRed
import com.salahguard.app.presentation.theme.MutedSand
import com.salahguard.app.presentation.theme.NightSurface
import com.salahguard.app.presentation.theme.NightSurfaceLighter
import com.salahguard.app.presentation.theme.SanctuaryGradient
import com.salahguard.app.presentation.theme.SuccessGreen
import com.salahguard.app.presentation.theme.WarmIvory
import com.salahguard.app.presentation.theme.WarmStone

/**
 * Semantic color tokens for the design system.
 *
 * This is the *only* file allowed to reference raw palette colors from
 * `presentation/theme`. Every reusable component in `designsystem/components`
 * must read colors from here (never from `theme.Color` directly, and never
 * as an inline hex literal).
 */
object SGColors {

    // ---- Text ----
    val textPrimary: Color = WarmIvory
    val textSecondary: Color = WarmStone
    val textTertiary: Color = MutedSand
    val textDisabled: Color = DisabledText

    // ---- Accent ----
    val accent: Color = Gold
    val accentBright: Color = GoldBright
    val accentGradient: List<Color> = GoldLeafGradient

    // ---- Status ----
    val success: Color = SuccessGreen
    val warning: Color = GoldBright
    val error: Color = MissedRed

    // ---- Surfaces ----
    val surfaceHeroGradient: List<Color> = SanctuaryGradient
    val surfaceStandard: Color = NightSurface
    val surfaceRaised: Color = NightSurfaceLighter

    // ---- Glass ----
    val glassFill: Color = NightSurface.copy(alpha = 0.55f)
    val glassFillRaised: Color = NightSurfaceLighter.copy(alpha = 0.55f)
    val glassFillSubtle: Color = NightSurfaceLighter.copy(alpha = 0.28f)
    val glassBorder: Color = GlassHighlight.copy(alpha = 0.14f)
    val glassBorderSubtle: Color = GlassHighlight.copy(alpha = 0.08f)

    // ---- Divider ----
    val divider: Color = MutedSand.copy(alpha = 0.15f)

    // ---- Chip ----
    val chipActiveContainer: Color = Gold.copy(alpha = 0.16f)
    val chipActiveContent: Color = Gold
    val chipInactiveContainer: Color = NightSurfaceLighter
    val chipInactiveContent: Color = MutedSand
    val chipSuccessContainer: Color = SuccessGreen.copy(alpha = 0.16f)
    val chipSuccessContent: Color = SuccessGreen
    val chipWarningContainer: Color = GoldBright.copy(alpha = 0.16f)
    val chipWarningContent: Color = GoldBright

    // ---- Progress ----
    val progressTrack: Color = NightSurfaceLighter
    val progressFillGradient: List<Color> = GoldLeafGradient
}
