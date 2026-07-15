package com.salahguard.app.presentation.components

import androidx.annotation.DrawableRes
import com.salahguard.app.R

/**
 * A single decorative overlay asset (transparent WEBP) that can be layered
 * on top of a prayer's background photo. Each case carries the intent
 * behind it so [HomeSanctuaryBackground] can decide how to animate it
 * (e.g. stars twinkle, particles float, clouds drift) without the mapper
 * needing to know anything about Compose animation.
 */
enum class PrayerOverlay(@DrawableRes val drawableRes: Int) {
    STARS(R.drawable.overlay_stars),
    CLOUDS(R.drawable.overlay_clouds),
    BIRDS(R.drawable.overlay_birds),
    GLOW(R.drawable.overlay_glow),
    LIGHT(R.drawable.overlay_light),
    SPARKLES(R.drawable.overlay_sparkles)
}

/** The full "look" for a given prayer: one background photo + its overlays, back to front. */
data class PrayerAtmosphere(
    @DrawableRes val background: Int,
    val overlays: List<PrayerOverlay>
)

/**
 * Single source of truth mapping each prayer to its background photo and
 * overlay set. Keeping this separate from [HomeSanctuaryBackground] means
 * no drawable IDs are hardcoded in the UI layer itself, and the mapping
 * can be unit-tested or tuned independently of the rendering code.
 */
object PrayerBackgroundMapper {

    private val FAJR_ATMOSPHERE = PrayerAtmosphere(
        background = R.drawable.bg_fajr,
        overlays = listOf(PrayerOverlay.STARS)
    )

    // No dedicated Sunrise photo asset exists in res/drawable, so this
    // reuses the Fajr skyline (closest match: still low, pre-daylight
    // horizon) but swaps out the night overlays for warm daytime ones.
    // HomeSanctuaryBackground layers a warm amber/peach color-grade wash
    // on top (driven by AtmosphereTheme.SUNRISE) so it still reads as its
    // own distinct moment rather than "Fajr again".
    private val SUNRISE_ATMOSPHERE = PrayerAtmosphere(
        background = R.drawable.bg_fajr,
        overlays = listOf(PrayerOverlay.CLOUDS, PrayerOverlay.GLOW)
    )

    private val DHUHR_ATMOSPHERE = PrayerAtmosphere(
        background = R.drawable.bg_dhuhr,
        overlays = listOf(PrayerOverlay.CLOUDS, PrayerOverlay.BIRDS, PrayerOverlay.GLOW)
    )

    private val ASR_ATMOSPHERE = PrayerAtmosphere(
        background = R.drawable.bg_asr,
        overlays = listOf(PrayerOverlay.CLOUDS)
    )

    private val MAGHRIB_ATMOSPHERE = PrayerAtmosphere(
        background = R.drawable.bg_maghrib,
        overlays = listOf(PrayerOverlay.GLOW)
    )

    private val ISHA_ATMOSPHERE = PrayerAtmosphere(
        background = R.drawable.bg_isha,
        overlays = listOf(PrayerOverlay.STARS)
    )

    /**
     * Resolves the atmosphere for a given prayer name as surfaced by
     * [com.salahguard.app.presentation.screens.home.HomeUiState] (e.g. "Fajr",
     * "Dhuhr", ...). Lookup is case-insensitive since the ViewModel already
     * capitalizes it, but we don't want rendering to depend on that detail.
     *
     * Falls back to the Dhuhr (daylight) atmosphere for null/unknown values
     * so the background never renders blank while data is still loading.
     */
    fun atmosphereFor(prayerName: String?): PrayerAtmosphere {
        return when (prayerName?.trim()?.lowercase()) {
            "fajr" -> FAJR_ATMOSPHERE
            "sunrise", "shurooq", "sherooq" -> SUNRISE_ATMOSPHERE
            "dhuhr" -> DHUHR_ATMOSPHERE
            "asr" -> ASR_ATMOSPHERE
            "maghrib" -> MAGHRIB_ATMOSPHERE
            "isha" -> ISHA_ATMOSPHERE
            else -> DHUHR_ATMOSPHERE
        }
    }
}