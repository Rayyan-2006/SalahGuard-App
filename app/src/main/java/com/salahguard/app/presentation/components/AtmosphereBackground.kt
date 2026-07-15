package com.salahguard.app.presentation.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.salahguard.app.presentation.theme.AsrBloom
import com.salahguard.app.presentation.theme.AsrHaze
import com.salahguard.app.presentation.theme.AsrSkyLow
import com.salahguard.app.presentation.theme.AsrSkyMid
import com.salahguard.app.presentation.theme.AsrSkyTop
import com.salahguard.app.presentation.theme.DhuhrBloom
import com.salahguard.app.presentation.theme.DhuhrHaze
import com.salahguard.app.presentation.theme.DhuhrSkyLow
import com.salahguard.app.presentation.theme.DhuhrSkyMid
import com.salahguard.app.presentation.theme.DhuhrSkyTop
import com.salahguard.app.presentation.theme.FajrBloom
import com.salahguard.app.presentation.theme.FajrHaze
import com.salahguard.app.presentation.theme.FajrSkyLow
import com.salahguard.app.presentation.theme.FajrSkyMid
import com.salahguard.app.presentation.theme.FajrSkyTop
import com.salahguard.app.presentation.theme.IshaBloom
import com.salahguard.app.presentation.theme.IshaHaze
import com.salahguard.app.presentation.theme.IshaSkyLow
import com.salahguard.app.presentation.theme.IshaSkyMid
import com.salahguard.app.presentation.theme.IshaSkyTop
import com.salahguard.app.presentation.theme.MaghribBloom
import com.salahguard.app.presentation.theme.MaghribHaze
import com.salahguard.app.presentation.theme.MaghribSkyLow
import com.salahguard.app.presentation.theme.MaghribSkyMid
import com.salahguard.app.presentation.theme.MaghribSkyTop
import com.salahguard.app.presentation.theme.SunriseBloom
import com.salahguard.app.presentation.theme.SunriseHaze
import com.salahguard.app.presentation.theme.SunriseSkyLow
import com.salahguard.app.presentation.theme.SunriseSkyMid
import com.salahguard.app.presentation.theme.SunriseSkyTop
import kotlin.random.Random

/**
 * The Atmospheric Background Engine.
 *
 * This is deliberately NOT a wallpaper, a photo, or an illustration. It is a
 * small stack of pure-color layers — a base gradient, a soft radial bloom, a
 * faint haze, a corner vignette and (optionally) a whisper of grain — that
 * together imply natural light and depth for the current prayer period
 * without ever drawing a literal "scene". The UI is meant to float above
 * this, the way it floats above real daylight.
 *
 * Reusable by design: any screen can drop this in and pass its own
 * [prayerName] and [opacity]. Home currently uses full opacity via
 * [HomeSanctuaryBackground]; Prayer, Journey and Reflect are expected to
 * reuse this same engine later at a lower [opacity] so their own content
 * stays primary.
 *
 * Performance notes:
 *  - Nothing here runs an infinite/continuous animation. The only motion is
 *    the one-time crossfade when [prayerName] changes (1200–1500ms), so
 *    there is no per-frame recomposition or redraw cost while a screen sits
 *    idle — this was a deliberate change from the previous "breathing"
 *    pulse, which recomposed a Canvas every frame forever.
 *  - Every layer is a plain gradient or a tiny fixed set of pre-computed
 *    points; there is no blur (Modifier.blur / RenderEffect) anywhere, since
 *    blur is one of the more GPU-expensive operations on Android.
 *  - Grain positions are generated once with a seeded [Random] and
 *    `remember`-ed, never recomputed per recomposition or per theme.
 */

/** The six atmospheric looks, one per prayer period (Sunrise included). */
enum class AtmospherePeriod { FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA }

/**
 * A complete, self-contained "look" for one prayer period: a 3-stop base
 * gradient plus the tuning for the bloom/haze/vignette/grain layers drawn
 * on top of it. Every value is a plain color or fraction — no assets.
 */
data class AtmosphereTheme(
    val period: AtmospherePeriod,
    val gradient: List<Color>,
    val bloomColor: Color,
    val bloomCenter: Offset,
    val bloomRadiusFraction: Float,
    val hazeColor: Color,
    val hazeCenter: Offset,
    val hazeRadiusFraction: Float,
    val hazeAlpha: Float,
    val vignetteAlpha: Float,
    /** Whether this atmosphere reads as visually bright overall (Dhuhr/Asr). */
    val isBright: Boolean,
    /** Extra top-edge darkening so header text stays readable on bright skies. */
    val topScrimAlpha: Float,
    val grainColor: Color,
    val grainAlpha: Float
)

private object AtmosphereThemes {

    val FAJR = AtmosphereTheme(
        period = AtmospherePeriod.FAJR,
        gradient = listOf(FajrSkyTop, FajrSkyMid, FajrSkyLow),
        bloomColor = FajrBloom,
        bloomCenter = Offset(0.72f, 0.06f),
        bloomRadiusFraction = 0.95f,
        hazeColor = FajrHaze,
        hazeCenter = Offset(0.20f, 0.85f),
        hazeRadiusFraction = 0.9f,
        hazeAlpha = 0.30f,
        vignetteAlpha = 0.42f,
        isBright = false,
        topScrimAlpha = 0.10f,
        grainColor = Color(0xFFEFF3F8),
        grainAlpha = 0.025f
    )

    val SUNRISE = AtmosphereTheme(
        period = AtmospherePeriod.SUNRISE,
        gradient = listOf(SunriseSkyTop, SunriseSkyMid, SunriseSkyLow),
        bloomColor = SunriseBloom,
        bloomCenter = Offset(0.5f, 0.78f),
        bloomRadiusFraction = 1.05f,
        hazeColor = SunriseHaze,
        hazeCenter = Offset(0.5f, 0.95f),
        hazeRadiusFraction = 1.0f,
        hazeAlpha = 0.38f,
        vignetteAlpha = 0.30f,
        isBright = false,
        topScrimAlpha = 0.14f,
        grainColor = Color(0xFFFFEFE0),
        grainAlpha = 0.03f
    )

    val DHUHR = AtmosphereTheme(
        period = AtmospherePeriod.DHUHR,
        gradient = listOf(DhuhrSkyTop, DhuhrSkyMid, DhuhrSkyLow),
        bloomColor = DhuhrBloom,
        bloomCenter = Offset(0.5f, 0.02f),
        bloomRadiusFraction = 0.85f,
        hazeColor = DhuhrHaze,
        hazeCenter = Offset(0.5f, 1.0f),
        hazeRadiusFraction = 0.95f,
        hazeAlpha = 0.22f,
        vignetteAlpha = 0.20f,
        isBright = true,
        topScrimAlpha = 0.38f,
        grainColor = Color(0xFF3E4A50),
        grainAlpha = 0.018f
    )

    val ASR = AtmosphereTheme(
        period = AtmospherePeriod.ASR,
        gradient = listOf(AsrSkyTop, AsrSkyMid, AsrSkyLow),
        bloomColor = AsrBloom,
        bloomCenter = Offset(0.68f, 0.12f),
        bloomRadiusFraction = 0.95f,
        hazeColor = AsrHaze,
        hazeCenter = Offset(0.25f, 0.9f),
        hazeRadiusFraction = 0.95f,
        hazeAlpha = 0.30f,
        vignetteAlpha = 0.26f,
        isBright = true,
        topScrimAlpha = 0.28f,
        grainColor = Color(0xFF4A3416),
        grainAlpha = 0.02f
    )

    val MAGHRIB = AtmosphereTheme(
        period = AtmospherePeriod.MAGHRIB,
        gradient = listOf(MaghribSkyTop, MaghribSkyMid, MaghribSkyLow),
        bloomColor = MaghribBloom,
        bloomCenter = Offset(0.5f, 0.88f),
        bloomRadiusFraction = 1.15f,
        hazeColor = MaghribHaze,
        hazeCenter = Offset(0.72f, 0.4f),
        hazeRadiusFraction = 1.0f,
        hazeAlpha = 0.40f,
        vignetteAlpha = 0.36f,
        isBright = false,
        topScrimAlpha = 0.12f,
        grainColor = Color(0xFFFFE3D0),
        grainAlpha = 0.03f
    )

    val ISHA = AtmosphereTheme(
        period = AtmospherePeriod.ISHA,
        gradient = listOf(IshaSkyTop, IshaSkyMid, IshaSkyLow),
        bloomColor = IshaBloom,
        bloomCenter = Offset(0.62f, 0.08f),
        bloomRadiusFraction = 0.9f,
        hazeColor = IshaHaze,
        hazeCenter = Offset(0.2f, 0.92f),
        hazeRadiusFraction = 0.9f,
        hazeAlpha = 0.30f,
        vignetteAlpha = 0.46f,
        isBright = false,
        topScrimAlpha = 0.08f,
        grainColor = Color(0xFFE8F2EC),
        grainAlpha = 0.022f
    )
}

/**
 * Resolves the atmosphere for a prayer name as surfaced by
 * [com.salahguard.app.presentation.screens.home.HomeUiState] (e.g. "Fajr",
 * "Sunrise", "Dhuhr", ...). Case-insensitive. Falls back to Dhuhr (daylight)
 * for null/unknown values so the background never renders blank.
 */
fun atmosphereThemeFor(prayerName: String?): AtmosphereTheme = when (prayerName?.trim()?.lowercase()) {
    "fajr" -> AtmosphereThemes.FAJR
    "sunrise", "shurooq", "sherooq" -> AtmosphereThemes.SUNRISE
    "dhuhr" -> AtmosphereThemes.DHUHR
    "asr" -> AtmosphereThemes.ASR
    "maghrib" -> AtmosphereThemes.MAGHRIB
    "isha" -> AtmosphereThemes.ISHA
    else -> AtmosphereThemes.DHUHR
}

/** How long a prayer-change crossfade takes. Kept inside the 1200–1500ms spec range. */
private const val CROSSFADE_DURATION_MS = 1350

/**
 * Fixed near-black ink used only for the top readability scrim (see
 * [AtmosphereLayers]). Intentionally NOT derived from any theme's own
 * gradient, since several themes (Dhuhr, Asr) are themselves light —
 * darkening with a light color would brighten instead of darken.
 */
private val READABILITY_INK = Color(0xFF04070A)

/**
 * The reusable atmospheric background composable.
 *
 * @param prayerName the current prayer period name (Fajr/Sunrise/Dhuhr/Asr/
 *   Maghrib/Isha), case-insensitive. Unknown/null falls back to Dhuhr.
 * @param opacity overall strength of the atmosphere layer, so screens with
 *   denser content (Prayer, Journey, Reflect) can dial it down without
 *   touching their own foreground.
 * @param content foreground UI, drawn above the atmosphere at full opacity
 *   regardless of [opacity].
 */
@Composable
fun AtmosphereBackground(
    modifier: Modifier = Modifier,
    prayerName: String?,
    opacity: Float = 1f,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val theme = remember(prayerName) { atmosphereThemeFor(prayerName) }

    // Grain dot positions: generated once, ever, for the lifetime of this
    // composable instance — never regenerated on theme or size change.
    val grainPoints = remember {
        val random = Random(seed = 7)
        List(140) { Triple(random.nextFloat(), random.nextFloat(), random.nextFloat() * 1.4f + 0.5f) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = opacity }
        ) {
            Crossfade(
                targetState = theme,
                animationSpec = tween(durationMillis = CROSSFADE_DURATION_MS, easing = EaseInOutSine),
                label = "atmosphereCrossfade"
            ) { activeTheme ->
                AtmosphereLayers(theme = activeTheme, grainPoints = grainPoints)
            }
        }
        content()
    }
}

/**
 * The five layers, back to front, for a single [AtmosphereTheme]:
 * 1. Base gradient  2. Radial bloom  3. Soft haze  4. Vignette  5. Grain.
 * A readability scrim is folded into the vignette/top pass rather than
 * being a separate draw call, since it's cheaper and reads identically.
 */
@Composable
private fun AtmosphereLayers(theme: AtmosphereTheme, grainPoints: List<Triple<Float, Float, Float>>) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Base gradient — the sky itself.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(theme.gradient))
        )

        // 2 + 3 + 4. Bloom, haze and vignette in one Canvas pass (three
        // cheap radial gradients, no blur, drawn once per theme change).
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Layer 2: large radial bloom — the implied light source.
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(theme.bloomColor.copy(alpha = 0.55f), Color.Transparent),
                    center = Offset(size.width * theme.bloomCenter.x, size.height * theme.bloomCenter.y),
                    radius = size.width * theme.bloomRadiusFraction
                )
            )

            // Layer 3: soft atmospheric haze — a second, gentler glow that
            // adds depth without reading as a second light source.
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(theme.hazeColor.copy(alpha = theme.hazeAlpha), Color.Transparent),
                    center = Offset(size.width * theme.hazeCenter.x, size.height * theme.hazeCenter.y),
                    radius = size.width * theme.hazeRadiusFraction
                )
            )

            // Layer 4: vignette — quiets the corners so focus stays central.
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, theme.gradient.last().copy(alpha = theme.vignetteAlpha)),
                    center = Offset(size.width * 0.5f, size.height * 0.46f),
                    radius = size.width * 1.05f
                )
            )

            // Readability scrim — a top-edge fade in a fixed near-black ink
            // (never a theme color) so it reliably darkens rather than
            // brightens. This is what keeps header text legible on bright
            // atmospheres like Dhuhr/Asr, where every theme color itself is
            // too light to darken anything. Alpha is tuned per theme
            // ([AtmosphereTheme.topScrimAlpha]) so dark atmospheres like
            // Isha barely need it while Dhuhr needs the most.
            drawRect(
                brush = Brush.verticalGradient(
                    0.0f to READABILITY_INK.copy(alpha = theme.topScrimAlpha),
                    0.22f to Color.Transparent
                )
            )
        }

        // 5. Extremely faint grain — a fixed set of tiny dots, static,
        // drawn once. No noise texture generation, no per-frame cost.
        if (theme.grainAlpha > 0f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                grainPoints.forEach { (fx, fy, r) ->
                    drawCircle(
                        color = theme.grainColor.copy(alpha = theme.grainAlpha),
                        radius = r,
                        center = Offset(size.width * fx, size.height * fy)
                    )
                }
            }
        }
    }
}