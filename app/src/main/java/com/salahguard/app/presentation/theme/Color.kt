package com.salahguard.app.presentation.theme

import androidx.compose.ui.graphics.Color

// This file is the single source of truth for every raw color value in the
// app. `presentation/designsystem/ColorTokens.kt` (SGColors) builds a
// semantic layer on top of these - text/accent/surface/status/glass/chip
// roles - which is what new design-system components consume.
//
// Going forward, no new `Color(0x...)` literals should be added to screens
// or components; add or reuse a token here (and alias it in SGColors if a
// semantic name is needed) instead. Existing inline `Color(0x...)` literals
// still present in a few components (see Ticket #002 migration notes) are
// out of scope for this ticket, which only touches theme-level files.
//
// ---- Dark palette (High-Contrast Sanctuary) ----
val NightBackground = Color(0xFF040807)   // Deepest sanctuary black-green
val NightSurface = Color(0xFF0B1614)      // elevated surface
val NightSurfaceLighter = Color(0xFF122421) // interactive elements

// ---- Typography Hierarchy (Luxury Contrast) ----
val WarmIvory = Color(0xFFF8F4E9)        // Primary Heading
val SoftCream = Color(0xFFF3EEE3)        // Primary Body
val WarmStone = Color(0xFFD7D2C6)        // Secondary Text
val MutedSand = Color(0xFFB8B2A5)        // Tertiary Text / Meta
val DisabledText = Color(0xFF8F928C)     // Disabled state

// ---- Accent & Status (Premium Glow) ----
val Gold = Color(0xFFD6B34A)             // Main Gold Accent
val GoldBright = Color(0xFFF4D35E)       // Pending / Highlight
val SuccessGreen = Color(0xFF74C69D)     // Completed state
val MissedRed = Color(0xFFD97777)        // Missed state

// ---- Gradients & Atmospheric Effects ----
val SanctuaryGradient = listOf(Color(0xFF0B1614), Color(0xFF040807))
val GoldLeafGradient = listOf(Color(0xFFF4D35E), Color(0xFFD6B34A), Color(0xFFBF8F00))
val ForestGreen = Color(0xFF1E4D3E)
val ForestGreenGlow = Color(0xFF2E7D62)

// ---- Sprint 11A: Premium Home Screen tokens ----
// Additive only — used exclusively by Home-screen presentation files
// (HomeScreen, PrayerCountdownCard, HopeRecoveryCard, HomeSanctuaryBackground)
// so no other screen's appearance changes.
val EmeraldMid = Color(0xFF102B23)          // background gradient mid-tone
val EmeraldDeep = Color(0xFF071510)         // background gradient anchor
val EmeraldGlowSoft = Color(0xFF1F7A5A)     // radial "breathing" light, used at low alpha
val RadialGoldGlow = Color(0xFFF4D35E)      // warm light pool behind the hero card, low alpha
val GlassHighlight = Color(0xFFFFFFFF)      // translucent glass sheen, used at low alpha
val StarlightIvory = Color(0xFFF7F5EF)      // Fajr / Isha star specks
val SilhouetteInk = Color(0xFF030B08)       // distant mosque silhouette, near-black emerald
val SageMist = Color(0xFFAFC3B0)            // soft emerald-tinted tertiary text (WCAG-safe on dark)

// ---- Sprint 11B: photo-background readability scrim ----
// Additive only — used exclusively by HomeSanctuaryBackground to darken the
// top and bottom of the new per-prayer background photos so header text and
// card content keep the same contrast they had over the flat gradient.
val PhotoScrimEdge = Color(0xFF040807)      // matches NightBackground; used at varying alpha stops

// ---- Sprint 12: Atmospheric Background Engine ----
// Pure color tokens for the six prayer-period atmospheres rendered by
// AtmosphereBackground (see presentation/components/AtmosphereBackground.kt).
// Deliberately NOT photographs or illustrations — these are raw colors
// composed into gradients, blooms, haze and vignette layers at runtime.
// Additive only; nothing here replaces an existing token.

// Fajr — quiet, before sunrise, hopeful. Deep navy / soft slate / cool blue.
val FajrSkyTop = Color(0xFF060B14)
val FajrSkyMid = Color(0xFF162238)
val FajrSkyLow = Color(0xFF223A52)
val FajrBloom = Color(0xFF3E6E93)
val FajrHaze = Color(0xFF2A4258)

// Sunrise — first light, warm horizon, gentle awakening.
val SunriseSkyTop = Color(0xFF23213C)
val SunriseSkyMid = Color(0xFF6B4A66)
val SunriseSkyLow = Color(0xFFE08A5B)
val SunriseBloom = Color(0xFFFFC98B)
val SunriseHaze = Color(0xFFD97E63)

// Dhuhr — bright, clean, open, minimal, almost white, very soft.
val DhuhrSkyTop = Color(0xFFEEF3F6)
val DhuhrSkyMid = Color(0xFFDCE7EC)
val DhuhrSkyLow = Color(0xFFCBDBE2)
val DhuhrBloom = Color(0xFFFFFFFF)
val DhuhrHaze = Color(0xFFB9CDD6)

// Asr — warm afternoon, golden sunlight, soft warmth.
val AsrSkyTop = Color(0xFFF3DFB8)
val AsrSkyMid = Color(0xFFE7B876)
val AsrSkyLow = Color(0xFFD08F4E)
val AsrBloom = Color(0xFFFFE29C)
val AsrHaze = Color(0xFFC77B42)

// Maghrib — the richest atmosphere. Warm orange, rose, deep purple.
val MaghribSkyTop = Color(0xFF241030)
val MaghribSkyMid = Color(0xFF7A2E4C)
val MaghribSkyLow = Color(0xFFE0663B)
val MaghribBloom = Color(0xFFFF9457)
val MaghribHaze = Color(0xFFB23E63)

// Isha — silent, deep, elegant. Dark emerald, night blue, almost black.
val IshaSkyTop = Color(0xFF010806)
val IshaSkyMid = Color(0xFF071F1B)
val IshaSkyLow = Color(0xFF0B1E2C)
val IshaBloom = Color(0xFF12483B)
val IshaHaze = Color(0xFF0E2A38)