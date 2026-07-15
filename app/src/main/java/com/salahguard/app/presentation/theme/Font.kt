package com.salahguard.app.presentation.theme

import androidx.compose.ui.text.font.FontFamily

/**
 * Poppins - the primary Latin typeface across the app (clean, modern,
 * friendly - matches the reference design's typography spec).
 * Bundled as real TTF files under res/font/ rather than using the
 * Downloadable Fonts API, so there's no dependency on Google Play
 * Services or a runtime download the first time the app opens.
 */
/**
 * Poppins - the primary Latin typeface across the app (clean, modern,
 * friendly - matches the reference design's typography spec).
 *
 * NOTE: Currently falling back to system SansSerif because the bundled
 * .ttf files in res/font/ are actually WOFF2 format, which Android's
 * Resources.getFont() cannot load.
 */
val PoppinsFontFamily = FontFamily.SansSerif

/**
 * Noto Naskh Arabic - used specifically for Quranic verses and Arabic
 * text (elegant, clear, matches the reference design's "Elegant / Clear"
 * spec for Arabic content).
 */
val ArabicFontFamily = FontFamily.Default
