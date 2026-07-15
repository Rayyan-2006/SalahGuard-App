package com.salahguard.app.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Home's atmospheric background: the illustrated per-prayer skyline art
 * (res/drawable/bg_*.jpeg) as the base "scene", finished with a color-grade
 * wash, bloom, vignette and readability scrim pulled from
 * [atmosphereThemeFor] — the same six-period palette that drives the
 * pure-color [AtmosphereBackground] engine used elsewhere. This keeps one
 * single source of truth for "what Fajr/Sunrise/Dhuhr/Asr/Maghrib/Isha
 * feel like" while still giving Home the richer, more premium look the
 * illustrated art provides over flat gradients alone.
 *
 * Layered back-to-front:
 * 1. Safety-net gradient (from the period's own theme colors) — invisible
 *    under the opaque photo above it, shown only if a drawable fails to load.
 * 2. The current prayer's illustrated skyline, crossfading over 1350ms
 *    whenever the prayer changes.
 * 3. A color-grade wash in the period's own palette, so every prayer still
 *    reads as its own distinct mood even though Sunrise reuses the Fajr art
 *    (see [PrayerBackgroundMapper] for why).
 * 4. A fixed-ink readability scrim, stronger at the top (header text) and
 *    bottom (nav bar), tuned per period so bright scenes (Dhuhr, Asr) get
 *    more help than dark ones (Isha).
 * 5. A static radial bloom implying one warm light source.
 * 6. A corner vignette.
 * 7. The near-invisible Islamic geometric lattice (static, Home-only).
 * 8. Prayer-specific decorative overlays (stars, clouds, birds, glow,
 *    light, sparkles) from [PrayerBackgroundMapper], each with its own
 *    slow, low-amplitude motion.
 * 9. Home screen content — untouched by this ticket.
 *
 * Performance: the only continuous animation left is the small per-overlay
 * drift/twinkle (cheap `graphicsLayer { alpha/translationX/Y }` on a single
 * Image each) — there is no full-screen Canvas redrawn every frame, which
 * is what the previous "breathing" glow did.
 */
@Composable
fun HomeSanctuaryBackground(
    modifier: Modifier = Modifier,
    currentPrayerName: String? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val atmosphere = remember(currentPrayerName) {
        PrayerBackgroundMapper.atmosphereFor(currentPrayerName)
    }
    val theme = remember(currentPrayerName) {
        atmosphereThemeFor(currentPrayerName)
    }
    val showStars = remember(atmosphere) { atmosphere.overlays.contains(PrayerOverlay.STARS) }

    val transition = rememberInfiniteTransition(label = "homeSanctuaryOverlays")

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Safety-net gradient — invisible under the opaque photo above
        // it, but keeps a themed look if a drawable ever fails to decode.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(theme.gradient))
        )

        // 2. Prayer skyline art — crossfades on prayer change.
        AnimatedContent(
            targetState = atmosphere.background,
            transitionSpec = {
                fadeIn(animationSpec = tween(1350, easing = EaseInOutSine))
                    .togetherWith(fadeOut(animationSpec = tween(1350, easing = EaseInOutSine)))
            },
            label = "prayerBackgroundCrossfade"
        ) { backgroundRes ->
            Image(
                painter = painterResource(id = backgroundRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // 3. Color-grade wash — tints the shared illustration style toward
        // this period's own palette so Sunrise (which reuses the Fajr art)
        // still reads as its own moment, and every period feels cohesive
        // with the rest of the app's color language.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to theme.gradient.first().copy(alpha = 0.22f),
                        0.55f to theme.bloomColor.copy(alpha = 0.10f),
                        1.0f to theme.gradient.last().copy(alpha = 0.32f)
                    )
                )
        )

        // 4. Readability scrim — fixed dark ink, never a light theme color,
        // so header text and the bottom nav bar keep contrast regardless of
        // how bright the underlying art is. Alpha is tuned per period via
        // [AtmosphereTheme.topScrimAlpha].
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.00f to READABILITY_INK.copy(alpha = theme.topScrimAlpha + 0.12f),
                        0.24f to READABILITY_INK.copy(alpha = theme.topScrimAlpha * 0.35f),
                        0.62f to READABILITY_INK.copy(alpha = theme.topScrimAlpha * 0.45f),
                        1.00f to READABILITY_INK.copy(alpha = theme.topScrimAlpha + 0.22f)
                    )
                )
        )

        // 5 + 6. Bloom + vignette — one static Canvas pass, no animation.
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(theme.bloomColor.copy(alpha = 0.30f), Color.Transparent),
                    center = Offset(size.width * theme.bloomCenter.x, size.height * theme.bloomCenter.y),
                    radius = size.width * theme.bloomRadiusFraction
                )
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, READABILITY_INK.copy(alpha = theme.vignetteAlpha)),
                    center = Offset(size.width * 0.5f, size.height * 0.44f),
                    radius = size.width * 1.05f
                )
            )
        }

        // 7. Extremely subtle geometric lattice — static, drawn once per size.
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawIslamicLattice(this)
        }

        // 8. Prayer-specific decorative overlays on top of everything else.
        val starTwinkle = if (showStars) {
            val twinkle by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 0.65f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 4200, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "starTwinkle"
            )
            twinkle
        } else {
            0f
        }

        atmosphere.overlays.forEach { overlay ->
            PrayerOverlayLayer(overlay = overlay, starTwinkle = starTwinkle)
        }

        content()
    }
}

/** Fixed near-black ink for the readability scrim/vignette — never a theme color. */
private val READABILITY_INK = Color(0xFF04070A)

/**
 * Renders a single decorative overlay asset with the gentle motion that
 * suits it. Every animation here is intentionally slow, low-amplitude and
 * scoped to one small Image layer — not a full-screen Canvas — so it stays
 * cheap even running continuously.
 */
@Composable
private fun BoxScope.PrayerOverlayLayer(overlay: PrayerOverlay, starTwinkle: Float) {
    val transition = rememberInfiniteTransition(label = "overlay_${overlay.name}")

    when (overlay) {
        PrayerOverlay.STARS -> {
            Image(
                painter = painterResource(id = overlay.drawableRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = (starTwinkle * 1.1f).coerceIn(0f, 0.9f) },
                contentScale = ContentScale.Crop
            )
        }

        PrayerOverlay.CLOUDS -> {
            val drift by transition.animateFloat(
                initialValue = -24f,
                targetValue = 24f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 14000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "cloudDrift"
            )
            Image(
                painter = painterResource(id = overlay.drawableRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = drift
                        alpha = 0.55f
                    },
                contentScale = ContentScale.Crop
            )
        }

        PrayerOverlay.BIRDS -> {
            val drift by transition.animateFloat(
                initialValue = -14f,
                targetValue = 14f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 20000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "birdDrift"
            )
            Image(
                painter = painterResource(id = overlay.drawableRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = drift
                        alpha = 0.4f
                    },
                contentScale = ContentScale.Crop
            )
        }

        PrayerOverlay.GLOW -> {
            Image(
                painter = painterResource(id = overlay.drawableRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.35f },
                contentScale = ContentScale.Crop
            )
        }

        PrayerOverlay.LIGHT, PrayerOverlay.SPARKLES -> {
            Image(
                painter = painterResource(id = overlay.drawableRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.4f },
                contentScale = ContentScale.Crop
            )
        }
    }
}

/**
 * A restrained, repeating 8-point-star lattice reminiscent of Islamic
 * geometric art — drawn at ~3% opacity so it reads as texture, never as
 * a foreground pattern competing with the cards above it.
 */
private fun drawIslamicLattice(scope: DrawScope) = with(scope) {
    val cell = 96f
    val strokeColor = Color(0xFFFFF8ED).copy(alpha = 0.028f)
    val stroke = Stroke(width = 1f)

    var y = -cell
    while (y < size.height + cell) {
        var x = -cell
        while (x < size.width + cell) {
            val path = Path().apply {
                val r = cell * 0.32f
                moveTo(x, y - r)
                lineTo(x + r * 0.7f, y - r * 0.7f)
                lineTo(x + r, y)
                lineTo(x + r * 0.7f, y + r * 0.7f)
                lineTo(x, y + r)
                lineTo(x - r * 0.7f, y + r * 0.7f)
                lineTo(x - r, y)
                lineTo(x - r * 0.7f, y - r * 0.7f)
                close()
            }
            drawPath(path, color = strokeColor, style = stroke)
            x += cell
        }
        y += cell
    }
}